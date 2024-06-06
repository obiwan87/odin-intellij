package com.lasagnerd.odin.codeInsight;

import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class OdinSymbolResolver extends OdinVisitor {

    List<OdinSymbol> symbols = new ArrayList<>();
    private final OdinSymbol.OdinVisibility defaultVisibility;
    private final OdinScope scope;

    public OdinSymbolResolver(OdinSymbol.OdinVisibility defaultVisibility, OdinScope scope) {
        this.defaultVisibility = defaultVisibility;
        this.scope = scope;
    }

    public static List<OdinSymbol> getLocalSymbols(OdinDeclaration odinDeclaration) {
        return getLocalSymbols(odinDeclaration, OdinScope.EMPTY);
    }

    public static List<OdinSymbol> getLocalSymbols(OdinDeclaration odinDeclaration, OdinScope odinScope) {
        return getSymbols(OdinSymbol.OdinVisibility.LOCAL, odinDeclaration, odinScope);
    }

    public static List<OdinSymbol> getSymbols(@NotNull OdinSymbol.OdinVisibility defaultVisibility, OdinDeclaration odinDeclaration, OdinScope scope) {
        OdinSymbolResolver odinSymbolResolver = new OdinSymbolResolver(defaultVisibility, scope);
        odinDeclaration.accept(odinSymbolResolver);
        if (odinSymbolResolver.symbols.isEmpty()) {
            System.out.println("No symbols found for declaration with type " + odinDeclaration.getClass().getSimpleName());
            for (OdinDeclaredIdentifier declaredIdentifier : odinDeclaration.getDeclaredIdentifiers()) {
                OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier, defaultVisibility);
                odinSymbolResolver.symbols.add(odinSymbol);
            }
        }
        return odinSymbolResolver.symbols;
    }

    @Override
    public void visitUnnamedParameter(@NotNull OdinUnnamedParameter o) {
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

        if(o.getDeclaredIdentifiers().size() == 1) {
            if(type != null) {
                symbols.addAll(OdinInsightUtils.getTypeSymbols(type, scope));
            } else {
                symbols.addAll(OdinInsightUtils.getTypeSymbols(o.getExpression(), scope));
            }
        }
    }

    @Override
    public void visitParameterDeclarator(@NotNull OdinParameterDeclarator o) {
        OdinType psiType = o.getTypeDefinition();
        boolean hasUsing = false;
        for (OdinParameter odinParameter : o.getParameterList()) {
            OdinDeclaredIdentifier declaredIdentifier = odinParameter.getDeclaredIdentifier();
            OdinSymbol symbol = new OdinSymbol(declaredIdentifier);
            hasUsing |= odinParameter.getUsing() != null;

            // TODO why does every parameter have to option of having a "using"?
            symbol.setHasUsing(hasUsing);
            symbol.setPsiType(psiType);

            symbols.add(symbol);
        }

        if(hasUsing) {
            if(o.getParameterList().size() == 1) {
                if(psiType != null) {
                    symbols.addAll(OdinInsightUtils.getTypeSymbols(psiType, scope));
                }
            }
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

        if(hasUsing) {
            if(o.getDeclaredIdentifiers().size() == 1) {
                symbols.addAll(OdinInsightUtils.getTypeSymbols(o.getType(), scope));
            }
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
            odinSymbol.setPsiType(typeDefinition);

            symbols.add(odinSymbol);
        }

        if(hasUsing) {
            if(o.getDeclaredIdentifiers().size() == 1 && o.getExpressionsList().getExpressionList().size() == 1) {
                if(o.getType() != null) {
                    symbols.addAll(OdinInsightUtils.getTypeSymbols(o.getType(), scope));
                } else {
                    OdinExpression odinExpression = o.getExpressionsList().getExpressionList().get(0);
                    symbols.addAll(OdinInsightUtils.getTypeSymbols(odinExpression, scope));
                }
            }
        }
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

    @Override
    public void visitUsingStatement(@NotNull OdinUsingStatement o) {
        List<OdinSymbol> typeSymbols = OdinInsightUtils.getTypeSymbols(o.getExpression(), this.scope);
        symbols.addAll(typeSymbols);
    }

    @Override
    public void visitForInParameterDeclaration(@NotNull OdinForInParameterDeclaration o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), OdinSymbol.OdinVisibility.LOCAL);
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.VARIABLE);
        symbols.add(odinSymbol);
    }

    @Override
    public void visitSwitchTypeVariableDeclaration(@NotNull OdinSwitchTypeVariableDeclaration o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), OdinSymbol.OdinVisibility.LOCAL);
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.VARIABLE);
        symbols.add(odinSymbol);
    }

    @Override
    public void visitPolymorphicType(@NotNull OdinPolymorphicType o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), OdinSymbol.OdinVisibility.LOCAL);
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.POLYMORPHIC_TYPE);
        symbols.add(odinSymbol);
    }

    @Override
    public void visitEnumDeclarationStatement(@NotNull OdinEnumDeclarationStatement o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o.getAttributeStatementList(), defaultVisibility));
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.ENUM);
        odinSymbol.setAttributeStatements(o.getAttributeStatementList());
        odinSymbol.setPsiType(o.getEnumType());
        symbols.add(odinSymbol);
    }

    @Override
    public void visitBitsetDeclarationStatement(@NotNull OdinBitsetDeclarationStatement o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o.getAttributeStatementList(), defaultVisibility));
        odinSymbol.setPsiType(o.getBitSetType());
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.BIT_SET);
        odinSymbol.setAttributeStatements(o.getAttributeStatementList());
        symbols.add(odinSymbol);
    }

    @Override
    public void visitConstantInitializationStatement(@NotNull OdinConstantInitializationStatement o) {
        OdinType typeDefinition = o.getType();
        for (int i = 0; i < o.getDeclaredIdentifiers().size(); i++) {
            OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifiers().get(i));
            OdinExpressionsList expressionsList = o.getExpressionsList();
            if (expressionsList.getExpressionList().size() > i) {
                OdinExpression odinExpression = expressionsList.getExpressionList().get(i);
                odinSymbol.setValueExpression(odinExpression);
            }

            odinSymbol.setHasUsing(false);
            odinSymbol.setPsiType(typeDefinition);
            odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.CONSTANT);
            symbols.add(odinSymbol);
        }

    }

    @Override
    public void visitUnionDeclarationStatement(@NotNull OdinUnionDeclarationStatement o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o.getAttributeStatementList(), defaultVisibility));
        odinSymbol.setPsiType(o.getUnionType());
        odinSymbol.setAttributeStatements(o.getAttributeStatementList());
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.UNION);
        symbols.add(odinSymbol);
    }

    @Override
    public void visitLabelDeclaration(@NotNull OdinLabelDeclaration o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), OdinSymbol.OdinVisibility.LOCAL);
        odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.LABEL);
        symbols.add(odinSymbol);
    }

    @Override
    public void visitForeignImportDeclarationStatement(@NotNull OdinForeignImportDeclarationStatement o) {
        for (OdinDeclaredIdentifier declaredIdentifier : o.getDeclaredIdentifiers()) {
            OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier);
            odinSymbol.setSymbolType(OdinSymbol.OdinSymbolType.FOREIGN_IMPORT);
            odinSymbol.setAttributeStatements(o.getAttributeStatementList());
            symbols.add(odinSymbol);
        }
    }

    public static @NotNull OdinSymbol.OdinVisibility getVisibility(@NotNull Collection<OdinAttributeStatement> attributeStatementList,
                                                                   OdinSymbol.OdinVisibility defaultVisibility) {
        OdinSymbol.OdinVisibility odinVisibility = OdinAttributeUtils.computeVisibility(attributeStatementList);
        return defaultVisibility == null ? odinVisibility : OdinSymbol.min(defaultVisibility, odinVisibility);
    }

}
