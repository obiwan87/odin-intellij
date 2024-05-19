package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class OdinSymbol {
    // Can't be an OdinDeclared identifier because here we might have
    @NotNull
    private final PsiNamedElement declaredIdentifier;
    OdinExpression valueExpression;
    OdinType type;
    OdinSymbolType symbolType;
    List<OdinAttributeStatement> attributeStatements;

    public boolean isTyped() {
        return type != null;
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
