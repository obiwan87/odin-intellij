package com.lasagnerd.odin.lang.stubs;

import com.lasagnerd.odin.lang.stubs.types.*;

@SuppressWarnings("unused")
public interface OdinStubElementTypes {

    OdinPackageClauseStubElementType PACKAGE_CLAUSE = new OdinPackageClauseStubElementType();
    OdinFileScopeStubElementType FILE_SCOPE = new OdinFileScopeStubElementType();
    OdinImportDeclarationStubElementType IMPORT_DECLARATION = new OdinImportDeclarationStubElementType();
    OdinShortVariableDeclarationStubElementType SHORT_VARIABLE_DECLARATION = new OdinShortVariableDeclarationStubElementType();
    OdinInitVariableDeclarationStubElementType INIT_VARIABLE_DECLARATION = new OdinInitVariableDeclarationStubElementType();
    OdinDeclaredIdentifierStubElementType DECLARED_IDENTIFIER = new OdinDeclaredIdentifierStubElementType();
    OdinConstantInitDeclarationStubElementType CONSTANT_INIT_DECLARATION = new OdinConstantInitDeclarationStubElementType();
}
