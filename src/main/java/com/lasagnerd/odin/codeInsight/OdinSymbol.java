package com.lasagnerd.odin.codeInsight;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class OdinSymbol {
    // Can't be an OdinDeclared identifier because here we might have
    private final PsiNamedElement declaredIdentifier;
    /**
     * Used for symbols with initialization. Don't know if it actually belongs here
     */
    OdinExpression valueExpression;
    OdinType psiType;
    OdinSymbolType symbolType;
    List<OdinAttributeStatement> attributeStatements;

    private final OdinVisibility visibility;

    public OdinSymbol(PsiNamedElement declaredIdentifier, @NotNull OdinVisibility visibility) {
        this.declaredIdentifier = declaredIdentifier;
        this.visibility = visibility;
    }

    public OdinSymbol(PsiNamedElement declaredIdentifier) {
        this.declaredIdentifier = declaredIdentifier;
        this.visibility = OdinVisibility.PUBLIC;
    }

    public boolean isTyped() {
        return psiType != null;
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
        ENUM_FIELD,
        ENUM,
        CONSTANT,
        VARIABLE,
        PACKAGE_REFERENCE,
        POLYMORPHIC_TYPE,
        LABEL, BIT_SET
    }

    public enum OdinVisibility {
        LOCAL,
        PACKAGE_PRIVATE,
        FILE_PRIVATE,
        PUBLIC
    }

    public static OdinVisibility min(OdinVisibility v1, OdinVisibility v2) {
        return v1.ordinal() < v2.ordinal() ? v1 : v2;
    }

    public static OdinVisibility max(OdinVisibility v1, OdinVisibility v2) {
        return v1.ordinal() > v2.ordinal() ? v1 : v2;
    }
}
