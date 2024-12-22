package com.lasagnerd.odin.lang.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.lasagnerd.odin.lang.stubs.types.*;
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
            case FILE_SCOPE -> OdinFileScopeStubElementType.INSTANCE;
            case PACKAGE_CLAUSE -> OdinPackageClauseStubElementType.INSTANCE;
            case IMPORT_DECLARATION -> new OdinImportDeclarationStubElementType();
            case SHORT_VARIABLE_DECLARATION -> new OdinShortVariableDeclarationStubElementType();
            case INIT_VARIABLE_DECLARATION -> new OdinInitVariableDeclarationStubElementType();
            case DECLARED_IDENTIFIER -> new OdinDeclaredIdentifierStubElementType();
            case CONSTANT_INIT_DECLARATION -> new OdinConstantInitDeclarationStubElementType();
            default -> null;
        };
    }
}
