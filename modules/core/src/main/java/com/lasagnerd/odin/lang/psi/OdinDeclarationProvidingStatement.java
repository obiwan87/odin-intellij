package com.lasagnerd.odin.lang.psi;

public interface OdinDeclarationProvidingStatement extends OdinStatement {
    OdinDeclaration getDeclaration();
}
