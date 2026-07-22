package com.lasagnerd.odin.tools.lldbdap;

import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.debug.DebugLauncher;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Main {
    private static final DateTimeFormatter RUN_ID =
            DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss").withZone(ZoneOffset.UTC);

    public static void main(String[] args) {
        int exitCode;
        try {
            exitCode = run(Config.parse(args));
        } catch (UsageException e) {
            System.err.println("Error: " + e.getMessage());
            printUsage();
            exitCode = 2;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            exitCode = 1;
        }
        System.exit(exitCode);
    }

    private static int run(Config config) throws Exception {
        Files.createDirectories(config.logDirectory());
        Path stderrLog = config.logDirectory().resolve("adapter-stderr.log");
        Path stdoutLog = config.logDirectory().resolve("adapter-stdout.log");
        Path outboundLog = config.logDirectory().resolve("dap-outbound.bin");
        Path inboundLog = config.logDirectory().resolve("dap-inbound.bin");

        List<String> command = new ArrayList<>();
        command.add(config.adapter().toString());
        if (config.transport() == Transport.TCP) {
            command.add("--port");
            command.add(Integer.toString(config.port()));
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(config.adapter().toAbsolutePath().getParent().toFile());
        Process process = processBuilder.start();
        long pid = process.pid();
        Instant started = Instant.now();

        System.out.println("Adapter:   " + config.adapter().toAbsolutePath());
        System.out.println("PID:       " + pid);
        System.out.println("Transport: " + config.transport().name().toLowerCase(Locale.ROOT));
        System.out.println("Protocol:  " + (config.lsp4jEnabled() ? "LSP4J" : "custom"));
        System.out.println("Logs:      " + config.logDirectory().toAbsolutePath());

        process.onExit().thenAccept(exited -> System.err.println(
                "Adapter process exited with code " + exited.exitValue() +
                        " after " + Duration.between(started, Instant.now()).toMillis() + " ms"));

        Thread stderrPump = pump(process.getErrorStream(), stderrLog, "[adapter stderr] ");
        Socket socket = null;
        InputStream dapInput;
        OutputStream dapOutput;

        try {
            if (config.transport() == Transport.STDIO) {
                dapInput = process.getInputStream();
                dapOutput = process.getOutputStream();
            } else {
                pump(process.getInputStream(), stdoutLog, "[adapter stdout] ");
                socket = connect(process, config.port(), config.timeout());
                dapInput = socket.getInputStream();
                dapOutput = socket.getOutputStream();
                System.out.println("Connected to 127.0.0.1:" + config.port());
            }

            if (config.lsp4jEnabled()) {
                return runWithLsp4j(dapInput, dapOutput, inboundLog, outboundLog, config);
            }
            return runCustom(dapInput, dapOutput, inboundLog, outboundLog, config);
        } catch (TimeoutException e) {
            System.err.println("TIMEOUT: no initialize response within " + config.timeout().toSeconds() + " seconds.");
            System.err.println("Adapter alive: " + process.isAlive() + ", PID: " + pid);
            if (config.keepAlive() && process.isAlive()) {
                System.err.println("The adapter will remain alive for debugger attachment.");
                System.err.println("Attach Visual Studio to PID " + pid + ", then press Enter here to terminate it.");
                System.in.read();
            }
            return 4;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
            if (process.isAlive()) {
                process.destroy();
                if (!process.waitFor(2, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    process.waitFor(2, TimeUnit.SECONDS);
                }
            }
            stderrPump.join(1_000);
        }
    }

    private static int runWithLsp4j(InputStream input, OutputStream output,
                                    Path inboundLog, Path outboundLog,
                                    Config config) throws Exception {
        InputStream recordedInput = new RecordingInputStream(input, inboundLog);
        OutputStream recordedOutput = new RecordingOutputStream(output, outboundLog);
        ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "lsp4j-dap");
            thread.setDaemon(true);
            return thread;
        });
        try {
            DiagnosticClient client = new DiagnosticClient();
            Launcher<IDebugProtocolServer> launcher = DebugLauncher.createLauncher(
                    client, IDebugProtocolServer.class,
                    recordedInput, recordedOutput, executor, consumer -> consumer);
            IDebugProtocolServer server = launcher.getRemoteProxy();
            launcher.startListening();

            InitializeRequestArguments arguments = new InitializeRequestArguments();
            arguments.setClientID("odin-lldb-dap-diagnostics");
            arguments.setClientName("Odin LLDB-DAP Diagnostics");
            arguments.setAdapterID("lldb-dap");
            arguments.setPathFormat("path");
            arguments.setLinesStartAt1(true);
            arguments.setColumnsStartAt1(true);
            arguments.setSupportsVariableType(false);
            arguments.setSupportsMemoryReferences(true);

            System.out.println("Sending initialize through LSP4J 0.23.0");
            Capabilities capabilities = server.initialize(arguments)
                    .get(Math.max(1, config.timeout().toMillis()), TimeUnit.MILLISECONDS);
            System.out.println("Initialize completed through LSP4J: success=true");
            System.out.println("Capabilities: " + capabilities);

            if (config.program() != null) {
                Map<String, Object> launchArguments = Map.of(
                        "program", config.program().toAbsolutePath().toString(),
                        "cwd", config.cwd().toAbsolutePath().toString(),
                        "name", "LLDB-DAP diagnostics",
                        "type", "lldb-dap",
                        "request", "launch");

                System.out.println("Sending launch asynchronously for " + config.program().toAbsolutePath());
                CompletableFuture<Void> launchFuture = server.launch(launchArguments);

                client.initialized.get(config.timeout().toMillis(), TimeUnit.MILLISECONDS);
                System.out.println("Received initialized event");

                System.out.println("Sending configurationDone");
                server.configurationDone(new ConfigurationDoneArguments())
                        .get(config.timeout().toMillis(), TimeUnit.MILLISECONDS);
                System.out.println("configurationDone response received");

                launchFuture.get(config.timeout().toMillis(), TimeUnit.MILLISECONDS);
                System.out.println("Delayed launch response received: success=true");

                try {
                    client.terminated.get(2, TimeUnit.SECONDS);
                    System.out.println("Received terminated event");
                } catch (TimeoutException ignored) {
                    System.out.println("No terminated event within 2 seconds; disconnecting");
                }

                DisconnectArguments disconnect = new DisconnectArguments();
                disconnect.setTerminateDebuggee(false);
                server.disconnect(disconnect).get(config.timeout().toMillis(), TimeUnit.MILLISECONDS);
                System.out.println("Disconnect completed");
            }
            return 0;
        } catch (java.util.concurrent.ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw e;
        } finally {
            executor.shutdownNow();
        }
    }

    private static int runCustom(InputStream dapInput, OutputStream dapOutput,
                                 Path inboundLog, Path outboundLog,
                                 Config config) throws Exception {
        byte[] initializeBody = initializeRequest().getBytes(StandardCharsets.UTF_8);
        writeMessage(dapOutput, outboundLog, initializeBody);
        System.out.println("Sent initialize request (" + initializeBody.length + " bytes)");

        Instant deadline = Instant.now().plus(config.timeout());
        boolean initialized = false;
        while (true) {
            Duration remaining = Duration.between(Instant.now(), deadline);
            if (remaining.isNegative() || remaining.isZero()) {
                throw new TimeoutException("initialize response timed out");
            }

            Message message = readMessage(dapInput, inboundLog, remaining);
            System.out.println("Received DAP message (" + message.body().length + " bytes):");
            System.out.println(new String(message.body(), StandardCharsets.UTF_8));

            String json = new String(message.body(), StandardCharsets.UTF_8);
            if (json.contains("\"type\":\"response\"")
                    && json.contains("\"command\":\"initialize\"")) {
                boolean success = json.contains("\"success\":true");
                System.out.println("Initialize completed: success=" + success);
                if (!success || config.program() == null) {
                    return success ? 0 : 3;
                }
                byte[] launchBody = launchRequest(config).getBytes(StandardCharsets.UTF_8);
                writeMessage(dapOutput, outboundLog, launchBody);
                System.out.println("Sent launch asynchronously; waiting for initialized event");
            } else if (json.contains("\"type\":\"event\"")
                    && json.contains("\"event\":\"initialized\"")) {
                initialized = true;
                byte[] configurationDone = configurationDoneRequest().getBytes(StandardCharsets.UTF_8);
                writeMessage(dapOutput, outboundLog, configurationDone);
                System.out.println("Received initialized event; sent configurationDone");
            } else if (initialized && json.contains("\"type\":\"response\"")
                    && json.contains("\"command\":\"launch\"")) {
                boolean success = json.contains("\"success\":true");
                System.out.println("Delayed launch response received: success=" + success);
                return success ? 0 : 3;
            }
        }
    }

    private static Socket connect(Process process, int port, Duration timeout)
            throws IOException, InterruptedException, TimeoutException {
        Instant deadline = Instant.now().plus(timeout);
        IOException lastFailure = null;
        while (Instant.now().isBefore(deadline)) {
            if (!process.isAlive()) {
                throw new IOException("adapter exited before opening TCP port " + port);
            }
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress("127.0.0.1", port), 250);
                return socket;
            } catch (IOException e) {
                lastFailure = e;
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                Thread.sleep(100);
            }
        }
        TimeoutException timeoutException = new TimeoutException("adapter did not open TCP port " + port);
        if (lastFailure != null) {
            timeoutException.initCause(lastFailure);
        }
        throw timeoutException;
    }

    private static void writeMessage(OutputStream output, Path log, byte[] body) throws IOException {
        byte[] header = ("Content-Length: " + body.length + "\r\n\r\n")
                .getBytes(StandardCharsets.US_ASCII);
        append(log, header);
        append(log, body);
        output.write(header);
        output.write(body);
        output.flush();
    }

    private static Message readMessage(InputStream input, Path log, Duration timeout) throws Exception {
        CompletableFuture<Message> future = new CompletableFuture<>();
        Thread reader = Thread.ofPlatform().daemon().name("dap-message-reader").start(() -> {
            try {
                future.complete(readMessageBlocking(input, log));
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        try {
            return future.get(Math.max(1, timeout.toMillis()), TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw e;
        } finally {
            if (!future.isDone()) {
                reader.interrupt();
            }
        }
    }

    private static Message readMessageBlocking(InputStream input, Path log) throws IOException {
        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        int matched = 0;
        byte[] terminator = {'\r', '\n', '\r', '\n'};
        while (matched < terminator.length) {
            int value = input.read();
            if (value < 0) {
                throw new EOFException("adapter stdout closed before a complete DAP header");
            }
            headerBuffer.write(value);
            matched = value == terminator[matched] ? matched + 1 : (value == '\r' ? 1 : 0);
            if (headerBuffer.size() > 64 * 1024) {
                throw new IOException("DAP header exceeded 64 KiB");
            }
        }

        byte[] header = headerBuffer.toByteArray();
        String headerText = new String(header, StandardCharsets.US_ASCII);
        int contentLength = -1;
        for (String line : headerText.split("\\r\\n")) {
            int colon = line.indexOf(':');
            if (colon > 0 && line.substring(0, colon).trim().equalsIgnoreCase("Content-Length")) {
                contentLength = Integer.parseInt(line.substring(colon + 1).trim());
            }
        }
        if (contentLength < 0) {
            throw new IOException("missing Content-Length header: " + headerText);
        }

        byte[] body = input.readNBytes(contentLength);
        if (body.length != contentLength) {
            throw new EOFException("expected " + contentLength + " DAP body bytes, received " + body.length);
        }
        append(log, header);
        append(log, body);
        return new Message(body);
    }

    private static Thread pump(InputStream input, Path log, String prefix) {
        return Thread.ofPlatform().daemon().name("process-output-pump").start(() -> {
            byte[] buffer = new byte[8 * 1024];
            try {
                int count;
                while ((count = input.read(buffer)) >= 0) {
                    if (count == 0) {
                        continue;
                    }
                    byte[] bytes = java.util.Arrays.copyOf(buffer, count);
                    append(log, bytes);
                    System.err.print(prefix + new String(bytes, StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                System.err.println(prefix + "output capture stopped: " + e.getMessage());
            }
        });
    }

    private static synchronized void append(Path path, byte[] bytes) throws IOException {
        Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static String initializeRequest() {
        return "{\"seq\":1,\"type\":\"request\",\"command\":\"initialize\",\"arguments\":{" +
                "\"clientID\":\"odin-lldb-dap-diagnostics\"," +
                "\"clientName\":\"Odin LLDB-DAP Diagnostics\"," +
                "\"adapterID\":\"lldb-dap\"," +
                "\"pathFormat\":\"path\"," +
                "\"linesStartAt1\":true," +
                "\"columnsStartAt1\":true," +
                "\"supportsVariableType\":false," +
                "\"supportsMemoryReferences\":true}}";
    }

    private static String launchRequest(Config config) {
        return "{\"seq\":2,\"type\":\"request\",\"command\":\"launch\",\"arguments\":{" +
                "\"program\":\"" + jsonEscape(config.program().toAbsolutePath().toString()) + "\"," +
                "\"cwd\":\"" + jsonEscape(config.cwd().toAbsolutePath().toString()) + "\"," +
                "\"name\":\"LLDB-DAP diagnostics\"," +
                "\"type\":\"lldb-dap\",\"request\":\"launch\"}}";
    }

    private static String configurationDoneRequest() {
        return "{\"seq\":3,\"type\":\"request\",\"command\":\"configurationDone\",\"arguments\":{}}";
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void printUsage() {
        System.err.println("""
                Usage:
                  java -jar lldb-dap-diagnostics.jar --adapter <lldb-dap.exe> [options]

                Options:
                  --transport stdio|tcp   Transport to test (default: stdio)
                  --port <number>         TCP port (default: 47191)
                  --timeout <seconds>     Startup/initialize timeout (default: 10)
                  --lsp4j-enabled         Use LSP4J 0.23.0 instead of the custom DAP codec
                  --program <path>        After initialize, launch this executable
                  --cwd <path>            Launch working directory (default: program's parent)
                  --keep-alive            On timeout, wait for Enter so a debugger can attach
                  --log-dir <path>        Output directory (default: logs/<UTC timestamp>)
                  --help                  Show this help
                """);
    }

    private record Message(byte[] body) {
    }

    private static final class DiagnosticClient implements IDebugProtocolClient {
        private final CompletableFuture<Void> initialized = new CompletableFuture<>();
        private final CompletableFuture<Void> terminated = new CompletableFuture<>();

        @Override
        public void initialized() {
            initialized.complete(null);
        }

        @Override
        public void output(OutputEventArguments arguments) {
            String output = arguments.getOutput();
            if (output != null && !output.isBlank()) {
                System.out.print("[DAP " + arguments.getCategory() + "] " + output);
            }
        }

        @Override
        public void terminated(TerminatedEventArguments arguments) {
            terminated.complete(null);
        }
    }

    private static final class RecordingInputStream extends InputStream {
        private final InputStream delegate;
        private final Path log;

        private RecordingInputStream(InputStream delegate, Path log) {
            this.delegate = delegate;
            this.log = log;
        }

        @Override
        public int read() throws IOException {
            int value = delegate.read();
            if (value >= 0) {
                append(log, new byte[]{(byte) value});
            }
            return value;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            int count = delegate.read(bytes, offset, length);
            if (count > 0) {
                append(log, java.util.Arrays.copyOfRange(bytes, offset, offset + count));
            }
            return count;
        }
    }

    private static final class RecordingOutputStream extends OutputStream {
        private final OutputStream delegate;
        private final Path log;

        private RecordingOutputStream(OutputStream delegate, Path log) {
            this.delegate = delegate;
            this.log = log;
        }

        @Override
        public void write(int value) throws IOException {
            delegate.write(value);
            append(log, new byte[]{(byte) value});
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            delegate.write(bytes, offset, length);
            append(log, java.util.Arrays.copyOfRange(bytes, offset, offset + length));
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }
    }

    private enum Transport {
        STDIO, TCP
    }

    private record Config(Path adapter, Transport transport, int port, Duration timeout,
                          boolean keepAlive, boolean lsp4jEnabled, Path program, Path cwd,
                          Path logDirectory) {
        private static Config parse(String[] args) throws UsageException {
            Path adapter = null;
            Transport transport = Transport.STDIO;
            int port = 47_191;
            Duration timeout = Duration.ofSeconds(10);
            boolean keepAlive = false;
            boolean lsp4jEnabled = false;
            Path program = null;
            Path cwd = null;
            Path logDirectory = null;

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--adapter" -> adapter = Path.of(requireValue(args, ++i, "--adapter"));
                    case "--transport" -> {
                        String value = requireValue(args, ++i, "--transport");
                        transport = switch (value.toLowerCase(Locale.ROOT)) {
                            case "stdio" -> Transport.STDIO;
                            case "tcp" -> Transport.TCP;
                            default -> throw new UsageException("unknown transport: " + value);
                        };
                    }
                    case "--port" -> port = parsePositiveInt(requireValue(args, ++i, "--port"), "port");
                    case "--timeout" -> timeout = Duration.ofSeconds(
                            parsePositiveInt(requireValue(args, ++i, "--timeout"), "timeout"));
                    case "--keep-alive" -> keepAlive = true;
                    case "--lsp4j-enabled" -> lsp4jEnabled = true;
                    case "--program" -> program = Path.of(requireValue(args, ++i, "--program"));
                    case "--cwd" -> cwd = Path.of(requireValue(args, ++i, "--cwd"));
                    case "--log-dir" -> logDirectory = Path.of(requireValue(args, ++i, "--log-dir"));
                    case "--help", "-h" -> throw new UsageException("help requested");
                    default -> throw new UsageException("unknown argument: " + args[i]);
                }
            }

            if (adapter == null) {
                throw new UsageException("--adapter is required");
            }
            if (!Files.isRegularFile(adapter)) {
                throw new UsageException("adapter does not exist: " + adapter);
            }
            if (program != null && !Files.isRegularFile(program)) {
                throw new UsageException("program does not exist: " + program);
            }
            if (cwd == null && program != null) {
                cwd = program.toAbsolutePath().getParent();
            }
            if (cwd != null && !Files.isDirectory(cwd)) {
                throw new UsageException("cwd is not a directory: " + cwd);
            }
            if (logDirectory == null) {
                logDirectory = Path.of("logs", RUN_ID.format(Instant.now()));
            }
            return new Config(adapter, transport, port, timeout, keepAlive, lsp4jEnabled,
                    program, cwd, logDirectory);
        }

        private static String requireValue(String[] args, int index, String option) throws UsageException {
            if (index >= args.length) {
                throw new UsageException("missing value for " + option);
            }
            return args[index];
        }

        private static int parsePositiveInt(String value, String label) throws UsageException {
            try {
                int parsed = Integer.parseInt(value);
                if (parsed <= 0) {
                    throw new NumberFormatException();
                }
                return parsed;
            } catch (NumberFormatException e) {
                throw new UsageException(label + " must be a positive integer: " + value);
            }
        }
    }

    private static final class UsageException extends Exception {
        private UsageException(String message) {
            super(message);
        }
    }
}
