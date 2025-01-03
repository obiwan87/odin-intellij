package com.lasagnerd.odin.debugger.dapDrivers;

import com.intellij.execution.ExecutionException;
import com.intellij.util.system.CpuArch;
import com.jetbrains.cidr.ArchitectureType;
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
        return "<unknown function label>";
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
        args.put("cwd", "D:/dev/code/odin-test-project/src");
//        args.put("debuggerRoot", "D:/dev/code/odin-test-project/src");
        args.put("sourcePath", "D:/dev/code/odin-test-project/src");
        if (sysroot != null) {
        }

        args.put("name", "LLDB Remote Debug");
        args.put("type", getType());
        args.put("request", "attach");
        args.put("attachCommands", new String[]{connectionString});

        addArgsForLaunch(args);
        server.attachNow(args);

        DAPDriverUtils.get(initializeFuture);

        return new DAPInferior();
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
    protected void doStartDebugging(HashMap<String, Object> args) throws ExecutionException {
        server.launchNow(args);
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
                server.configurationDone(new ConfigurationDoneArguments()).get();
                start.run();
            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
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

    protected class LldbDapClient extends DAPDebuggerClient {

    }
}
