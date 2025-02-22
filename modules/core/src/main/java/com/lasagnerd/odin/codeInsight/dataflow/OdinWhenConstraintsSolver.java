package com.lasagnerd.odin.codeInsight.dataflow;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.dataflow.cfg.OdinWhenBranchBlock;
import com.lasagnerd.odin.codeInsight.dataflow.cfg.OdinWhenInstruction;
import com.lasagnerd.odin.codeInsight.dataflow.cfg.OdinWhenTreeBuilder;
import com.lasagnerd.odin.codeInsight.dataflow.conditions.*;
import com.lasagnerd.odin.codeInsight.dataflow.constraints.OdinSymbolicEqualityConstraint;
import com.lasagnerd.odin.codeInsight.dataflow.constraints.OdinTransferFunction;
import com.lasagnerd.odin.codeInsight.dataflow.constraints.OdinValueEqualityConstraint;
import com.lasagnerd.odin.codeInsight.dataflow.constraints.OdinValueInequalityConstraint;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import com.lasagnerd.odin.lang.psi.OdinVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class OdinWhenConstraintsSolver extends OdinVisitor {
    public static final OdinTransferFunction IDENTITY = l -> l;

    public static OdinLattice solveLattice(OdinContext context, PsiElement element) {
        OdinLattice lattice = OdinLattice.fromContext(context);
        OdinWhenInstruction odinConditionalBlock = OdinWhenTreeBuilder.buildTree(element);
        return doSolveLattice(lattice, element, odinConditionalBlock);
    }

    public static OdinTransferFunction toTransferFunction(AtomicCondition condition) {
        if (condition.hasOnlyValues()) {
            return IDENTITY;
        }

        if (condition.getOperator() == OdinTypes.EQEQ) {
            // Both symbols -> introduces inequality or equality constraint
            if (condition.hasOnlySymbols()) {
                return OdinSymbolicEqualityConstraint.createOdinEqualityConstraint(condition);
            }
            // One value and one symbol
            else if (condition.hasValue() && condition.hasSymbol()) {
                return OdinValueEqualityConstraint.create(condition);
            }
        }

        if (condition.getOperator() == OdinTypes.NEQ) {
            if (condition.hasValue() && condition.hasSymbol()) {
                return OdinValueInequalityConstraint.create(condition);
            }
        }
        return IDENTITY;
    }

    private static OdinLattice doSolveLattice(@NotNull OdinLattice lattice, PsiElement element, OdinWhenInstruction odinConditionalBlock) {
        OdinLattice outLattice = lattice.copy();

        List<Condition> conditions = outLattice.getConditions();

        boolean foundAncestor = false;
        for (OdinWhenBranchBlock branch : odinConditionalBlock.getBranches()) {
            boolean isAncestor = PsiTreeUtil.isAncestor(branch.getPsiElement(), element, true);
            if (isAncestor) {
                foundAncestor = true;
                break;
            }
        }
        if (!foundAncestor) {
            return outLattice;
        }

        for (OdinWhenBranchBlock branch : odinConditionalBlock.getBranches()) {
            boolean isAncestor = PsiTreeUtil.isAncestor(branch.getPsiElement(), element, true);
            if (branch.getCondition() != null) {
                // Careful, ConditionExtractor uses reference resolver
                Condition condition = ConditionExtractor.toCondition(lattice.toContext(), branch.getCondition());
                if (condition != null) {
                    if (isAncestor) {
                        conditions.add(condition);
                    } else {
                        conditions.add(new NotCondition(condition));
                    }
                }
            }

            if (isAncestor) {
                CNFFormula cnfFormula = CNFFormula.fromConjunctions(conditions).simplify();
                List<Set<AtomicCondition>> clauses = cnfFormula.getClauses();

                for (Set<AtomicCondition> clause : clauses) {
                    OdinLattice clauseLattice = lattice.copy();
                    for (AtomicCondition atomicCondition : clause) {
                        // combine
                        OdinTransferFunction transferFunction = toTransferFunction(atomicCondition);
                        clauseLattice.combine(transferFunction.transfer(clauseLattice));
                    }
                    // intersect
                    outLattice.intersect(clauseLattice);
                }


                for (OdinWhenInstruction child : branch.getChildren()) {
                    if (PsiTreeUtil.isAncestor(child.getWhenStatement(), element, true)) {
                        // We need to apply the transfer function before entering the next block
                        // The transfer function depends on the conditions gathered so far
                        return doSolveLattice(outLattice, element, child);
                    }
                }
            }
        }

        return outLattice;
    }
}
