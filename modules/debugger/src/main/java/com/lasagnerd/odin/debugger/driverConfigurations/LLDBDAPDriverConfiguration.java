package com.lasagnerd.odin.debugger.driverConfigurations;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.jetbrains.cidr.ArchitectureType;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.lasagnerd.odin.debugger.dapDrivers.DAPDebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.dapDrivers.LLDBDAPDriver;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

public class LLDBDAPDriverConfiguration extends DAPDebuggerDriverConfiguration {

    private final Path path;

    public LLDBDAPDriverConfiguration(Path path) {
        this.path = path;
    }

    public static @NotNull GeneralCommandLine createLLDBDAPDriverCommandLine(Path debuggerExecutable) {
        GeneralCommandLine cli = new GeneralCommandLine();
        cli.setExePath(debuggerExecutable.toString());
        cli.setWorkDirectory(debuggerExecutable.getParent().toString());
        cli.withEnvironment(
                Map.of(
                        "LLDB_USE_NATIVE_PDB_READER", "yes"
                )
        );
        return cli;
    }

    @Override
    public @NotNull String getDriverName() {
        return "LLDB Odin Debugger";
    }

    protected Path getDebuggerExecutable() {
        return path;
    }

    @Override
    public @NotNull DebuggerDriver createDriver(DebuggerDriver.@NotNull Handler handler, @NotNull ArchitectureType architectureType) throws ExecutionException {
        return new LLDBDAPDriver(handler, this, DebuggerDriver.StandardDebuggerLanguage.C);
    }

    @Override
    public @NotNull GeneralCommandLine createDriverCommandLine(@NotNull DebuggerDriver debuggerDriver, @NotNull ArchitectureType architectureType) {
        return createLLDBDAPDriverCommandLine(getDebuggerExecutable());
    }

    @Override
    public void customizeInitializeArguments(InitializeRequestArguments initArgs) {

    }
}
