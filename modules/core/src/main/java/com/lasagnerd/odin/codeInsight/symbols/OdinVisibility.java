package com.lasagnerd.odin.codeInsight.symbols;

public enum OdinVisibility {
    PACKAGE_PRIVATE,
    FILE_PRIVATE,
    PACKAGE_EXPORTED,
    NONE;

    public static OdinVisibility min(OdinVisibility v1, OdinVisibility v2) {
        if (v1 == NONE)
            return v2;
        if (v2 == NONE)
            return v1;
        return v1.ordinal() < v2.ordinal() ? v1 : v2;
    }

    public static OdinVisibility max(OdinVisibility v1, OdinVisibility v2) {
        if (v1 == NONE)
            return v2;
        if (v2 == NONE)
            return v1;
        return v1.ordinal() > v2.ordinal() ? v1 : v2;
    }
}
