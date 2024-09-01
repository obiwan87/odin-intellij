package com.lasagnerd.odin.debugger.dap.lldb;

import com.intellij.execution.ExecutionException;
import com.intellij.util.system.CpuArch;
import com.jetbrains.cidr.ArchitectureType;
import com.lasagnerd.odin.debugger.dap.DAPDebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.dap.DAPDriver;
import com.lasagnerd.odin.debugger.dap.WrappedDebugServer;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class LldbDapDriver extends DAPDriver<
        IDebugProtocolServer, WrappedDebugServer<IDebugProtocolServer>,
        LldbDapDriver.LldbDapClient
        > {
    public LldbDapDriver(@NotNull Handler handler, DAPDebuggerDriverConfiguration config) throws ExecutionException {
        super(handler, config);
    }

    @Override
    protected @NotNull String functionParser(String name) {
        int beginIndex = name.indexOf(".");
        if (beginIndex >= 0)
            return name.substring(beginIndex);
        return "<unknown function name>";
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
    protected Class<IDebugProtocolServer> getServerInterface() {
        return IDebugProtocolServer.class;
    }

    @Override
    protected WrappedDebugServer<IDebugProtocolServer> wrapDebugServer(IDebugProtocolServer remoteProxy) {
        return new WrappedDebugServer<>(remoteProxy);
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
    public @Nullable String getArchitecture() {
        return ArchitectureType.forVmCpuArch(CpuArch.CURRENT).getId();
    }

    protected class LldbDapClient extends DAPDebuggerClient {

    }
}
