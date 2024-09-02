package com.lasagnerd.odin.debugger.drivers;

import com.intellij.execution.ExecutionException;
import com.intellij.util.system.CpuArch;
import com.jetbrains.cidr.ArchitectureType;
import com.lasagnerd.odin.debugger.drivers.dap.DAPDebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.drivers.dap.DAPDriver;
import com.lasagnerd.odin.debugger.drivers.dap.WrappedDebugServer;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class LLDBDAPDriver extends DAPDriver<
        IDebugProtocolServer, WrappedDebugServer<IDebugProtocolServer>,
        LLDBDAPDriver.LldbDapClient
        > {
    public LLDBDAPDriver(@NotNull Handler handler, DAPDebuggerDriverConfiguration config, DebuggerLanguage language) throws ExecutionException {
        super(handler, config, language);
    }

    @Override
    protected @NotNull String functionParser(String name) {
        int beginIndex = name.indexOf(".");
        if (beginIndex >= 0)
            return name.substring(beginIndex);
        return "<unknown function label>";
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
