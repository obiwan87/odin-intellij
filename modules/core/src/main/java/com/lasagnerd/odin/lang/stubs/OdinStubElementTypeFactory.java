package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.lasagnerd.odin.lang.stubs.types.OdinVariableInitializationStubElementType;
import org.jetbrains.annotations.NotNull;

public class OdinStubElementTypeFactory {
    private OdinStubElementTypeFactory() {

    }

    public static IStubElementType<?, ?> create(@NotNull String name) {
        return switch (name) {
            case "VARIABLE_INITIALIZATION_STATEMENT" -> new OdinVariableInitializationStubElementType(name);
//            case "CONSTANT_INITIALIZATION_STATEMENT" -> new OdinProcedureDeclarationStubElementType(name);
            default -> null;
        };
    }
}
