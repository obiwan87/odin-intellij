package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.insights.OdinDeclarationSpec;

import java.util.List;

public interface OdinScopeBlock extends PsiElement {
    List<OdinStatement> getBlockStatements();

    List<OdinDeclarationSpec> getDeclarationsSpecs();

}
