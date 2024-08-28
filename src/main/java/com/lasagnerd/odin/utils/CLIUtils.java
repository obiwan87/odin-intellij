package com.lasagnerd.odin.utils;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingAnsiEscapesAwareProcessHandler;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.util.io.BaseOutputReader;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CLIUtils {
    public static Optional<ProcessOutput> execute(GeneralCommandLine cli, int timeoutMillis) {
        final var handler = createProcessBlockingHandler(cli);
        return handler.map(h -> runProcessWithGlobalProgress(h, timeoutMillis));
    }

    public static Optional<? extends CapturingProcessHandler> createProcessBlockingHandler(GeneralCommandLine cli) {
        return BlockingCapturingProcessHandler.startProcess(cli);
    }

    public static ProcessOutput runProcessWithGlobalProgress(CapturingProcessHandler handler, @Nullable Integer timeoutMillis) {
        return runProcess(handler, ProgressManager.getGlobalProgressIndicator(), timeoutMillis);
    }

    public static ProcessOutput runProcess(CapturingProcessHandler handler, @Nullable ProgressIndicator indicator, @Nullable Integer timeoutMillis) {
        if (indicator != null && timeoutMillis != null) {
            return handler.runProcessWithProgressIndicator(indicator, timeoutMillis);
        } else if (indicator != null) {
            return handler.runProcessWithProgressIndicator(indicator);
        } else if (timeoutMillis != null) {
            return handler.runProcess(timeoutMillis);
        } else {
            return handler.runProcess();
        }
    }

    //From Apache Ant

    /**
     * Crack a command line.
     *
     * @param toProcess the command line to process.
     * @return the command line broken into strings.
     * An empty or null toProcess parameter results in a zero sized array.
     */
    public static String[] translateCommandline(String toProcess) throws ConfigurationException {
        if (toProcess == null || toProcess.isEmpty()) {
            //no command? no string
            return new String[0];
        }
        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer(toProcess, "\"' ", true);
        final ArrayList<String> result = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
                case inQuote:
                    if ("'".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                case inDoubleQuote:
                    if ("\"".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                default:
                    switch (nextTok) {
                        case "'" -> state = inQuote;
                        case "\"" -> state = inDoubleQuote;
                        case " " -> {
                            if (lastTokenHasBeenQuoted || !current.isEmpty()) {
                                result.add(current.toString());
                                current.setLength(0);
                            }
                        }
                        case null, default -> current.append(nextTok);
                    }
                    lastTokenHasBeenQuoted = false;
                    break;
            }
        }
        if (lastTokenHasBeenQuoted || !current.isEmpty()) {
            result.add(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote) {
            throw new ConfigurationException("unbalanced quotes in " + toProcess);
        }
        return result.toArray(new String[0]);
    }

    public static List<String> colored(boolean colored, boolean debug) {
        // TODO remove this check once JetBrains implements colored terminal in the debugger
        // https://youtrack.jetbrains.com/issue/CPP-11622/ANSI-color-codes-not-honored-in-Debug-Run-Configuration-output-window
        if (debug) {
            return Collections.emptyList();
        } else {
            return List.of("--color", colored ? "on" : "off");
        }
    }

    public static void executeCommandLineWithErrorChecks(GeneralCommandLine cli) throws ExecutionException, ProcessException {
        val outputOpt = execute(cli, Integer.MAX_VALUE);
        checkProcessOutput(cli, outputOpt.orElse(null));
    }

    public static void checkProcessOutput(GeneralCommandLine cli, @Nullable ProcessOutput output) throws ExecutionException, ProcessException {
        if (output == null) {
            throw new ExecutionException("Failed to execute \"" + cli.getCommandLineString() + "\"!");
        }
        if (output.getExitCode() != 0) {
            throw new ProcessException(cli.getCommandLineString(),
                    output.getStdout(),
                    output.getStderr(),
                    output.getExitCode());
        }
    }

    @RequiredArgsConstructor
    public static class ProcessException extends Exception {
        public final String command;
        public final String stdout;
        public final String stderr;
        public final int exitCode;
    }

    private static class BlockingCapturingProcessHandler extends CapturingAnsiEscapesAwareProcessHandler {
        private static final Logger LOG = Logger.getInstance(BlockingCapturingProcessHandler.class);

        public static Optional<BlockingCapturingProcessHandler> startProcess(GeneralCommandLine commandLine) {
            try {
                return Optional.of(new BlockingCapturingProcessHandler(commandLine));
            } catch (ExecutionException e) {
                LOG.error(e);
            }
            return Optional.empty();
        }

        public BlockingCapturingProcessHandler(@NotNull GeneralCommandLine commandLine) throws ExecutionException {
            super(commandLine);
        }

        @Override
        protected BaseOutputReader.@NotNull Options readerOptions() {
            return BaseOutputReader.Options.BLOCKING;
        }
    }
}
