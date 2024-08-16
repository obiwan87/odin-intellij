package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.refactoring.introduce.PsiIntroduceTarget;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import org.jetbrains.annotations.NotNull;

public class OdinIntroduceScope extends PsiIntroduceTarget<OdinExpression> {
    public OdinIntroduceScope(@NotNull OdinExpression odinExpression) {
        super(odinExpression);
    }
}
