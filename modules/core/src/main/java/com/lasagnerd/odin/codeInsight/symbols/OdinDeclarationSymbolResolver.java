package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinAttributeUtils;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinEnumType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinPackageReferenceType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinTypeReference;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.lasagnerd.odin.codeInsight.OdinInsightUtils.getTypeElements;

public class OdinDeclarationSymbolResolver extends OdinVisitor {
    private static final Logger LOG = Logger.getInstance(OdinDeclarationSymbolResolver.class);
    List<OdinSymbol> symbols = new ArrayList<>();
    private final OdinVisibility defaultVisibility;
    private final OdinContext context;

    public OdinDeclarationSymbolResolver(OdinVisibility defaultVisibility, OdinContext context) {
        this.defaultVisibility = defaultVisibility;
        this.context = context;
    }

    public static List<OdinSymbol> getSymbols(OdinDeclaration odinDeclaration) {
        return getSymbols(OdinVisibility.PACKAGE_EXPORTED, odinDeclaration, new OdinContext());
    }

    public static List<OdinSymbol> getSymbols(OdinDeclaration odinDeclaration, OdinContext odinContext) {
        return getSymbols(OdinVisibility.NONE, odinDeclaration, odinContext);
    }

    public static List<OdinSymbol> getSymbols(@NotNull OdinVisibility defaultVisibility,
                                              OdinDeclaration odinDeclaration,
                                              OdinContext context) {
        OdinDeclarationSymbolResolver odinDeclarationSymbolResolver = new OdinDeclarationSymbolResolver(defaultVisibility, context);
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
            OdinSymbol symbol = new OdinSymbol(declaredIdentifier);
            symbol.setPsiType(type);
            symbol.setHasUsing(using);
            symbol.setScope(OdinScope.LOCAL);
            symbol.setVisibility(OdinVisibility.NONE);
            if (declaredIdentifier.getDollar() == null) {
                symbol.setSymbolType(OdinSymbolType.PARAMETER);
            } else {
                symbol.setSymbolType(OdinSymbolType.POLYMORPHIC_TYPE);
            }
            symbols.add(symbol);
        }

