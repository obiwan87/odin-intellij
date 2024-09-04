package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface OdinDeclaration extends OdinPsiElement {
    List<OdinDeclaredIdentifier> getDeclaredIdentifiers();
}
