package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.lasagnerd.odin.lang.stubs.types.OdinFileScopeStubElementType;
import com.lasagnerd.odin.lang.stubs.types.OdinImportDeclarationElementType;
import com.lasagnerd.odin.lang.stubs.types.OdinPackageClauseStubElementType;
import com.lasagnerd.odin.lang.stubs.types.OdinVariableInitializationStubElementType;
import org.jetbrains.annotations.NotNull;

public class OdinStubElementTypeFactory {
    private OdinStubElementTypeFactory() {

    }

    public static IStubElementType<?, ?> create(@NotNull String name) {
        return switch (name) {
            case "FILE_SCOPE" -> new OdinFileScopeStubElementType(name);
            case "PACKAGE_CLAUSE" -> new OdinPackageClauseStubElementType(name);
            case "VARIABLE_INITIALIZATION_STATEMENT" -> new OdinVariableInitializationStubElementType(name);
            case "IMPORT_DECLARATION" -> new OdinImportDeclarationElementType(name);
//            case "CONSTANT_INITIALIZATION_STATEMENT" -> new OdinProcedureDeclarationStubElementType(name);
            default -> null;
        };
    }
}
