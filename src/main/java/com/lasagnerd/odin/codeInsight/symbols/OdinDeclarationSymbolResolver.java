package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.codeInsight.OdinAttributeUtils;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class OdinDeclarationSymbolResolver extends OdinVisitor {
    private static final Logger LOG = Logger.getInstance(OdinDeclarationSymbolResolver.class);
    List<OdinSymbol> symbols = new ArrayList<>();
    private final OdinSymbol.OdinVisibility defaultVisibility;
    private final OdinSymbolTable symbolTable;

    public OdinDeclarationSymbolResolver(OdinSymbol.OdinVisibility defaultVisibility, OdinSymbolTable symbolTable) {
        this.defaultVisibility = defaultVisibility;
        this.symbolTable = symbolTable;
    }

    public static List<OdinSymbol> getLocalSymbols(OdinDeclaration odinDeclaration) {
        return getLocalSymbols(odinDeclaration, OdinSymbolTable.EMPTY);
    }

    public static List<OdinSymbol> getLocalSymbols(OdinDeclaration odinDeclaration, OdinSymbolTable odinSymbolTable) {
        return getSymbols(OdinSymbol.OdinVisibility.NONE, odinDeclaration, odinSymbolTable);
    }

    public static List<OdinSymbol> getSymbols(@NotNull OdinSymbol.OdinVisibility defaultVisibility,
                                              OdinDeclaration odinDeclaration,
                                              OdinSymbolTable symbolTable) {
        OdinDeclarationSymbolResolver odinDeclarationSymbolResolver = new OdinDeclarationSymbolResolver(defaultVisibility, symbolTable);
        odinDeclaration.accept(odinDeclarationSymbolResolver);
        if (odinDeclarationSymbolResolver.symbols.isEmpty()) {
            LOG.debug("No symbols found for declaration with type " + odinDeclaration.getClass().getSimpleName());
            VirtualFile virtualFile = odinDeclaration.getContainingFile().getVirtualFile();
            if (virtualFile != null) {
                LOG.debug("Containing file: " + virtualFile.getPath());
            }
            LOG.debug("Text: " + odinDeclaration.getText());
        }
        return odinDeclarationSymbolResolver.symbols;
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
            odinSymbol.setSymbolType(OdinSymbolType.PARAMETER);
            symbols.add(odinSymbol);
        }

        if (using && o.getDeclaredIdentifiers().size() == 1) {
            if (type != null) {
                symbols.addAll(OdinInsightUtils.getTypeElements(type, symbolTable));
            } else {
                symbols.addAll(OdinInsightUtils.getTypeElements(o.getExpression(), symbolTable));
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
            symbol.setSymbolType(OdinSymbolType.PARAMETER);

            symbols.add(symbol);
        }

        if (hasUsing) {
            if (o.getParameterList().size() == 1) {
                if (psiType != null) {
                    symbols.addAll(OdinInsightUtils.getTypeElements(psiType, symbolTable));
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
            odinSymbol.setSymbolType(OdinSymbolType.VARIABLE);
            symbols.add(odinSymbol);
        }

        if (hasUsing) {
            if (o.getDeclaredIdentifiers().size() == 1) {
                symbols.addAll(OdinInsightUtils.getTypeElements(o.getType(), symbolTable));
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
            odinSymbol.setSymbolType(OdinSymbolType.VARIABLE);
            odinSymbol.setHasUsing(hasUsing);
            odinSymbol.setPsiType(typeDefinition);

            symbols.add(odinSymbol);
        }

        if (hasUsing) {
            if (o.getDeclaredIdentifiers().size() == 1 && o.getExpressionsList().getExpressionList().size() == 1) {
                if (o.getType() != null) {
                    symbols.addAll(OdinInsightUtils.getTypeElements(o.getType(), symbolTable));
                } else {
                    OdinExpression odinExpression = o.getExpressionsList().getExpressionList().get(0);
                    symbols.addAll(OdinInsightUtils.getTypeElements(odinExpression, symbolTable));
                }
            }
        }
    }

    @Override
    public void visitImportDeclarationStatement(@NotNull OdinImportDeclarationStatement o) {
        var alias = o.getAlias();
        OdinSymbol odinSymbol = new OdinSymbol(Objects.requireNonNullElse(alias, o));
        odinSymbol.setSymbolType(OdinSymbolType.PACKAGE_REFERENCE);

        symbols.add(odinSymbol);
    }

    @Override
    public void visitProcedureDeclarationStatement(@NotNull OdinProcedureDeclarationStatement o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o.getAttributeStatementList(), defaultVisibility));
        odinSymbol.setAttributeStatements(o.getAttributeStatementList());
        odinSymbol.setSymbolType(OdinSymbolType.PROCEDURE);

        symbols.add(odinSymbol);
    }

    @Override
    public void visitStructDeclarationStatement(@NotNull OdinStructDeclarationStatement o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o.getAttributeStatementList(), defaultVisibility));
        odinSymbol.setAttributeStatements(o.getAttributeStatementList());
        odinSymbol.setSymbolType(OdinSymbolType.STRUCT);

        symbols.add(odinSymbol);
    }

    @Override
    public void visitProcedureOverloadDeclarationStatement(@NotNull OdinProcedureOverloadDeclarationStatement o) {
        OdinSymbol symbol = new OdinSymbol(o.getDeclaredIdentifier());
        symbol.setAttributeStatements(o.getAttributeStatementList());
        symbol.setSymbolType(OdinSymbolType.PROCEDURE_OVERLOAD);
        this.symbols.add(symbol);
    }

    @Override
    public void visitUsingStatement(@NotNull OdinUsingStatement o) {
        List<OdinSymbol> typeSymbols = OdinInsightUtils.getTypeElements(o.getExpression(), this.symbolTable);
        symbols.addAll(typeSymbols);
    }

    @Override
    public void visitForInParameterDeclaration(@NotNull OdinForInParameterDeclaration o) {
        for (OdinForInParameterDeclarator odinForInParameterDeclarator : o.getForInParameterDeclaratorList()) {
            PsiNamedElement declaredIdentifier = odinForInParameterDeclarator.getDeclaredIdentifier();
            OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier, OdinSymbol.OdinVisibility.NONE);
            odinSymbol.setSymbolType(OdinSymbolType.VARIABLE);
            symbols.add(odinSymbol);
        }
    }

    @Override
    public void visitSwitchTypeVariableDeclaration(@NotNull OdinSwitchTypeVariableDeclaration o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), OdinSymbol.OdinVisibility.NONE);
        odinSymbol.setSymbolType(OdinSymbolType.VARIABLE);
        symbols.add(odinSymbol);
    }

    @Override
    public void visitPolymorphicType(@NotNull OdinPolymorphicType o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), OdinSymbol.OdinVisibility.NONE);
        odinSymbol.setSymbolType(OdinSymbolType.POLYMORPHIC_TYPE);
        symbols.add(odinSymbol);
    }

    @Override
    public void visitEnumDeclarationStatement(@NotNull OdinEnumDeclarationStatement o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o.getAttributeStatementList(), defaultVisibility));
        odinSymbol.setSymbolType(OdinSymbolType.ENUM);
        odinSymbol.setAttributeStatements(o.getAttributeStatementList());
        odinSymbol.setPsiType(o.getEnumType());
        symbols.add(odinSymbol);
    }

    @Override
    public void visitBitsetDeclarationStatement(@NotNull OdinBitsetDeclarationStatement o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o.getAttributeStatementList(), defaultVisibility));
        odinSymbol.setPsiType(o.getBitSetType());
        odinSymbol.setSymbolType(OdinSymbolType.BIT_SET);
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
            odinSymbol.setSymbolType(OdinSymbolType.CONSTANT);
            odinSymbol.setAttributeStatements(o.getAttributeStatementList());
            symbols.add(odinSymbol);
        }

    }

    @Override
    public void visitUnionDeclarationStatement(@NotNull OdinUnionDeclarationStatement o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), getVisibility(o.getAttributeStatementList(), defaultVisibility));
        odinSymbol.setPsiType(o.getUnionType());
        odinSymbol.setAttributeStatements(o.getAttributeStatementList());
        odinSymbol.setSymbolType(OdinSymbolType.UNION);
        symbols.add(odinSymbol);
    }

    @Override
    public void visitLabelDeclaration(@NotNull OdinLabelDeclaration o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), OdinSymbol.OdinVisibility.NONE);
        odinSymbol.setSymbolType(OdinSymbolType.LABEL);
        symbols.add(odinSymbol);
    }

    @Override
    public void visitForeignImportDeclarationStatement(@NotNull OdinForeignImportDeclarationStatement o) {
        for (OdinDeclaredIdentifier declaredIdentifier : o.getDeclaredIdentifiers()) {
            OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier);
            odinSymbol.setSymbolType(OdinSymbolType.FOREIGN_IMPORT);
            odinSymbol.setAttributeStatements(o.getAttributeStatementList());
            symbols.add(odinSymbol);
        }
    }

    @Override
    public void visitFieldDeclarationStatement(@NotNull OdinFieldDeclarationStatement o) {
        OdinType type = o.getType();
        boolean hasUsing = o.getUsing() != null;
        for (OdinDeclaredIdentifier odinDeclaredIdentifier : o.getDeclaredIdentifierList()) {
            OdinSymbol odinSymbol = new OdinSymbol(odinDeclaredIdentifier, OdinSymbol.OdinVisibility.NONE);
            odinSymbol.setSymbolType(OdinSymbolType.FIELD);
            odinSymbol.setPsiType(type);
            odinSymbol.setScope(OdinSymbol.OdinScope.TYPE);
            odinSymbol.setImplicitlyDeclared(false);
            odinSymbol.setHasUsing(hasUsing);
//            odinSymbol.setPackagePath();
            symbols.add(odinSymbol);
        }
        if (o.getDeclaredIdentifierList().size() == 1 && hasUsing) {
            OdinInsightUtils.getSymbolsOfFieldWithUsing(symbolTable, o, symbols);
        }
    }

    public static @NotNull OdinSymbol.OdinVisibility getVisibility(@NotNull Collection<OdinAttributeStatement> attributeStatementList,
                                                                   OdinSymbol.OdinVisibility defaultVisibility) {
        OdinSymbol.OdinVisibility odinVisibility = OdinAttributeUtils.computeVisibility(attributeStatementList);
        return defaultVisibility == null ? odinVisibility : OdinSymbol.min(defaultVisibility, odinVisibility);
    }

}
