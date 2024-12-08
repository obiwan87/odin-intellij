package com.lasagnerd.odin.codeInsight.dataflow.cfg;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OdinWhenTreeBuilder {
    public static OdinWhenInstruction buildTree(PsiElement position) {
        OdinWhenStatement topMostWhenStatement = PsiTreeUtil.getTopmostParentOfType(position, OdinWhenStatement.class);
        if (topMostWhenStatement != null) {
            return buildWhenTree(topMostWhenStatement);
        }
        return new OdinWhenInstruction();
    }

    private static OdinWhenInstruction buildWhenTree(OdinWhenStatement parentWhenStatement) {
        OdinWhenInstruction instruction = new OdinWhenInstruction();
        instruction.setWhenStatement(parentWhenStatement);
        OdinWhenBlock whenBlock = parentWhenStatement.getWhenBlock();
        List<OdinWhenBlock> workingList = new ArrayList<>();
        workingList.add(whenBlock);

        OdinWhenBranchBlock previousBranch = null;

        while (!workingList.isEmpty()) {
            OdinWhenBlock currentWhenBlock = workingList.removeLast();
            OdinWhenBranchBlock whenBranch = new OdinWhenBranchBlock();
            whenBranch.setParentWhenStatement(instruction);
            OdinCondition condition = currentWhenBlock.getCondition();
            if (condition != null) {
                whenBranch.setCondition(condition.getExpression());
            }
            whenBranch.setPsiElement(currentWhenBlock.getStatementBody());
            whenBranch.setPreviousBranch(previousBranch);
            instruction.getBranches().add(whenBranch);

            addChildren(currentWhenBlock.getStatementBody(), whenBranch);

            previousBranch = whenBranch;
            OdinElseWhenBlock elseWhenBlock = currentWhenBlock.getElseWhenBlock();
            if (elseWhenBlock != null) {
                OdinWhenBlock nextItem = elseWhenBlock.getWhenBlock();
                if (nextItem != null) {
                    workingList.add(nextItem);
                } else {
                    OdinWhenBranchBlock elseBranch = new OdinWhenBranchBlock();
                    elseBranch.setParentWhenStatement(instruction);
                    elseBranch.setPreviousBranch(whenBranch);
                    elseBranch.setPsiElement(elseWhenBlock.getStatementBody());
                    instruction.getBranches().add(elseBranch);
                    addChildren(currentWhenBlock.getStatementBody(), elseBranch);
                }
            }
        }

        return instruction;
    }

    private static void addChildren(@Nullable OdinStatementBody statementBody, OdinWhenBranchBlock branch) {
        List<OdinWhenStatement> children = getChildren(statementBody);

        for (OdinWhenStatement child : children) {
            OdinWhenInstruction childInstruction = buildWhenTree(child);
            branch.getChildren().add(childInstruction);
        }
    }

    public static List<OdinWhenStatement> getChildren(OdinStatementBody statementBody) {
        if (statementBody == null)
            return Collections.emptyList();
        if (statementBody.getDoStatement() != null) {
            return PsiTreeUtil.getChildrenOfTypeAsList(statementBody, OdinWhenStatement.class);
        }
        if (statementBody.getBlock() != null) {
            if (statementBody.getBlock().getStatementList() != null) {
                return PsiTreeUtil.getChildrenOfTypeAsList(statementBody.getBlock().getStatementList(), OdinWhenStatement.class);
            }
        }
        return Collections.emptyList();
    }
}
