package com.lasagnerd.odin.codeInsight.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.Data;

@Data
public class TsOdinParameter {
    private String name;
    private OdinDeclaredIdentifier identifier;

    private boolean isExplicitPolymorphicParameter;

    private OdinExpression defaultValueExpression;
    private OdinType psiType;
    private TsOdinType type;

    private int index;
}