package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.lang.psi.OdinAttributesDefinition;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinType;
import com.lasagnerd.odin.lang.psi.OdinUsingStatement;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class OdinSymbol {
    // Psi stuff
    private PsiNamedElement declaredIdentifier;
    private OdinType psiType;
    private List<OdinAttributesDefinition> attributes;
    private OdinUsingStatement usingStatement;

    private String name;
    private String packagePath;
    private OdinSymbolType symbolType;
    private OdinVisibility visibility;
    private OdinScope scope;
    private boolean hasUsing;
    private boolean implicitlyDeclared;
    private boolean builtin;
    @With
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
        return OdinInsightUtils.containsAttribute(attributes, "static");
    }

    @Override
    public String toString() {
        return getName() + "(" + getSymbolType() + ")";
    }
}
