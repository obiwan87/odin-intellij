package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import org.jetbrains.annotations.NotNull;

public class OdinStubElementTypeFactory {

    public static final String PACKAGE_CLAUSE = "PACKAGE_CLAUSE";
    public static final String FILE_SCOPE = "FILE_SCOPE";
    public static final String IMPORT_DECLARATION = "IMPORT_DECLARATION";
    public static final String SHORT_VARIABLE_DECLARATION = "SHORT_VARIABLE_DECLARATION";
    public static final String INIT_VARIABLE_DECLARATION = "INIT_VARIABLE_DECLARATION";
    public static final String DECLARED_IDENTIFIER = "DECLARED_IDENTIFIER";
    public static final String CONSTANT_INIT_DECLARATION = "CONSTANT_INIT_DECLARATION";

    private OdinStubElementTypeFactory() {

    }

    public static IStubElementType<?, ?> create(@NotNull String name) {
        return switch (name) {
            case FILE_SCOPE -> OdinStubElementTypes.FILE_SCOPE;
            case PACKAGE_CLAUSE -> OdinStubElementTypes.PACKAGE_CLAUSE;
            case IMPORT_DECLARATION -> OdinStubElementTypes.IMPORT_DECLARATION;
            case SHORT_VARIABLE_DECLARATION -> OdinStubElementTypes.SHORT_VARIABLE_DECLARATION;
            case INIT_VARIABLE_DECLARATION -> OdinStubElementTypes.INIT_VARIABLE_DECLARATION;
            case DECLARED_IDENTIFIER -> OdinStubElementTypes.DECLARED_IDENTIFIER;
            case CONSTANT_INIT_DECLARATION -> OdinStubElementTypes.CONSTANT_INIT_DECLARATION;
            default -> null;
        };
    }
}
