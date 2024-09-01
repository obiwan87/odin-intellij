package com.lasagnerd.odin;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface OdinIcons {
    Icon OdinRunConfiguration = IconLoader.getIcon("icons/odinFlat.svg", OdinIcons.class);
    Icon OdinFileType = IconLoader.getIcon("icons/odinFileType.svg", OdinIcons.class);

    Icon OdinSdk = IconLoader.getIcon("icons/odinSdk.svg", OdinIcons.class);

    interface Types {
        Icon Struct = IconLoader.getIcon("icons/struct.svg", Types.class);
        Icon Union = IconLoader.getIcon("icons/union.svg", Types.class);
    }
}
