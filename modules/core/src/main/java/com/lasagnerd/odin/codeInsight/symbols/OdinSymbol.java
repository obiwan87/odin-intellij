package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinAttributeUtils;
import com.lasagnerd.odin.lang.psi.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@NoArgsConstructor
@Data
public class OdinSymbol {
    //    private OdinDeclaration declaration;
    private PsiNamedElement declaredIdentifier;
    private OdinType psiType;
    private List<OdinAttributesDefinition> attributes;

    private String name;
    private String packagePath;
    private OdinSymbolType symbolType;
    private OdinVisibility visibility;
    private OdinScope scope;
    private boolean hasUsing;
    private boolean implicitlyDeclared;
    private boolean builtin;
    private boolean visibleThroughUsing;
    private boolean foreign;

    public OdinSymbol(PsiNamedElement declaredIdentifier, @NotNull OdinVisibility visibility) {
        this.declaredIdentifier = declaredIdentifier;
        this.visibility = visibility;
        this.scope = OdinScope.NONE;
        this.name = declaredIdentifier.getName();

    }

    public OdinSymbol(PsiNamedElement declaredIdentifier) {
        this.declaredIdentifier = declaredIdentifier;
        this.visibility = OdinVisibility.NONE;
        this.scope = OdinScope.NONE;
        this.name = declaredIdentifier.getName();
    }

    public OdinDeclaration getDeclaration() {
        if (declaredIdentifier != null) {
            return PsiTreeUtil.getParentOfType(declaredIdentifier, OdinDeclaration.class, false);
        }
        return null;
    }

    public OdinSymbolOrigin getSymbolOrigin() {
        if (isVisibleThroughUsing()) return OdinSymbolOrigin.USING;
        if (isForeign()) return OdinSymbolOrigin.FOREIGN;
        return OdinSymbolOrigin.NONE;
    }

    public boolean isContext() {
        return this.isImplicitlyDeclared() && getName().equals("context");
    }

    public boolean isStatic() {
        return OdinAttributeUtils.containsAttribute(attributes, "static");
    }
}
