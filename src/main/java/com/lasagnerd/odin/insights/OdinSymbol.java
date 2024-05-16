package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.OdinAttributeStatement;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinTypeDefinitionExpression;
import lombok.Data;

import java.util.List;

@Data
public class OdinSymbol {
    // Can't be an OdinDeclared identifier because here we might have
    PsiNamedElement declaredIdentifier;
    OdinExpression valueExpression;
    OdinTypeDefinitionExpression typeDefinitionExpression;
    OdinSymbolType symbolType;
    List<OdinAttributeStatement> attributeStatements;

    public boolean isTyped() {
        return typeDefinitionExpression != null;
    }

    public String getName() {
        return declaredIdentifier.getName();
    }

    boolean hasUsing;

    public enum OdinSymbolType {
        PARAMETER,
        FIELD,
        PROCEDURE,
        STRUCT,
        UNION,
        CONSTANT,
        VARIABLE,
        PACKAGE,
    }
}
