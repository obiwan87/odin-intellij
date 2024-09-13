package com.lasagnerd.odin.codeInsight.typeSystem;

import com.intellij.psi.PsiElement;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinArrayType extends TsOdinType {
    TsOdinType elementType;
    PsiElement psiSizeElement;
    boolean soa;
    boolean simd;

    @Override
    public String getLabel() {
        return "[" + psiSizeElement.getText() + "]" + (elementType != null ? elementType.getLabel() : "<undefined>");
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.ARRAY;
    }
}
