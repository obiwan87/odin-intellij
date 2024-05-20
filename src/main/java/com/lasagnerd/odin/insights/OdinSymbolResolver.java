package com.lasagnerd.odin.insights;

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
            odinSymbolResolver.addSymbols(odinDeclaration);
        }
        return odinSymbolResolver.symbols;
    }

    @Override
    public void visitParameterInitialization(@NotNull OdinParameterInitialization o) {
        boolean using = o.getParameter().getUsing() != null;
        OdinTypeDefinitionExpression typeDefinitionExpression = null;
        OdinExpression valueExpression;
        OdinType type = null;

        if (o.getTypeDefinitionContainer() != null) {
            type = o.getTypeDefinitionContainer().getTypeDefinitionExpression().getType();
        }

        valueExpression = o.getExpression();

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
        OdinTypeDefinitionExpression typeDefinition = o.getTypeDefinition();
        for (OdinParameter odinParameter : o.getParameterList()) {
            OdinDeclaredIdentifier declaredIdentifier = odinParameter.getDeclaredIdentifier();
            OdinSymbol symbol = new OdinSymbol(declaredIdentifier);
            symbol.setHasUsing(odinParameter.getUsing() != null);
            symbol.setPsiType(typeDefinition.getType());
            symbols.add(symbol);
        }
    }

    @Override
    public void visitVariableDeclarationStatement(@NotNull OdinVariableDeclarationStatement o) {
        boolean hasUsing = o.getUsing() != null;

        for (var declaredIdentifier : o.getDeclaredIdentifiers()) {
            OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier);
            odinSymbol.setPsiType(o.getTypeDefinitionExpression().getType());
            odinSymbol.setHasUsing(hasUsing);
            symbols.add(odinSymbol);
        }
    }

    @Override
    public void visitVariableInitializationStatement(@NotNull OdinVariableInitializationStatement o) {
        boolean hasUsing = o.getUsing() != null;
        OdinTypeDefinitionExpression typeDefinition = o.getTypeDefinitionExpression();
        for (int i = 0; i < o.getDeclaredIdentifiers().size(); i++) {
            OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifiers().get(i));
            OdinExpressionsList expressionsList = o.getExpressionsList();
            if (expressionsList.getExpressionList().size() > i) {
                OdinExpression odinExpression = expressionsList.getExpressionList().get(i);
                odinSymbol.setValueExpression(odinExpression);
            }
            odinSymbol.setHasUsing(hasUsing);
            if (typeDefinition != null)
                odinSymbol.setPsiType(typeDefinition.getType());

            symbols.add(odinSymbol);
        }
    }

    private void addSymbols(@NotNull OdinDeclaration o) {
        for (OdinDeclaredIdentifier declaredIdentifier : o.getDeclaredIdentifiers()) {
            OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier);
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
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o));
        odinSymbol.setAttributeStatements(o.getAttributeStatementList());
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.PROCEDURE);
        odinSymbol.setAttributeStatements(o.getAttributeStatementList());
        symbols.add(odinSymbol);
    }

    private OdinSymbol.OdinVisibility getVisibility(@NotNull OdinProcedureDeclarationStatement o) {
        return fileVisibility == null ? computeVisibility(o.getAttributeStatementList()) : fileVisibility;
    }

    private static OdinSymbol.OdinVisibility computeVisibility(@NotNull List<OdinAttributeStatement> attributeStatements) {
        for (OdinAttributeStatement attributeStatement : attributeStatements) {
            for (OdinArgument odinArgument : attributeStatement.getArgumentList()) {
                if (odinArgument instanceof OdinNamedArgument odinNamedArgument) {
                    String text = odinNamedArgument.getIdentifierToken().getText();
                    if (text.equals("private")) {
                        OdinExpression valueExpression = odinNamedArgument.getExpression();
                        if (OdinInsightUtils.isStringLiteralWithValue(valueExpression, "file")) {
                            return OdinSymbol.OdinVisibility.FILE_PRIVATE;
                        }
                    }
                }

                if (odinArgument instanceof OdinUnnamedArgument unnamedArgument) {
                    OdinExpression expression = unnamedArgument.getExpression();
                    if (expression.getText().equals("private")) {
                        return OdinSymbol.OdinVisibility.PACKAGE_PRIVATE;
                    }
                }
            }
        }

        return OdinSymbol.OdinVisibility.PUBLIC;
    }
}
