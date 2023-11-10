package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;

import java.util.List;

public interface OdinDeclaration extends PsiElement {
    List<OdinDeclaredIdentifier> getDeclaredIdentifiers();
}
