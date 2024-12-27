package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.ui.LayeredIcon;
import com.lasagnerd.odin.OdinIcons;

import javax.swing.*;

public class OdinTestRunConfigurationType extends ConfigurationTypeBase {
    public static final String ID = "OdinTestRunConfiguration";

    public OdinTestRunConfigurationType() {
        super(ID, "Odin Test", "Odin test run configuration type",
                NotNullLazyValue.createValue(() -> LayeredIcon.layeredIcon(
                        new Icon[]{OdinIcons.OdinRunConfiguration, AllIcons.RunConfigurations.TestMark}
                )));
        addFactory(new OdinTestRunConfigurationFactory(this));
    }
}
