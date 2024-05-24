package com.lasagnerd.odin.codeInsight;

import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OdinSymbolResolver extends OdinVisitor {

    List<OdinSymbol> symbols = new ArrayList<>();
    OdinSymbol.OdinVisibility fileVisibility;

    private OdinSymbolResolver() {

    }

    public static List<OdinSymbol> getSymbols(OdinDeclaration odinDeclaration) {
        return getSymbols(null, odinDeclaration);
    }

    public static List<OdinSymbol> getSymbols(OdinSymbol.OdinVisibility fileVisibility, OdinDeclaration odinDeclaration) {
        OdinSymbolResolver odinSymbolResolver = new OdinSymbolResolver();
        odinSymbolResolver.fileVisibility = fileVisibility;
        odinDeclaration.accept(odinSymbolResolver);
        if (odinSymbolResolver.symbols.isEmpty()) {
            for (OdinDeclaredIdentifier declaredIdentifier : odinDeclaration.getDeclaredIdentifiers()) {
                OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier);
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
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o.getAttributeStatementList()));
        odinSymbol.setAttributeStatements(o.getAttributeStatementList());
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.PROCEDURE);

        symbols.add(odinSymbol);
    }

    @Override
    public void visitStructDeclarationStatement(@NotNull OdinStructDeclarationStatement o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o.getAttributeStatementList()));
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

    private OdinSymbol.OdinVisibility getVisibility(@NotNull List<OdinAttributeStatement> attributeStatementList) {
        return fileVisibility == null ? OdinAttributeUtils.computeVisibility(attributeStatementList) : fileVisibility;
    }

}
