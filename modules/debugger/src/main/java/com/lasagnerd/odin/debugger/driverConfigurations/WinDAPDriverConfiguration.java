package com.lasagnerd.odin.debugger.driverConfigurations;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.jetbrains.cidr.ArchitectureType;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.lasagnerd.odin.debugger.OdinDebuggerLanguage;
import com.lasagnerd.odin.debugger.dapDrivers.DAPDebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.dapDrivers.WinDAPDriver;
import lombok.val;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class WinDAPDriverConfiguration extends DAPDebuggerDriverConfiguration {
    private final Path path;
    private final WinDAPDriver.HandshakeStrategy handshakeStrategy;

    public WinDAPDriverConfiguration(Path path, WinDAPDriver.HandshakeStrategy handshakeStrategy) {
        this.path = path;
        this.handshakeStrategy = handshakeStrategy;
    }

    protected Path getDebuggerExecutable() {
        return path;
    }

    @Override
    public @NotNull String getDriverName() {
        return "Odin Windows Debugger";
    }

    @Override
    public @NotNull DebuggerDriver createDriver(DebuggerDriver.@NotNull Handler handler, @NotNull ArchitectureType architectureType)
            throws ExecutionException {
        return new WinDAPDriver(handshakeStrategy, handler, this, OdinDebuggerLanguage.INSTANCE);
    }

    @Override
    public @NotNull GeneralCommandLine createDriverCommandLine(
            @NotNull DebuggerDriver debuggerDriver, @NotNull ArchitectureType architectureType) {
        val path = getDebuggerExecutable();
        val cli = new GeneralCommandLine();
        cli.setExePath(path.toString());
        cli.addParameters("--interpreter=vscode", "--extConfigDir=%USERPROFILE%\\.cppvsdbg\\extensions");
        cli.setWorkDirectory(path.getParent().toString());
        return cli;
    }

    @Override
    public void customizeInitializeArguments(InitializeRequestArguments initArgs) {
        initArgs.setPathFormat("path");
        initArgs.setAdapterID("cppvsdbg");
    }
}
