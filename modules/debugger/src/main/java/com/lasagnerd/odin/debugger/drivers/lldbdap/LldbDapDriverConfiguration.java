package com.lasagnerd.odin.debugger.drivers.lldbdap;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.jetbrains.cidr.ArchitectureType;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.lasagnerd.odin.debugger.drivers.dap.DAPDebuggerDriverConfiguration;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

public class LldbDapDriverConfiguration extends DAPDebuggerDriverConfiguration {

    private final Path path;

    public LldbDapDriverConfiguration(Path path) {
        this.path = path;
    }

    @Override
    public @NotNull String getDriverName() {
        return "LLDB Odin Debugger";
    }

    protected Path getDebuggerExecutable() {
        // TODO get from settings
        return path;
    }

    @Override
    public @NotNull DebuggerDriver createDriver(DebuggerDriver.@NotNull Handler handler, @NotNull ArchitectureType architectureType) throws ExecutionException {
        return new LldbDapDriver(handler, this);
    }

    @Override
    public @NotNull GeneralCommandLine createDriverCommandLine(@NotNull DebuggerDriver debuggerDriver, @NotNull ArchitectureType architectureType) throws ExecutionException {
        Path path = getDebuggerExecutable();
        GeneralCommandLine cli = new GeneralCommandLine();
        cli.setExePath(path.toString());
        cli.setWorkDirectory(path.getParent().toString());
        cli.withEnvironment(
                Map.of(
                        "LLDB_USE_NATIVE_PDB_READER", "yes"
                )
        );
        return cli;
    }

    @Override
    public void customizeInitializeArguments(InitializeRequestArguments initArgs) {

    }
}
