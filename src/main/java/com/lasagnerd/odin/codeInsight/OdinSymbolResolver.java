package com.lasagnerd.odin.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class OdinSymbolResolver extends OdinVisitor {

    List<OdinSymbol> symbols = new ArrayList<>();
    OdinSymbol.OdinVisibility defaultVisibility;

    private OdinSymbolResolver() {

    }

    public static OdinSymbol createSymbol(OdinDeclaredIdentifier declaredIdentifier) {
        AtomicReference<OdinDeclaration> declaration = new AtomicReference<>();
        PsiElement declaringParent = PsiTreeUtil.findFirstParent(declaredIdentifier, parent -> {
            if(parent instanceof OdinDeclaration odinDeclaration) {
                if(odinDeclaration.getDeclaredIdentifiers().contains(declaredIdentifier)) {
                    declaration.set(odinDeclaration);
                }
            }
            return parent instanceof OdinFileScope || parent instanceof OdinScopeBlock;
        });

        if(declaringParent instanceof OdinFileScope fileScope) {
            Collection<OdinAttributeStatement> attributeStatements = PsiTreeUtil.findChildrenOfType(declaration.get(), OdinAttributeStatement.class);
            OdinSymbol.OdinVisibility globalFileVisibility = OdinScopeResolver.getGlobalFileVisibility(fileScope);
            return new OdinSymbol(declaredIdentifier, getVisibility(attributeStatements, globalFileVisibility));
        }

        if(declaringParent instanceof OdinScopeBlock) {
            if(declaration.get() != null) {
                if(declaration.get().getParent() instanceof OdinFileScopeStatementList fileScopeStatementList) {
                    Collection<OdinAttributeStatement> attributeStatements = PsiTreeUtil.findChildrenOfType(declaration.get(), OdinAttributeStatement.class);
                    OdinFileScope fileScopeStatementListParent = (OdinFileScope) fileScopeStatementList.getParent();
                    OdinSymbol.OdinVisibility globalFileVisibility = OdinScopeResolver.getGlobalFileVisibility(fileScopeStatementListParent);
                    return new OdinSymbol(declaredIdentifier, getVisibility(attributeStatements, globalFileVisibility));
                }
            }
            return new OdinSymbol(declaredIdentifier, OdinSymbol.OdinVisibility.LOCAL);
        }
        return new OdinSymbol(declaredIdentifier);
    }

    public static List<OdinSymbol> getLocalSymbols(OdinDeclaration odinDeclaration) {
        return getSymbols(OdinSymbol.OdinVisibility.LOCAL, odinDeclaration);
    }

    public static List<OdinSymbol> getSymbols(@NotNull OdinSymbol.OdinVisibility defaultVisibility, OdinDeclaration odinDeclaration) {
        OdinSymbolResolver odinSymbolResolver = new OdinSymbolResolver();
        odinSymbolResolver.defaultVisibility = defaultVisibility;
        odinDeclaration.accept(odinSymbolResolver);
        if (odinSymbolResolver.symbols.isEmpty()) {
            for (OdinDeclaredIdentifier declaredIdentifier : odinDeclaration.getDeclaredIdentifiers()) {
                OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier, defaultVisibility);
                odinSymbolResolver.symbols.add(odinSymbol);
            }
        }
        return odinSymbolResolver.symbols;
    }

    @Override
    public void visitParameterInitialization(@NotNull OdinParameterInitialization o) {
        boolean using = o.getParameter().getUsing() != null;
        OdinType type = null;

        if (o.getTypeDefinitionContainer() != null) {
            type = o.getTypeDefinitionContainer().getType();
        }

        OdinExpression valueExpression = o.getExpression();

        for (var declaredIdentifier : o.getDeclaredIdentifiers()) {
            OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier);
            odinSymbol.setValueExpression(valueExpression);
            odinSymbol.setPsiType(type);
            odinSymbol.setHasUsing(using);

            symbols.add(odinSymbol);
        }
    }

    @Override
    public void visitParameterDeclarator(@NotNull OdinParameterDeclarator o) {
        OdinType psiType = o.getTypeDefinition();
        for (OdinParameter odinParameter : o.getParameterList()) {
            OdinDeclaredIdentifier declaredIdentifier = odinParameter.getDeclaredIdentifier();
            OdinSymbol symbol = new OdinSymbol(declaredIdentifier);
            symbol.setHasUsing(odinParameter.getUsing() != null);
            symbol.setPsiType(psiType);
            symbols.add(symbol);
        }
    }

    @Override
    public void visitVariableDeclarationStatement(@NotNull OdinVariableDeclarationStatement o) {
        boolean hasUsing = o.getUsing() != null;

        for (var declaredIdentifier : o.getDeclaredIdentifiers()) {
            OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier);
            odinSymbol.setPsiType(o.getType());
            odinSymbol.setHasUsing(hasUsing);
            odinSymbol.setAttributeStatements(o.getAttributeStatementList());
            symbols.add(odinSymbol);
        }
    }

    @Override
    public void visitVariableInitializationStatement(@NotNull OdinVariableInitializationStatement o) {
        boolean hasUsing = o.getUsing() != null;
        OdinType typeDefinition = o.getType();
        for (int i = 0; i < o.getDeclaredIdentifiers().size(); i++) {
            OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifiers().get(i));
            OdinExpressionsList expressionsList = o.getExpressionsList();
            if (expressionsList.getExpressionList().size() > i) {
                OdinExpression odinExpression = expressionsList.getExpressionList().get(i);
                odinSymbol.setValueExpression(odinExpression);
            }
            odinSymbol.setHasUsing(hasUsing);
            if (typeDefinition != null)
                odinSymbol.setPsiType(typeDefinition);

            symbols.add(odinSymbol);
        }
    }

    @Override
    public void visitPackageDeclaration(@NotNull OdinPackageDeclaration o) {
        super.visitPackageDeclaration(o);
    }

    @Override
    public void visitImportDeclarationStatement(@NotNull OdinImportDeclarationStatement o) {
        var alias = o.getAlias();
        OdinSymbol odinSymbol = new OdinSymbol(Objects.requireNonNullElse(alias, o));
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.PACKAGE_REFERENCE);

        symbols.add(odinSymbol);
    }

    @Override
    public void visitProcedureDeclarationStatement(@NotNull OdinProcedureDeclarationStatement o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o.getAttributeStatementList(), defaultVisibility));
        odinSymbol.setAttributeStatements(o.getAttributeStatementList());
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.PROCEDURE);

        symbols.add(odinSymbol);
    }

    @Override
    public void visitStructDeclarationStatement(@NotNull OdinStructDeclarationStatement o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o.getAttributeStatementList(), defaultVisibility));
        odinSymbol.setAttributeStatements(o.getAttributeStatementList());
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.STRUCT);

        symbols.add(odinSymbol);
    }

    @Override
    public void visitProcedureOverloadDeclarationStatement(@NotNull OdinProcedureOverloadDeclarationStatement o) {
        OdinSymbol symbol = new OdinSymbol(o.getDeclaredIdentifier());
        symbol.setAttributeStatements(o.getAttributeStatementList());
        this.symbols.add(symbol);
    }

    private static @NotNull OdinSymbol.OdinVisibility getVisibility(@NotNull Collection<OdinAttributeStatement> attributeStatementList, OdinSymbol.OdinVisibility defaultVisibility) {
        OdinSymbol.OdinVisibility odinVisibility = OdinAttributeUtils.computeVisibility(attributeStatementList);
        return defaultVisibility == null ? odinVisibility : OdinSymbol.min(defaultVisibility, odinVisibility);
    }

}
