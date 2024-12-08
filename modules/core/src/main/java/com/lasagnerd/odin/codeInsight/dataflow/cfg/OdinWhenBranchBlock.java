package com.lasagnerd.odin.codeInsight.dataflow.cfg;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OdinWhenBranchBlock {
    PsiElement psiElement;
    OdinWhenInstruction parentWhenStatement;

    OdinWhenBranchBlock previousBranch;
    List<OdinWhenInstruction> children = new ArrayList<>();

    OdinExpression condition;
}
