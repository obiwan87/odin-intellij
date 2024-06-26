package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinTypeDefinitionExpression;
import lombok.Data;

@Data
public class TsOdinParameter {
    String valueName;
    OdinDeclaredIdentifier valueDeclaredIdentifier;

    boolean isExplicitPolymorphicParameter;

    OdinTypeDefinitionExpression typeDefinitionExpression;
    TsOdinType type;

    int index;
}