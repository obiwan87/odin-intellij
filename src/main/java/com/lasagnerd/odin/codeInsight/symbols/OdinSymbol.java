package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@NoArgsConstructor
@Data
public class OdinSymbol {
//    private OdinDeclaration declaration;
    private PsiNamedElement declaredIdentifier;
    /**
     * Used for symbols with initialization. Don't know if it actually belongs here
     */
    private OdinExpression valueExpression;
    private OdinType psiType;
    private List<OdinAttributeStatement> attributeStatements;

    private String name;
    private String packagePath;
    private OdinSymbolType symbolType;
    private OdinVisibility visibility;
    private OdinScope scope;
    boolean hasUsing;
    boolean implicitlyDeclared;

    public OdinSymbol(PsiNamedElement declaredIdentifier, @NotNull OdinVisibility visibility) {
        this.declaredIdentifier = declaredIdentifier;
        this.visibility = visibility;
        this.name = declaredIdentifier.getName();
    }

    public OdinSymbol(PsiNamedElement declaredIdentifier) {
        this.declaredIdentifier = declaredIdentifier;
        this.visibility = OdinVisibility.PUBLIC;
        this.name = declaredIdentifier.getName();
    }

    public OdinDeclaration getDeclaration() {
        if(declaredIdentifier != null) {
            return PsiTreeUtil.getParentOfType(declaredIdentifier, OdinDeclaration.class, false);
        }
        return null;
    }

    public enum OdinSymbolType {
        UNKNOWN,
        PARAMETER,
        FIELD,
        PROCEDURE,
        PROCEDURE_OVERLOAD,
        STRUCT,
        UNION,
        ENUM_FIELD,
        ENUM,
        CONSTANT,
        VARIABLE,
        PACKAGE_REFERENCE,
        POLYMORPHIC_TYPE,
        LABEL,
        FOREIGN_IMPORT,
        SWIZZLE_FIELD, BIT_SET
    }

    public enum OdinVisibility {
        NONE,
        PACKAGE_PRIVATE,
        FILE_PRIVATE,
        PUBLIC
    }

    public enum OdinScope {
        GLOBAL,
        LOCAL,
        TYPE
    }

    public static OdinVisibility min(OdinVisibility v1, OdinVisibility v2) {
        return v1.ordinal() < v2.ordinal() ? v1 : v2;
    }

    public static OdinVisibility max(OdinVisibility v1, OdinVisibility v2) {
        return v1.ordinal() > v2.ordinal() ? v1 : v2;
    }
}
