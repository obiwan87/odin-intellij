package com.lasagnerd.odin;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface OdinIcons {
    Icon OdinRunConfigurationIcon = IconLoader.getIcon("icons/odinFlat.svg", OdinIcons.class);
    Icon OdinFileTypeIcon = IconLoader.getIcon("icons/odinFileType.svg", OdinIcons.class);

    interface Types {
        Icon Struct = IconLoader.getIcon("icons/struct.svg", Types.class);
        Icon Union = IconLoader.getIcon("icons/union.svg", Types.class);
    }
}
