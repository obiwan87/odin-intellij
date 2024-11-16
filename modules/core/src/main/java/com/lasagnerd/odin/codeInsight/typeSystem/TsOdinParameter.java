package com.lasagnerd.odin.codeInsight.typeSystem;

import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TsOdinParameter {
    private String name;
    private OdinDeclaredIdentifier identifier;

    private boolean isExplicitPolymorphicParameter;

    private OdinExpression defaultValueExpression;
    private OdinParameterDeclaration parameterDeclaration;
    private OdinType psiType;
    private TsOdinType type;

    boolean anyInt;

    private int index;

    public boolean hasPolymorphicDeclarations() {
        if(!isExplicitPolymorphicParameter) {
            return !PsiTreeUtil.findChildrenOfType(parameterDeclaration,
                    OdinPolymorphicType.class).isEmpty();
        }
        return true;
    }

}