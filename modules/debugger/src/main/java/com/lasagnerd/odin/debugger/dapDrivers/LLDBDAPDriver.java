package com.lasagnerd.odin.debugger.dapDrivers;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.system.CpuArch;
import com.jetbrains.cidr.ArchitectureType;
import com.jetbrains.cidr.execution.Installer;
import lombok.val;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LLDBDAPDriver extends DAPDriver<
        IDebugProtocolServer, WrappedDebugServer<IDebugProtocolServer>,
        LLDBDAPDriver.LldbDapClient
        > {
    private final CompletableFuture<Void> initializedEventFuture = new CompletableFuture<>();
    private CompletableFuture<Void> startDebuggingFuture;

    public LLDBDAPDriver(@NotNull Handler handler, DAPDebuggerDriverConfiguration config, DebuggerLanguage language) throws ExecutionException {
        super(handler, config, language);
    }

    private static String[] @NotNull [] toSourceMap(@NotNull List<PathMapping> pathMappings) {
        String[][] sourceMap = new String[pathMappings.size()][2];
        for (int i = 0; i < pathMappings.size(); i++) {
            PathMapping pathMapping = pathMappings.get(i);
            sourceMap[i] = new String[]{pathMapping.to, pathMapping.from};
        }
        return sourceMap;
    }

    @Override
    protected @NotNull String functionParser(String name) {
        int beginIndex = name.indexOf(".");
        if (beginIndex >= 0)
            return name.substring(beginIndex);
        return name;
    }

    @Override
    protected Class<IDebugProtocolServer> getServerInterface() {
        return IDebugProtocolServer.class;
    }

    @Override
    protected WrappedDebugServer<IDebugProtocolServer> wrapDebugServer(IDebugProtocolServer remoteProxy) {
        return new WrappedDebugServer<>(remoteProxy);
    }

    @Override
    public @NotNull Inferior loadForRemote(@NotNull String connectionString, @Nullable File symbolFile, @Nullable File sysroot, @NotNull List<PathMapping> pathMappings) throws ExecutionException {
        if (initializeFuture == null) {
            throw new IllegalStateException("DAPDriver$postConstructor wasn't called!");
        }

        HashMap<String, Object> args = new HashMap<>();
        if (symbolFile != null) {
            args.put("program", symbolFile.getAbsolutePath());
        }
        if (sysroot != null) {
            args.put("cwd", FileUtil.toSystemIndependentName(sysroot.getAbsolutePath()));
            args.put("sourcePath", FileUtil.toSystemIndependentName(sysroot.getAbsolutePath()));
        }

        args.put("name", "LLDB Remote Debug");
        args.put("type", getType());
        args.put("request", "attach");
        args.put("attachCommands", new String[]{connectionString});

        addArgsForLaunch(args);
        DAPDriverUtils.get(initializeFuture);
        startDebuggingFuture = server.attach(args);

        return new LLDBDAPInferior();
    }

    @Override
    public void addPathMapping(int index, @NotNull String from, @NotNull String to) throws ExecutionException {

    }

    @Override
    protected @NotNull String getType() {
        return "lldb-dap";
    }

    @Override
    protected LldbDapClient createDebuggerClient() {
        return this.new LldbDapClient();
    }

    @Override
    protected CompletableFuture<?> wrapInitialize(CompletableFuture<Capabilities> capabilitiesCompletableFuture) {
        return capabilitiesCompletableFuture;
    }

    @Override
    protected void doStartDebugging(HashMap<String, Object> args) {
        startDebuggingFuture = server.launch(args);
    }

    @Override
    public @NotNull Inferior loadForLaunch(@NotNull Installer installer, @Nullable String s) throws ExecutionException {
        if (initializeFuture == null) {
            throw new IllegalStateException("DAPDriver$postConstructor wasn't called!");
        }
        GeneralCommandLine cli = installer.install();
        HashMap<String, Object> args = new HashMap<>();
        args.put("program", FileUtil.toSystemDependentName(cli.getExePath()));
        args.put("cwd", cli.getWorkDirectory().toString());
        args.put("name", "CPP Debug");
        args.put("type", getType());
        args.put("request", "launch");
        addArgsForLaunch(args);
        if (cli instanceof PtyCommandLine) {
            args.put("console", "integratedTerminal");
        }
        String[] params = cli.getParametersList().getArray();
        if (params.length > 0) {
            args.put("args", params);
        }
        DAPDriverUtils.get(initializeFuture);
        doStartDebugging(args);
        return new LLDBDAPInferior();
    }

    @Override
    protected @NotNull String getClientName() {
        return "IntelliJ LLDB Odin";
    }

    @Override
    protected @NotNull String getClientID() {
        return "ij-lldb-odin";
    }

    @Override
    public @Nullable String getArchitecture() {
        return ArchitectureType.forVmCpuArch(CpuArch.CURRENT).getId();
    }

    protected class DAPRemoteInferior extends Inferior {
        private final Runnable start;

        public DAPRemoteInferior(Runnable start) {
            this.start = start;
        }

        @Override
        protected long startImpl() {
            try {
                DAPDriverUtils.get(initializedEventFuture);
                server.configurationDoneNow(new ConfigurationDoneArguments());
                DAPDriverUtils.get(startDebuggingFuture);
                start.run();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            return -1;
        }

        @Override
        protected void detachImpl() {
            val args = new DisconnectArguments();
            server.disconnect(args);
        }

        @Override
        protected boolean destroyImpl() {
            detachImpl();
            return true;
        }
    }

    protected class LLDBDAPInferior extends DAPInferior {
        @Override
        protected long startImpl() {
            try {
                DAPDriverUtils.get(initializedEventFuture);
                server.configurationDoneNow(new ConfigurationDoneArguments());
                DAPDriverUtils.get(startDebuggingFuture);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            return -1;
        }
    }

    protected class LldbDapClient extends DAPDebuggerClient {
        @Override
        public void initialized() {
            initializedEventFuture.complete(null);
        }
    }
}
