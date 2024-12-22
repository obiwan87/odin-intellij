package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.lasagnerd.odin.lang.stubs.types.*;
import org.jetbrains.annotations.NotNull;

public class OdinStubElementTypeFactory {
    private OdinStubElementTypeFactory() {

    }

    public static IStubElementType<?, ?> create(@NotNull String name) {
        return switch (name) {
            case "FILE_SCOPE" -> new OdinFileScopeStubElementType(name);
            case "PACKAGE_CLAUSE" -> new OdinPackageClauseStubElementType(name);
            case "IMPORT_DECLARATION" -> new OdinImportDeclarationElementType(name);
            case "SHORT_VARIABLE_DECLARATION" -> new OdinShortVariableDeclarationElementType(name);
            case "INIT_VARIABLE_DECLARATION" -> new OdinInitVariableDeclarationElementType(name);
            default -> null;
        };
    }
}
