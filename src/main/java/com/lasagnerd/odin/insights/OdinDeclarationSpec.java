package com.lasagnerd.odin.insights;

import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinPolymorphicType;
import com.lasagnerd.odin.lang.psi.OdinTypeDefinitionExpression;
import lombok.Data;

@Data
public class OdinDeclarationSpec {
    OdinDeclaredIdentifier valueDeclaredIdentifier;
    OdinExpression valueExpression;
    OdinTypeDefinitionExpression typeDefinitionExpression;

    boolean hasUsing;
    boolean isVariadic;
}
