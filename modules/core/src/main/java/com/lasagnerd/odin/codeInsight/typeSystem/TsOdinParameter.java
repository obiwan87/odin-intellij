package com.lasagnerd.odin.codeInsight.typeSystem;

import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.symbols.OdinDeclarationSymbolResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TsOdinParameter {
    private String name;
    private OdinDeclaredIdentifier identifier;

    private boolean isExplicitPolymorphicParameter;

    private OdinExpression defaultValueExpression;
    private OdinParameterDeclaration parameterDeclaration;
    private OdinType psiType;
    private TsOdinType type;

    boolean anyInt;

    private int index;

    public boolean hasPolymorphicDeclarations() {
        if (!isExplicitPolymorphicParameter) {
            return !PsiTreeUtil.findChildrenOfType(parameterDeclaration,
                    OdinPolymorphicType.class).isEmpty();
        }
        return true;
    }

    public OdinSymbol toSymbol() {
        List<OdinSymbol> symbols = OdinDeclarationSymbolResolver.getSymbols(parameterDeclaration);
        return symbols.stream().filter(s -> s.getDeclaredIdentifier() == identifier)
                .findFirst()
                .orElse(null);
    }
}