        if (using && o.getDeclaredIdentifiers().size() == 1) {
            List<OdinSymbol> typeElements;
            if (type != null) {
                typeElements = getTypeElements(context, type);
            } else {
                typeElements = getTypeElements(context, o.getExpression());
            }
            typeElements = setVisibleThroughUsing(typeElements);
            symbols.addAll(typeElements);
        }
    }

    private static List<OdinSymbol> setVisibleThroughUsing(List<OdinSymbol> symbols) {
        return symbols.stream().map(s -> s.withVisibleThroughUsing(true)).collect(Collectors.toList());
    }

    @Override
    public void visitParameterDeclarator(@NotNull OdinParameterDeclarator o) {
        OdinType psiType = o.getTypeDefinition();
        boolean hasUsing = false;
        for (OdinParameter odinParameter : o.getParameterList()) {
            OdinDeclaredIdentifier declaredIdentifier = odinParameter.getDeclaredIdentifier();
            OdinSymbol symbol = new OdinSymbol(declaredIdentifier);
            hasUsing |= odinParameter.getUsing() != null;

            symbol.setHasUsing(hasUsing);
            symbol.setPsiType(psiType);
            if (declaredIdentifier.getDollar() == null) {
                symbol.setSymbolType(OdinSymbolType.PARAMETER);
            } else {
                symbol.setSymbolType(OdinSymbolType.POLYMORPHIC_TYPE);
            }
            symbol.setScope(OdinScope.LOCAL);
            symbol.setVisibility(OdinVisibility.NONE);

            symbols.add(symbol);
        }

        if (hasUsing) {
            if (o.getParameterList().size() == 1) {
                if (psiType != null) {
                    List<OdinSymbol> typeElements = getTypeElements(context, psiType);
                    typeElements = setVisibleThroughUsing(typeElements);

                    symbols.addAll(typeElements);
                }
            }
        }
    }

    @Override
    public void visitVariableDeclarationStatement(@NotNull OdinVariableDeclarationStatement o) {
        boolean isLocal = OdinInsightUtils.isLocalVariable(o);

        boolean hasUsing = o.getUsing() != null;

        List<OdinAttributesDefinition> attributeList = o.getAttributesDefinitionList();
        OdinScope scope = getScope(o);
        OdinVisibility visibility = getVisibility(scope, attributeList);

        for (var declaredIdentifier : o.getDeclaredIdentifiers()) {
            OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier, visibility);
            if (isLocal) {
                odinSymbol.setScope(OdinScope.LOCAL);
            } else {
                odinSymbol.setScope(OdinScope.GLOBAL);
            }
            odinSymbol.setPsiType(o.getType());
            odinSymbol.setHasUsing(hasUsing);
            odinSymbol.setAttributes(attributeList);
            odinSymbol.setSymbolType(OdinSymbolType.VARIABLE);
            odinSymbol.setScope(scope);
            symbols.add(odinSymbol);
        }

        if (hasUsing) {
            if (o.getDeclaredIdentifiers().size() == 1) {
                List<OdinSymbol> typeElements = getTypeElements(context, o.getType());
                typeElements = setVisibleThroughUsing(typeElements);
                symbols.addAll(typeElements);
            }
        }

    }

    private @NotNull OdinVisibility getVisibility(OdinScope scope, List<OdinAttributesDefinition> attributeList) {
        if (scope == OdinScope.LOCAL)
            return OdinVisibility.NONE;
        return getVisibility(attributeList, this.defaultVisibility);
    }

    private static @NotNull OdinScope getScope(@NotNull OdinDeclaration o) {
        return OdinInsightUtils.isLocal(o) ? OdinScope.LOCAL : OdinScope.GLOBAL;
    }

    @Override
    public void visitVariableInitializationStatement(@NotNull OdinVariableInitializationStatement o) {
        boolean isLocal = OdinInsightUtils.isLocalVariable(o);

        boolean hasUsing = o.getUsing() != null;
        OdinType typeDefinition = o.getType();

        OdinScope scope = getScope(o);
        OdinVisibility visibility = getVisibility(scope, o.getAttributesDefinitionList());
        for (int i = 0; i < o.getDeclaredIdentifiers().size(); i++) {
            OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifiers().get(i), visibility);
            odinSymbol.setSymbolType(OdinSymbolType.VARIABLE);
            odinSymbol.setHasUsing(hasUsing);
            odinSymbol.setPsiType(typeDefinition);
            odinSymbol.setAttributes(o.getAttributesDefinitionList());
            odinSymbol.setScope(scope);
            if (isLocal) {
                odinSymbol.setScope(OdinScope.LOCAL);
            } else {
                odinSymbol.setScope(OdinScope.GLOBAL);
            }
            symbols.add(odinSymbol);
        }

        if (hasUsing) {
            if (o.getDeclaredIdentifiers().size() == 1 && Objects.requireNonNull(o.getRhsExpressions()).getExpressionList().size() == 1) {
                if (o.getType() != null) {
                    List<OdinSymbol> typeElements = getTypeElements(context, o.getType());
                    typeElements = setVisibleThroughUsing(typeElements);
                    symbols.addAll(typeElements);
                } else {
                    OdinExpression odinExpression = o.getRhsExpressions().getExpressionList().getFirst();
                    List<OdinSymbol> typeElements = getTypeElements(context, odinExpression);
                    typeElements = setVisibleThroughUsing(typeElements);
                    symbols.addAll(typeElements);
                }
            }
        }
    }


    @Override
    public void visitImportDeclaration(@NotNull OdinImportDeclaration o) {
        var alias = o.getAlias();
        OdinSymbol odinSymbol = new OdinSymbol(Objects.requireNonNullElse(alias, o));
        odinSymbol.setSymbolType(OdinSymbolType.PACKAGE_REFERENCE);
        odinSymbol.setVisibility(OdinVisibility.FILE_PRIVATE);

        symbols.add(odinSymbol);
    }


    @Override
    public void visitUsingStatement(@NotNull OdinUsingStatement o) {
        for (OdinExpression expression : o.getExpressionList()) {

            List<OdinSymbol> typeSymbols;
            TsOdinType tsOdinType = expression.getInferredType(context);
            if (tsOdinType instanceof TsOdinTypeReference tsOdinTypeReference) {
                typeSymbols = getTypeElements(context, OdinTypeResolver.resolveTypeReference(context, tsOdinTypeReference)
                        .baseType(true));
            } else {
                var stream = getTypeElements(context, tsOdinType.baseType(true)).stream();
                if (tsOdinType.baseType(true) instanceof TsOdinPackageReferenceType)
                    stream = stream
                            .filter(s -> s.getVisibility() == OdinVisibility.PACKAGE_EXPORTED);
                typeSymbols = stream.toList();
            }
            typeSymbols = setVisibleThroughUsing(typeSymbols);
            symbols.addAll(typeSymbols);
        }
    }

    @Override
    public void visitForInParameterDeclaration(@NotNull OdinForInParameterDeclaration o) {
        for (OdinForInParameterDeclarator odinForInParameterDeclarator : o.getForInParameterDeclaratorList()) {
            PsiNamedElement declaredIdentifier = odinForInParameterDeclarator.getDeclaredIdentifier();
            OdinSymbol odinSymbol = new OdinSymbol(declaredIdentifier, OdinVisibility.NONE);
            odinSymbol.setSymbolType(OdinSymbolType.VARIABLE);
            odinSymbol.setScope(OdinScope.LOCAL);
            symbols.add(odinSymbol);
        }
    }

    @Override
    public void visitSwitchTypeVariableDeclaration(@NotNull OdinSwitchTypeVariableDeclaration o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), OdinVisibility.NONE);
        odinSymbol.setSymbolType(OdinSymbolType.VARIABLE);
        odinSymbol.setScope(OdinScope.LOCAL);

        symbols.add(odinSymbol);
    }

    @Override
    public void visitPolymorphicType(@NotNull OdinPolymorphicType o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), OdinVisibility.NONE);
        odinSymbol.setSymbolType(OdinSymbolType.POLYMORPHIC_TYPE);
        odinSymbol.setScope(OdinScope.LOCAL);
        symbols.add(odinSymbol);
    }


    @Override
    public void visitConstantInitializationStatement(@NotNull OdinConstantInitializationStatement o) {
        OdinType declaredType = OdinInsightUtils.getDeclaredType(o);
        OdinSymbolType symbolType = switch (declaredType) {
            case OdinProcedureType ignored -> OdinSymbolType.PROCEDURE;
            case OdinStructType ignored -> OdinSymbolType.STRUCT;
            case OdinBitFieldType ignored -> OdinSymbolType.BIT_FIELD;
            case OdinProcedureGroupType ignored -> OdinSymbolType.PROCEDURE_OVERLOAD;
            case OdinEnumType ignored -> OdinSymbolType.ENUM;
            case OdinBitSetType ignored -> OdinSymbolType.BIT_SET;
            case OdinUnionType ignored -> OdinSymbolType.UNION;
            case OdinProcedureLiteralType ignored -> OdinSymbolType.PROCEDURE;
            case null, default -> OdinSymbolType.CONSTANT;
        };

        OdinType typeDefinition = o.getType();
        OdinScope scope = getScope(o);
        List<OdinAttributesDefinition> attributeList = o.getAttributesDefinitionList();

        OdinVisibility visibility = getVisibility(scope, attributeList);
        for (int i = 0; i < o.getDeclaredIdentifiers().size(); i++) {
            OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifiers().get(i), visibility);

            if (o.getUsing() != null) {
                odinSymbol.setHasUsing(true);
                if (declaredType instanceof OdinEnumType enumType) {
                    List<OdinSymbol> enumFields = OdinInsightUtils.getEnumFields(enumType);
                    symbols.addAll(enumFields);
                } else {
                    OdinExpression expression = o.getExpressionList().getFirst();
                    TsOdinType tsOdinType = expression.getInferredType(context);
                    if (tsOdinType instanceof TsOdinTypeReference typeReference
                            && typeReference.referencedType().baseType(true) instanceof TsOdinEnumType enumType) {
                        List<OdinSymbol> enumFields = OdinInsightUtils.getEnumFields((OdinEnumType) enumType.getPsiType());
                        symbols.addAll(enumFields);
                    }
                }
            }
            odinSymbol.setPsiType(typeDefinition);
            odinSymbol.setSymbolType(symbolType);
            odinSymbol.setAttributes(attributeList);
            odinSymbol.setScope(scope);
            symbols.add(odinSymbol);
        }

    }


    @Override
    public void visitLabelDeclaration(@NotNull OdinLabelDeclaration o) {
        OdinSymbol odinSymbol = new OdinSymbol(o.getDeclaredIdentifier(), OdinVisibility.NONE);
        odinSymbol.setSymbolType(OdinSymbolType.LABEL);
        odinSymbol.setScope(OdinScope.LOCAL);
        symbols.add(odinSymbol);
    }

    @Override
    public void visitForeignImportDeclarationStatement(@NotNull OdinForeignImportDeclarationStatement o) {
        OdinScope scope = getScope(o);

        OdinSymbol odinSymbol = new OdinSymbol();
        if (o.getAlias() != null) {
            odinSymbol.setDeclaredIdentifier(o.getAlias());
        } else {
            odinSymbol.setDeclaredIdentifier(o);
        }
        odinSymbol.setName(o.getName());
        odinSymbol.setSymbolType(OdinSymbolType.FOREIGN_IMPORT);
        odinSymbol.setScope(scope);
        odinSymbol.setVisibility(OdinVisibility.NONE);
        odinSymbol.setAttributes(o.getAttributesDefinitionList());
        symbols.add(odinSymbol);

    }

    @Override
    public void visitEnumValueDeclaration(@NotNull OdinEnumValueDeclaration o) {
        OdinSymbol odinSymbol = new OdinSymbol();
        odinSymbol.setName(o.getDeclaredIdentifier().getName());
        odinSymbol.setDeclaredIdentifier(o.getDeclaredIdentifier());
        odinSymbol.setImplicitlyDeclared(false);
        odinSymbol.setScope(OdinScope.TYPE);
        odinSymbol.setSymbolType(OdinSymbolType.ENUM_FIELD);
        odinSymbol.setVisibility(OdinVisibility.NONE);
        OdinEnumType enumType = PsiTreeUtil.getParentOfType(o, OdinEnumType.class);
        odinSymbol.setPsiType(enumType);
        symbols.add(odinSymbol);
    }

    @Override
    public void visitFieldDeclarationStatement(@NotNull OdinFieldDeclarationStatement o) {
        OdinType type = o.getType();
        boolean hasUsing = o.getUsing() != null;
        for (OdinDeclaredIdentifier odinDeclaredIdentifier : o.getDeclaredIdentifierList()) {
            OdinSymbol odinSymbol = new OdinSymbol(odinDeclaredIdentifier, OdinVisibility.NONE);
            odinSymbol.setSymbolType(OdinSymbolType.STRUCT_FIELD);
            odinSymbol.setPsiType(type);
            odinSymbol.setScope(OdinScope.TYPE);
            odinSymbol.setImplicitlyDeclared(false);
            odinSymbol.setHasUsing(hasUsing);
            symbols.add(odinSymbol);
        }
        if (o.getDeclaredIdentifierList().size() == 1 && hasUsing) {
            List<OdinSymbol> symbolsVisibleThroughUsing = new ArrayList<>();
            OdinInsightUtils.getSymbolsOfFieldWithUsing(context, o, symbolsVisibleThroughUsing);
            symbolsVisibleThroughUsing = setVisibleThroughUsing(symbolsVisibleThroughUsing);
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
                OdinDeclarationSymbolResolver declarationSymbolResolver = new OdinDeclarationSymbolResolver(defaultVisibility, context);
                odinStatement.accept(declarationSymbolResolver);
                declarationSymbolResolver.symbols.forEach(s -> {
                    s.setForeign(true);
                    this.symbols.add(s);
                });
            }
        }
    }

    @Override
    public void visitWhenStatement(@NotNull OdinWhenStatement o) {
        OdinWhenBlock whenBlock = o.getWhenBlock();
        addWhenBlockDeclarations(whenBlock);
    }

    private void addWhenBlockDeclarations(OdinWhenBlock whenBlock) {
        boolean ifConditionTrue = true;
        boolean ignoreCondition = true;

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

    public static @NotNull OdinVisibility getVisibility(@NotNull Collection<OdinAttributesDefinition> attributes,
                                                        OdinVisibility defaultVisibility) {
        OdinVisibility odinVisibility = OdinAttributeUtils.computeVisibility(attributes);
        return defaultVisibility == null ? odinVisibility : OdinVisibility.min(defaultVisibility, odinVisibility);
    }

}
