package com.lasagnerd.odin.codeInsight.evaluation;

import com.lasagnerd.odin.lang.psi.OdinRefExpression;
import com.lasagnerd.odin.lang.psi.OdinVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * This class evaluates compile time expressions, such as where-constraints and constant expressions
 *
 */
public class OdinExpressionEvaluator extends OdinVisitor {
    @Override
    public void visitRefExpression(@NotNull OdinRefExpression o) {
        super.visitRefExpression(o);
    }

}
