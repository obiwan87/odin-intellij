package com.lasagnerd.odin.lang.stubs;

import com.lasagnerd.odin.lang.stubs.types.*;

@SuppressWarnings("unused")
public interface OdinStubElementTypes {

    Class<OdinPackageClauseStubElementType> PACKAGE_CLAUSE = OdinPackageClauseStubElementType.class;
    Class<OdinFileScopeStubElementType> FILE_SCOPE = OdinFileScopeStubElementType.class;
    Class<OdinImportDeclarationStubElementType> IMPORT_DECLARATION = OdinImportDeclarationStubElementType.class;
    Class<OdinShortVariableDeclarationStubElementType> SHORT_VARIABLE_DECLARATION = OdinShortVariableDeclarationStubElementType.class;
    Class<OdinInitVariableDeclarationStubElementType> INIT_VARIABLE_DECLARATION = OdinInitVariableDeclarationStubElementType.class;
    Class<OdinDeclaredIdentifierStubElementType> DECLARED_IDENTIFIER = OdinDeclaredIdentifierStubElementType.class;
    Class<OdinConstantInitDeclarationStubElementType> CONSTANT_INIT_DECLARATION = OdinConstantInitDeclarationStubElementType.class;
}
