package com.lasagnerd.odin.lang.psi;

public interface OdinDeclarationProvidingStatement<T extends OdinDeclaration> extends OdinStatement {
    T getDeclaration();
}
