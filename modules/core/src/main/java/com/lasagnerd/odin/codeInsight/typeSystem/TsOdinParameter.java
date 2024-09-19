package com.lasagnerd.odin.codeInsight.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TsOdinParameter {
    enum ParameterFlags {
        ANY_INT
    }
    private String name;
    private OdinDeclaredIdentifier identifier;

    private boolean isExplicitPolymorphicParameter;

    private OdinExpression defaultValueExpression;
    private OdinType psiType;
    private TsOdinType type;

    boolean anyInt;

    private int index;

}