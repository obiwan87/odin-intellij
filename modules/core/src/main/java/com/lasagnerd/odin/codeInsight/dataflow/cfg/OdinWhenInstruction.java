package com.lasagnerd.odin.codeInsight.dataflow.cfg;

import com.intellij.psi.PsiElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OdinWhenInstruction {
    PsiElement whenStatement;
    List<OdinWhenBranchBlock> branches = new ArrayList<>();
}
