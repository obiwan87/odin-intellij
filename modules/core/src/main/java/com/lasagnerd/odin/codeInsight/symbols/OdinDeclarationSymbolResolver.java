package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinAttributeUtils;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.evaluation.OdinExpressionEvaluator;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInTypes;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinEnumType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static List<OdinSymbol> getSymbols(OdinDeclaration odinDeclaration) {
        return getSymbols(odinDeclaration, OdinSymbolTable.EMPTY);
    }

    public static List<OdinSymbol> getSymbols(OdinDeclaration odinDeclaration, OdinSymbolTable odinSymbolTable) {
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

        for (var declaredIdentifier : o.getDeclaredIdentifiers()) {
            OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier);
            odinSymbol.setPsiType(type);
            odinSymbol.setHasUsing(using);
            odinSymbol.setSymbolType(OdinSymbolType.PARAMETER);
            symbols.add(odinSymbol);
        }

        if (using && o.getDeclaredIdentifiers().size() == 1) {
            List<OdinSymbol> typeElements;
            if (type != null) {
                typeElements = OdinInsightUtils.getTypeElements(type, symbolTable);
            } else {
                typeElements = OdinInsightUtils.getTypeElements(o.getExpression(), symbolTable);
            }
            setVisibleThroughUsing(typeElements);
            symbols.addAll(typeElements);
        }
    }

    private static void setVisibleThroughUsing(List<OdinSymbol> symbols) {
        symbols.forEach(
                s -> s.setVisibleThroughUsing(true)
        );
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
                    List<OdinSymbol> typeElements = OdinInsightUtils.getTypeElements(psiType, symbolTable);
                    setVisibleThroughUsing(typeElements);

                    symbols.addAll(typeElements);
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
            odinSymbol.setAttributes(o.getAttributeList());
            odinSymbol.setSymbolType(OdinSymbolType.VARIABLE);
            symbols.add(odinSymbol);
        }

        if (hasUsing) {
            if (o.getDeclaredIdentifiers().size() == 1) {
                List<OdinSymbol> typeElements = OdinInsightUtils.getTypeElements(o.getType(), symbolTable);
                setVisibleThroughUsing(typeElements);
                symbols.addAll(typeElements);
            }
        }

    }

    @Override
    public void visitVariableInitializationStatement(@NotNull OdinVariableInitializationStatement o) {
        boolean isLocal = OdinInsightUtils.isLocalVariable(o);

        boolean hasUsing = o.getUsing() != null;
        OdinType typeDefinition = o.getType();
        for (int i = 0; i < o.getDeclaredIdentifiers().size(); i++) {
            OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifiers().get(i));
            odinSymbol.setSymbolType(OdinSymbolType.VARIABLE);
            odinSymbol.setHasUsing(hasUsing);
            odinSymbol.setPsiType(typeDefinition);

            if (isLocal) {
                odinSymbol.setScope(OdinSymbol.OdinScope.LOCAL);
            } else {
                odinSymbol.setScope(OdinSymbol.OdinScope.GLOBAL);
            }
            symbols.add(odinSymbol);
        }

        if (hasUsing) {
            if (o.getDeclaredIdentifiers().size() == 1 && Objects.requireNonNull(o.getRhsExpressions()).getExpressionList().size() == 1) {
                if (o.getType() != null) {
                    List<OdinSymbol> typeElements = OdinInsightUtils.getTypeElements(o.getType(), symbolTable);
                    setVisibleThroughUsing(typeElements);
                    symbols.addAll(typeElements);
                } else {
                    OdinExpression odinExpression = o.getRhsExpressions().getExpressionList().getFirst();
                    List<OdinSymbol> typeElements = OdinInsightUtils.getTypeElements(odinExpression, symbolTable);
                    setVisibleThroughUsing(typeElements);
                    symbols.addAll(typeElements);
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
    public void visitUsingStatement(@NotNull OdinUsingStatement o) {
        for (OdinExpression expression : o.getExpressionList()) {
            List<OdinSymbol> typeSymbols = OdinInsightUtils.getTypeElements(expression, this.symbolTable);
            setVisibleThroughUsing(typeSymbols);
            symbols.addAll(typeSymbols);
        }
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
    public void visitConstantInitializationStatement(@NotNull OdinConstantInitializationStatement o) {
        OdinType declaredType = OdinInsightUtils.getDeclaredType(o);
        OdinSymbolType symbolType = switch (declaredType) {
            case OdinProcedureType ignored -> OdinSymbolType.PROCEDURE;
            case OdinStructType ignored -> OdinSymbolType.STRUCT;
            case OdinBitFieldType ignored -> OdinSymbolType.BIT_FIELD;
            case OdinProcedureOverloadType ignored -> OdinSymbolType.PROCEDURE_OVERLOAD;
            case OdinEnumType ignored -> OdinSymbolType.ENUM;
            case OdinBitSetType ignored -> OdinSymbolType.BIT_SET;
            case OdinUnionType ignored -> OdinSymbolType.UNION;
            case OdinProcedureLiteralType ignored -> OdinSymbolType.PROCEDURE;
            case null, default -> OdinSymbolType.CONSTANT;
        };

        OdinType typeDefinition = o.getType();
        for (int i = 0; i < o.getDeclaredIdentifiers().size(); i++) {
            OdinSymbol odinSymbol = new OdinSymbol(
                    o.getDeclaredIdentifiers().get(i),
                    getVisibility(o.getAttributeList(), defaultVisibility));

            if (o.getUsing() != null) {
                odinSymbol.setHasUsing(true);
                if (declaredType instanceof OdinEnumType enumType) {
                    List<OdinSymbol> enumFields = OdinInsightUtils.getEnumFields(enumType);
                    symbols.addAll(enumFields);
                } else {
                    TsOdinType tsOdinType = OdinInferenceEngine.inferType(symbolTable, o.getExpressionList().getFirst());
                    if (tsOdinType instanceof TsOdinMetaType metaType
                            && metaType.representedType().baseType(true) instanceof TsOdinEnumType enumType) {
                        List<OdinSymbol> enumFields = OdinInsightUtils.getEnumFields((OdinEnumType) enumType.getPsiType());
                        symbols.addAll(enumFields);
                    }
                }
            }
            odinSymbol.setPsiType(typeDefinition);
            odinSymbol.setSymbolType(symbolType);
            odinSymbol.setAttributes(o.getAttributeList());
            symbols.add(odinSymbol);
        }

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
            odinSymbol.setAttributes(o.getAttributeList());
            symbols.add(odinSymbol);
        }
    }

    @Override
    public void visitEnumValueDeclaration(@NotNull OdinEnumValueDeclaration o) {
        OdinSymbol odinSymbol = new OdinSymbol();
        odinSymbol.setName(o.getDeclaredIdentifier().getName());
        odinSymbol.setDeclaredIdentifier(o.getDeclaredIdentifier());
        odinSymbol.setImplicitlyDeclared(false);
        odinSymbol.setScope(OdinSymbol.OdinScope.TYPE);
        odinSymbol.setSymbolType(OdinSymbolType.ENUM_FIELD);
        OdinEnumType enumType = PsiTreeUtil.getParentOfType(o, OdinEnumType.class);
        odinSymbol.setPsiType(enumType);
        symbols.add(odinSymbol);
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
            symbols.add(odinSymbol);
        }
        if (o.getDeclaredIdentifierList().size() == 1 && hasUsing) {
            ArrayList<OdinSymbol> symbolsVisibleThroughUsing = new ArrayList<>();
            OdinInsightUtils.getSymbolsOfFieldWithUsing(symbolTable, o, symbolsVisibleThroughUsing);
            setVisibleThroughUsing(symbolsVisibleThroughUsing);
            symbols.addAll(symbolsVisibleThroughUsing);
        }
    }

    @Override
    public void visitBitFieldFieldDeclaration(@NotNull OdinBitFieldFieldDeclaration o) {
        symbols.add(OdinInsightUtils.createBitFieldSymbol(o));
    }

    @Override
    public void visitForeignStatement(@NotNull OdinForeignStatement o) {
        OdinForeignStatementList foreignStatementList = o.getForeignBlock().getForeignStatementList();
        if (foreignStatementList != null) {
            for (OdinStatement odinStatement : foreignStatementList.getStatementList()) {
                odinStatement.accept(this);
            }
        }
    }

    @Override
    public void visitWhenStatement(@NotNull OdinWhenStatement o) {
        OdinWhenBlock whenBlock = o.getWhenBlock();
        addWhenBlockDeclarations(whenBlock);
    }

    private void addWhenBlockDeclarations(OdinWhenBlock whenBlock) {
        OdinCondition condition = whenBlock.getCondition();

        EvOdinValue conditionValue = TsOdinBuiltInTypes.NULL;
        if (condition != null) {
            conditionValue = OdinExpressionEvaluator.evaluate(symbolTable, condition.getExpression());
        }

        boolean ifConditionTrue = conditionValue.asBool() == Boolean.TRUE;
        boolean ignoreCondition = conditionValue.isNull();

//        boolean ifConditionTrue = true;
//        boolean ignoreCondition = true;

        OdinStatementBody statementBody = whenBlock.getStatementBody();

        if (statementBody != null && (ifConditionTrue || ignoreCondition)) {
            addStatementBodySymbols(statementBody);
        }

        OdinElseWhenBlock elseWhenBlock = whenBlock.getElseWhenBlock();
        if (elseWhenBlock != null && (!ifConditionTrue || ignoreCondition)) {
            OdinWhenBlock nextWhenBlock = elseWhenBlock.getWhenBlock();
            if (nextWhenBlock != null) {
                addWhenBlockDeclarations(nextWhenBlock);
            } else if (elseWhenBlock.getStatementBody() != null) {
                addStatementBodySymbols(elseWhenBlock.getStatementBody());
            }
        }
    }

    private void addStatementBodySymbols(OdinStatementBody statementBody) {
        OdinDoStatement doStatement = statementBody.getDoStatement();
        @Nullable OdinBlock block = statementBody.getBlock();

        if (doStatement != null) {
            doStatement.accept(this);
        } else if (block != null) {
            for (OdinStatement statement : block.getStatements()) {
                statement.accept(this);
            }
        }
    }

    public static @NotNull OdinSymbol.OdinVisibility getVisibility(@NotNull Collection<OdinAttribute> attributeStatementList,
                                                                   OdinSymbol.OdinVisibility defaultVisibility) {
        OdinSymbol.OdinVisibility odinVisibility = OdinAttributeUtils.computeVisibility(attributeStatementList);
        return defaultVisibility == null ? odinVisibility : OdinSymbol.min(defaultVisibility, odinVisibility);
    }

}
