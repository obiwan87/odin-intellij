package com.lasagnerd.odin;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.PropertyKey;

public class OdinBundle extends DynamicBundle {
    public static final String ODIN_BUNDLE = "odin.Bundle";
    public static final OdinBundle INSTANCE = new OdinBundle();
    protected OdinBundle() {
        super(ODIN_BUNDLE);
    }

    @Nls
    public static String message(@PropertyKey(resourceBundle = ODIN_BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}
