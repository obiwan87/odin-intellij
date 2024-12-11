package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.OdinContext;

public class OdinFullSymbolTableBuilder extends OdinSymbolTableBuilderBase {
    public OdinFullSymbolTableBuilder(PsiElement originalPosition,
                                      String packagePath,
                                      OdinSymbolTableHelper.StopCondition stopCondition,
                                      OdinContext initialContext) {
        super(originalPosition, packagePath, stopCondition, initialContext);
    }
}
