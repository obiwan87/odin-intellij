package com.lasagnerd.odin.codeInsight.dataflow.conditions;

import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValues;
import com.lasagnerd.odin.codeInsight.evaluation.OdinExpressionEvaluator;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInTypes;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionExtractor extends OdinVisitor {
    Condition result;
    OdinContext context;

    private ConditionExtractor() {

    }

    public static Condition toCondition(OdinExpression expression) {
        if (expression == null)
            return Condition.EMPTY;
        ConditionExtractor conditionExtractor = new ConditionExtractor();
        expression.accept(conditionExtractor);
        return conditionExtractor.result;
    }

    @Override
    public void visitParenthesizedExpression(@NotNull OdinParenthesizedExpression o) {
        if (o.getExpression() != null) {
            o.getExpression().accept(this);
        }
    }

    @Override
    public void visitAndExpression(@NotNull OdinAndExpression o) {
        AndCondition andCondition = new AndCondition();
        andCondition.setLeft(toCondition(o.getLeft()));
        andCondition.setRight(toCondition(o.getLeft()));
        this.result = andCondition;
    }

    @Override
    public void visitOrExpression(@NotNull OdinOrExpression o) {
        OrCondition orCondition = new OrCondition();
        orCondition.setLeft(toCondition(o.getLeft()));
        orCondition.setRight(toCondition(o.getRight()));
        this.result = orCondition;
    }

    @Override
    public void visitEqeqExpression(@NotNull OdinEqeqExpression o) {
        AtomicCondition atomicCondition = new AtomicCondition(o);
        atomicCondition.setLeft(createValueOrSymbol(o.getLeft()));
        atomicCondition.setRight(createValueOrSymbol(o.getRight()));
        atomicCondition.setOperator(OdinTypes.EQEQ);
        this.result = atomicCondition;
    }

    @Override
    public void visitNeqExpression(@NotNull OdinNeqExpression o) {
        AtomicCondition atomicCondition = new AtomicCondition(o);
        atomicCondition.setLeft(createValueOrSymbol(o.getLeft()));
        atomicCondition.setRight(createValueOrSymbol(o.getRight()));
        atomicCondition.setOperator(OdinTypes.NEQ);
        this.result = atomicCondition;
    }

    @Override
    public void visitUnaryNotExpression(@NotNull OdinUnaryNotExpression o) {
        NotCondition notCondition = new NotCondition();
        notCondition.setInner(toCondition(o));
        this.result = notCondition;
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression o) {
        TsOdinType type = o.getInferredType();
        if (TsOdinBuiltInTypes.getBoolTypes().contains(type.baseType(true))) {
            AtomicCondition atomicCondition = new AtomicCondition(o);
            atomicCondition.setLeft(createValueOrSymbol(o));
            atomicCondition.setRight(new ValueOperand(EvOdinValues.BUILTIN_IDENTIFIERS.get("true")));
            atomicCondition.setOperator(OdinTypes.EQEQ);
            this.result = atomicCondition;
        }
    }

    @Override
    public void visitPsiElement(@NotNull OdinPsiElement o) {
        throw new UnsupportedOperationException("AST Nodes of type " + o.getClass().getSimpleName() + " are not supported");
    }

    private @Nullable Operand createValueOrSymbol(OdinExpression o) {
        if (o == null)
            return null;
        if (o instanceof OdinRefExpression refExpression && refExpression.getIdentifier() != null) {
            return new SymbolicOperand(refExpression.getIdentifier().getReferencedSymbol(context));
        }
        if (o instanceof OdinLiteralExpression || o instanceof OdinImplicitSelectorExpression) {
            return new ValueOperand(OdinExpressionEvaluator.evaluate(o));
        }

        return null;
    }
}
