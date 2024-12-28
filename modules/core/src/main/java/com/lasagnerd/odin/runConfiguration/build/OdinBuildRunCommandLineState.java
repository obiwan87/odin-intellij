package com.lasagnerd.odin.runConfiguration.build;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.lasagnerd.odin.runConfiguration.OdinBaseCommandLineState;
import com.lasagnerd.odin.runConfiguration.OdinBaseRunConfigurationOptions;
import com.lasagnerd.odin.runConfiguration.OdinRunConfigurationUtils;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class OdinBuildRunCommandLineState extends OdinBaseCommandLineState {
    private final OdinBaseRunConfigurationOptions options;

    public OdinBuildRunCommandLineState(@NotNull ExecutionEnvironment environment,
                                        @NotNull OdinBaseRunConfigurationOptions options) {
        super(environment);
        this.options = options;
    }

    public @NotNull GeneralCommandLine createCommandLine(boolean debug) {
        return OdinRunConfigurationUtils.createCommandLine(debug, getEnvironment(), options);
    }
}
