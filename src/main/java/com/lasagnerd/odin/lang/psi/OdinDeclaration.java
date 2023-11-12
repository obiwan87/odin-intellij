package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface OdinDeclaration extends PsiElement {
    List<OdinDeclaredIdentifier> getDeclaredIdentifiers();
}
