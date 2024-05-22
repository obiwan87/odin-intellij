package com.lasagnerd.odin.insights.typeSystem;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinArrayType extends TsOdinType {
    TsOdinType elementType;
    PsiElement psiSizeElement;

    @Override
    public String getLabel() {
        return "[" + psiSizeElement.getText() + "]" + (elementType != null ? elementType.getLabel() : "<undefined>");
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.ARRAY;
    }
}
