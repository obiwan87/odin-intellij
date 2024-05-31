package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.OdinSymbol;

import java.util.Collections;
import java.util.List;

public interface OdinScopeBlock extends PsiElement {
    default List<OdinStatement> getBlockStatements() {
        return Collections.emptyList();
    }

    default List<OdinSymbol> getSymbols() {
        return Collections.emptyList();
    }
}
