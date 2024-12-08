package com.lasagnerd.odin.codeInsight.dataflow.cfg;

import com.intellij.psi.PsiElement;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OdinWhenInstruction {
    PsiElement whenStatement;
    List<OdinWhenBranchBlock> branches = new ArrayList<>();
}
