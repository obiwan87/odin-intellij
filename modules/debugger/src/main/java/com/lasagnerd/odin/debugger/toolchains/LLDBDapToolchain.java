package com.lasagnerd.odin.debugger.toolchains;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.jetbrains.cidr.ArchitectureType;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.OdinDebuggerLanguage;
import com.lasagnerd.odin.debugger.drivers.dap.DAPDebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.drivers.LLDBDAPDriver;
import com.lasagnerd.odin.extensions.OdinDebuggerToolchain;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

public class LLDBDapToolchain implements OdinDebuggerToolchain, DebuggerDriverConfigurationProvider {
    public static LLDBDapToolchain INSTANCE = new LLDBDapToolchain();

    private LLDBDapToolchain() {

    }

    @Override
    public String getId() {
        return "odin-lldb-dap";
    }

    @Override
    public String getLabel() {
        return "LLDB DAP";
    }

    @Override
    public String[] getExecutableNames() {
        return new String[]{
                "lldb-dap"
        };
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isDownloadable() {
        return false;
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public DebuggerDriverConfiguration createDebuggerDriverConfiguration(String path) {
        return new LLDBDAPDriverConfiguration(Path.of(path));
    }

    public static class LLDBDAPDriverConfiguration extends DAPDebuggerDriverConfiguration {

        private final Path path;

        public LLDBDAPDriverConfiguration(Path path) {
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
            return new LLDBDAPDriver(handler, this, OdinDebuggerLanguage.INSTANCE);
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
}
