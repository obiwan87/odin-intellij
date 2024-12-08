package com.lasagnerd.odin.codeInsight.dataflow.conditions;

import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinTypes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CNFConverter {

    public CNFFormula toCNF(OdinExpression expression) {
        Condition condition = ConditionExtractor.toCondition(expression);
        return toCNF(condition);
    }

    public CNFFormula toCNF(Condition condition) {
        if (condition instanceof AtomicCondition) {
            // CNF is just one clause with that atomic condition
            return CNFFormula.fromSingleClause(Collections.singleton((AtomicCondition) condition));
        } else if (condition instanceof AndCondition andCond) {
            CNFFormula leftCNF = toCNF(andCond.getLeft());
            CNFFormula rightCNF = toCNF(andCond.getRight());
            // CNF(AND) is just union of clauses
            return leftCNF.and(rightCNF);
        } else if (condition instanceof OrCondition orCond) {
            CNFFormula leftCNF = toCNF(orCond.getLeft());
            CNFFormula rightCNF = toCNF(orCond.getRight());
            return distributeOrOverAnd(leftCNF, rightCNF);
        } else if (condition instanceof NotCondition notCond) {
            return handleNotCondition(notCond.getInner());
        } else {
            // If you have other condition types, handle them here.
            throw new UnsupportedOperationException("Unknown expression type: " + condition.getClass());
        }
    }

    /**
     * This method applies the distributive law to combine two CNF formulas under OR:
     * (CNF1 ∨ CNF2)
     * <p>
     * Given CNF(L) = (L1) && (L2) && ...
     * and CNF(R) = (R1) && (R2) && ...
     * <p>
     * To form CNF(L ∨ R), you must produce a CNF that represents:
     * (L1 ∨ R) && (L2 ∨ R) && ... if R is a clause, or similarly distribute if R has multiple clauses.
     * <p>
     * In general:
     * (X1 && X2 && ... && Xn) ∨ (Y1 && Y2 && ... && Ym)
     * = AND over all combinations of (Xi ∨ Yj)
     * = Distribute OR across all clauses from both sides.
     */
    private CNFFormula distributeOrOverAnd(CNFFormula leftCNF, CNFFormula rightCNF) {
        // Distributing OR over AND is like a Cartesian product of clauses:
        // For each clause in leftCNF and each clause in rightCNF, produce the distributed clauses.
        //
        // Actually:
        // (A && B) ∨ (C && D) = 
        // ((A ∨ C) && (A ∨ D)) && ((B ∨ C) && (B ∨ D))

        CNFFormula result = new CNFFormula();

        for (Set<AtomicCondition> leftClause : leftCNF.getClauses()) {
            for (Set<AtomicCondition> rightClause : rightCNF.getClauses()) {
                // Combine each pair of clauses with OR:
                // (leftClause ∨ rightClause)
                Set<AtomicCondition> newClause = new HashSet<>(leftClause);
                newClause.addAll(rightClause);
                result.addClause(newClause);
            }
        }

        return result;
    }

    /**
     * Handle a NotCondition. If `expr` is atomic, just wrap it as a negated atomic condition.
     * If not, you'd need to push the negation inward (not shown here).
     */
    private CNFFormula handleNotCondition(Condition expr) {
        if (expr instanceof AtomicCondition) {
            // Create a negated form of that atomic condition.
            // Suppose you have a way to negate an atomic condition. For example:
            AtomicCondition negated = negateAtomic((AtomicCondition) expr);
            return CNFFormula.fromSingleClause(Collections.singleton(negated));
        } else if (expr instanceof AndCondition || expr instanceof OrCondition) {
            // If not is applied to a compound, you must push negations down using De Morgan's laws.
            // That step should ideally be done before CNF conversion.
            // For example:
            // !(A && B) = (!A || !B)
            // !(A || B) = (!A && !B)
            // Here we show a simple example:
            if (expr instanceof AndCondition andCond) {
                // !(A && B) = (!A || !B)
                Condition notLeft = new NotCondition(andCond.getLeft());
                Condition notRight = new NotCondition(andCond.getRight());
                // Convert (notLeft || notRight)
                return toCNF(new OrCondition(notLeft, notRight));
            } else {
                // !(A || B) = (!A && !B)
                OrCondition orCond = (OrCondition) expr;
                Condition notLeft = new NotCondition(orCond.getLeft());
                Condition notRight = new NotCondition(orCond.getRight());
                // Convert (notLeft && notRight)
                return toCNF(new AndCondition(notLeft, notRight));
            }
        } else if (expr instanceof NotCondition innerNot) {
            // !!E = E, double negation elimination
            return toCNF(innerNot.getInner());
        } else {
            throw new UnsupportedOperationException("Unknown expression type under NOT: " + expr.getClass());
        }
    }

    /**
     * Example negation of an AtomicCondition.
     * In practice, you might store a boolean flag or operator field inside AtomicCondition.
     * For this example, let's assume AtomicCondition has a method to produce its negation.
     */
    private AtomicCondition negateAtomic(AtomicCondition atomic) {
        // Implement negation logic depending on your domain.
        // For example, if AtomicCondition has an operator ==, negation might be !=.
        // This is domain-specific. Here, we just assume there's a method:
        // return atomic.negate();
        IElementType operator;
        if (atomic.getOperator() == OdinTypes.EQEQ) {
            operator = OdinTypes.NEQ;
        } else if (atomic.getOperator() == OdinTypes.NEQ) {
            operator = OdinTypes.EQ;
        } else {
            throw new UnsupportedOperationException("Implement atomic negation based on your domain.");
        }

        AtomicCondition atomicCondition = new AtomicCondition(atomic.getExpression());
        atomicCondition.setOperands(atomic.getOperands());
        atomicCondition.setOperator(operator);
        return atomicCondition;

    }
}
