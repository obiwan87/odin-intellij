package com.lasagnerd.odin.codeInsight.typeSystem;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.lang.psi.OdinMatrixType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinMatrixType extends TsOdinTypeBase implements TsOdinElementOwner {
    private TsOdinType elementType;

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.UNKNOWN;
    }

    @Override
    public String getLabel() {
        OdinMatrixType psiType = type();
        return "matrix[" + psiType.getArraySizeList().stream().map(PsiElement::getText).collect(Collectors.joining(", ")) + "]" + labelOrEmpty(elementType).trim();
    }
}
