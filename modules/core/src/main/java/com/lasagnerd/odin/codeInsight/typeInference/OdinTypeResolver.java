package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.evaluation.OdinExpressionEvaluator;
import com.lasagnerd.odin.codeInsight.symbols.OdinDeclarationSymbolResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableResolver;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine.doInferType;
import static com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine.inferType;
import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInTypes.RESERVED_TYPES;

@EqualsAndHashCode(callSuper = true)
public class OdinTypeResolver extends OdinVisitor {

    public static @NotNull TsOdinType resolveType(OdinSymbolTable symbolTable, OdinSymbol symbol) {
        OdinType type = symbol.getPsiType();
        OdinDeclaration declaration = symbol.getDeclaration();
        OdinDeclaredIdentifier declaredIdentifier;
        if (symbol.getDeclaredIdentifier() instanceof OdinDeclaredIdentifier) {
            declaredIdentifier = (OdinDeclaredIdentifier) symbol.getDeclaredIdentifier();
        } else {
            declaredIdentifier = null;
        }

        return resolveType(0, symbolTable, declaredIdentifier, declaration, type);
    }

    public static @NotNull TsOdinType resolveType(OdinSymbolTable symbolTable, @NotNull OdinType type) {
        return resolveType(0, symbolTable, null, null, type);
    }

    public static @NotNull TsOdinType resolveType(OdinSymbolTable symbolTable,
                                                  OdinDeclaredIdentifier declaredIdentifier,
                                                  OdinDeclaration declaration,
                                                  @NotNull OdinType type) {
        return resolveType(0, symbolTable, declaredIdentifier, declaration, type);
    }

    public static @NotNull TsOdinType resolveType(int level,
                                                  OdinSymbolTable symbolTable,
                                                  OdinDeclaredIdentifier declaredIdentifier,
                                                  OdinDeclaration declaration,
                                                  OdinType type) {
        if(type == null)
            return TsOdinBuiltInTypes.UNKNOWN;

        OdinTypeResolver typeResolver = new OdinTypeResolver(level, symbolTable, declaration, declaredIdentifier);
        type.accept(typeResolver);
        return Objects.requireNonNullElse(typeResolver.type, TsOdinBuiltInTypes.UNKNOWN);
    }

    public static @NotNull TsOdinMetaType findMetaType(OdinSymbolTable symbolTable,
                                                       OdinTypeDefinitionExpression expression,
                                                       @NotNull OdinType type) {
        return findMetaType(symbolTable, null, null, expression, type);
    }

    public static @NotNull TsOdinMetaType findMetaType(OdinSymbolTable symbolTable,
                                                       OdinDeclaredIdentifier declaredIdentifier,
                                                       OdinDeclaration declaration,
                                                       OdinExpression firstExpression,
                                                       @NotNull OdinType type) {

        TsOdinType tsOdinType = resolveType(symbolTable, declaredIdentifier, declaration, type);

        return createMetaType(tsOdinType, firstExpression);
    }

    public static @NotNull TsOdinMetaType createMetaType(TsOdinType tsOdinType, OdinExpression firstExpression) {
        TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(tsOdinType.getMetaType());
        tsOdinMetaType.setName(tsOdinType.getName());
        tsOdinMetaType.setDeclaredIdentifier(tsOdinMetaType.getDeclaredIdentifier());
        tsOdinMetaType.setDeclaration(tsOdinType.getDeclaration());
        tsOdinMetaType.setSymbolTable(tsOdinType.getSymbolTable());
        tsOdinMetaType.setRepresentedType(tsOdinType);
        tsOdinMetaType.setPsiType(tsOdinType.getPsiType());
        tsOdinMetaType.setDistinct(OdinInsightUtils.isDistinct(firstExpression));
        return tsOdinMetaType;
    }

    public static @NotNull TsOdinType resolveMetaType(OdinSymbolTable symbolTable, TsOdinMetaType metaType) {
        return resolveMetaType(0, symbolTable, metaType);
    }

    public static TsOdinType resolveMetaType(int level, OdinSymbolTable symbolTable, TsOdinMetaType metaType) {
        if (metaType.getRepresentedType() instanceof TsOdinBuiltInType) {
            return TsOdinBuiltInTypes.getBuiltInType(metaType.getName());
        } else {
            TsOdinType tsOdinType;
            if (metaType.getRepresentedType() != null) {
                return metaType.getRepresentedType();
            } else if (metaType.getPsiType() != null) {
                OdinDeclaredIdentifier declaredIdentifier = metaType.getDeclaredIdentifier();
                tsOdinType = resolveType(level,
                        symbolTable,
                        declaredIdentifier,
                        metaType.getDeclaration(),
                        metaType.getPsiType());
                tsOdinType.setDeclaration(metaType.getDeclaration());
                tsOdinType.setDeclaredIdentifier(declaredIdentifier);
                symbolTable.addKnownType(declaredIdentifier, tsOdinType);
                if (declaredIdentifier != null) {
                    tsOdinType.setName(declaredIdentifier.getName());
                }
                tsOdinType.getSymbolTable().putAll(symbolTable);
                metaType.setRepresentedType(tsOdinType);
                return tsOdinType;
            } else if (metaType.getRepresentedMetaType() == TsOdinMetaType.MetaType.ALIAS) {

                TsOdinTypeAlias typeAlias = new TsOdinTypeAlias();
                metaType.setRepresentedType(typeAlias);

                if (metaType.getTypeExpression() instanceof OdinTypeDefinitionExpression typeDefinitionExpression) {
                    typeAlias.setDistinct(typeDefinitionExpression.getDistinct() != null);
                    typeAlias.setPsiType(typeDefinitionExpression.getType());
                } else {
                    typeAlias.setPsiTypeExpression(metaType.getTypeExpression());
                }

                typeAlias.setDeclaration(metaType.getDeclaration());
                typeAlias.setName(metaType.getName());
                typeAlias.setSymbolTable(metaType.getSymbolTable());

                TsOdinType aliasedType = resolveMetaType(level + 1,
                        metaType.getAliasedMetaType().getSymbolTable(),
                        metaType.getAliasedMetaType());
                typeAlias.setAliasedType(aliasedType);

                return typeAlias;
            }
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    // Result
    TsOdinType type;

    private final int level;
    private final OdinSymbolTable symbolTable;
    private final OdinDeclaration typeDeclaration;
    private final OdinDeclaredIdentifier typeDeclaredIdentifier;

    // avoid stackoverflow when encountering circular references
    private final Set<OdinDeclaredIdentifier> visitedDeclaredIdentifiers = new HashSet<>();


    public OdinTypeResolver(int level, OdinSymbolTable symbolTable, OdinDeclaration typeDeclaration, OdinDeclaredIdentifier typeDeclaredIdentifier) {
        this.level = level;
        this.symbolTable = symbolTable;
        this.typeDeclaration = typeDeclaration;
        this.typeDeclaredIdentifier = typeDeclaredIdentifier;
    }

    // resolve type calls
    public @NotNull TsOdinType doResolveType(OdinSymbolTable symbolTable,
                                             OdinDeclaredIdentifier declaredIdentifier,
                                             OdinDeclaration declaration,
                                             @NotNull OdinType type) {
        return resolveType(level + 1, symbolTable, declaredIdentifier, declaration, type);
    }

    public @NotNull TsOdinType doResolveType(OdinSymbolTable symbolTable,
                                             @NotNull OdinType type) {
        return resolveType(level + 1, symbolTable, null, null, type);
    }

    public @NotNull TsOdinType doResolveMetaType(OdinSymbolTable symbolTable, TsOdinMetaType metaType) {
        return resolveMetaType(level + 1, symbolTable, metaType);
    }

    public @NotNull TsOdinType doResolveType(OdinSymbolTable symbolTable, OdinExpression odinExpression) {
        TsOdinType tsOdinType = doInferType(symbolTable, odinExpression);
        if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
            return doResolveMetaType(symbolTable, tsOdinMetaType);
        }
        return tsOdinType;
    }

    // logging
    @SuppressWarnings("unused")
    public void log(String message) {
//        System.out.println("\t".repeat(level) + message);
    }

    private List<TsOdinParameter> createParameters(TsOdinType baseType, List<OdinParamEntry> paramEntries) {
        OdinSymbolTable localSymbolTable = baseType.getSymbolTable();
        List<TsOdinParameter> typeParameters = new ArrayList<>();
        int k = 0;
        for (var paramEntry : paramEntries) {
            OdinParameterDeclaration parameterDeclaration = paramEntry.getParameterDeclaration();

            // First, add all $Identifier expressions we encounter in this parameter to the current scope

            // Value polymorphism
            for (OdinPolymorphicType odinPolymorphicType : PsiTreeUtil.findChildrenOfType(paramEntry, OdinPolymorphicType.class)) {
                TsOdinType tsOdinType = doResolveType(localSymbolTable, odinPolymorphicType);
                localSymbolTable.addType(tsOdinType.getName(), tsOdinType);
                localSymbolTable.add(odinPolymorphicType.getDeclaredIdentifier());
                if (baseType instanceof TsOdinGenericType tsOdinGenericType) {
                    tsOdinGenericType.getPolymorphicParameters().put(tsOdinType.getName(), tsOdinType);
                }
            }

            // Type polymorphism
            // TODO Get rid of dollar in OdinDeclaredIdentifier
            for (OdinDeclaredIdentifier declaredIdentifier : PsiTreeUtil.findChildrenOfType(paramEntry, OdinDeclaredIdentifier.class)) {
                if (declaredIdentifier.getDollar() != null) {
                    TsOdinPolymorphicType valuePolymorphicType = new TsOdinPolymorphicType();
                    String name = declaredIdentifier.getName();
                    valuePolymorphicType.setName(name);
                    valuePolymorphicType.setDeclaredIdentifier(declaredIdentifier);
                    localSymbolTable.addType(valuePolymorphicType.getName(), valuePolymorphicType);
                    localSymbolTable.add(declaredIdentifier);
                    if (baseType instanceof TsOdinGenericType tsOdinGenericType) {
                        tsOdinGenericType.getPolymorphicParameters().put(valuePolymorphicType.getName(), valuePolymorphicType);
                    }
                }
            }

            List<TsOdinParameter> parameters = createParameters(paramEntry, parameterDeclaration, k);
            for (var tsOdinParameter : parameters) {
                if (tsOdinParameter.getPsiType() != null) {
                    TsOdinType tsOdinType = doResolveType(localSymbolTable, tsOdinParameter.getPsiType());
                    tsOdinParameter.setType(tsOdinType);
                } else if (tsOdinParameter.getDefaultValueExpression() != null) {
                    OdinSymbolTable paramSymbolTable = OdinSymbolTableResolver.computeSymbolTable(tsOdinParameter.getDefaultValueExpression());
                    TsOdinType tsOdinType = inferType(paramSymbolTable, tsOdinParameter.getDefaultValueExpression());
                    tsOdinParameter.setType(tsOdinType);
                    tsOdinParameter.setPsiType(tsOdinType.getPsiType());
                }
                typeParameters.add(tsOdinParameter);
            }

            k += parameters.size();
        }

        return typeParameters;
    }

    private void initializeNamedType(TsOdinType tsOdinType) {

        tsOdinType.setDeclaredIdentifier(typeDeclaredIdentifier);
        String name = typeDeclaredIdentifier != null ? typeDeclaredIdentifier.getName() : null;
        tsOdinType.setName(name);
        tsOdinType.setDeclaration(typeDeclaration);
        if (typeDeclaredIdentifier != null) {
            this.symbolTable.addKnownType(typeDeclaredIdentifier, tsOdinType);
        }
        tsOdinType.getSymbolTable().putAll(symbolTable);
        tsOdinType.getSymbolTable().setPackagePath(symbolTable.getPackagePath());
        log("Initialized " + tsOdinType.getClass().getSimpleName() + " with name " + name);
    }

    private TsOdinType resolveIdentifier(OdinIdentifier typeIdentifier, OdinSymbolTable symbolTable) {
        PsiNamedElement declaration;
        String identifierText = typeIdentifier.getText();

        TsOdinType scopeType = symbolTable.getType(typeIdentifier.getIdentifierToken().getText());
        if (scopeType != null) {
            return scopeType;
        } else {
            declaration = symbolTable.getNamedElement(typeIdentifier.getIdentifierToken().getText());
            if (!(declaration instanceof OdinDeclaredIdentifier declaredIdentifier)) {
                if (RESERVED_TYPES.contains(identifierText)) {
                    return TsOdinBuiltInTypes.getBuiltInType(identifierText);
                }
                return TsOdinBuiltInTypes.UNKNOWN;
            } else {
                var knownType = symbolTable.getKnownTypes().get(declaredIdentifier);
                if (knownType != null) {
                    log("Cache hit for type: " + knownType.getLabel());
                    return knownType;
                } else {
                    return resolveTypeFromDeclaredIdentifier(symbolTable, declaredIdentifier);
                }
            }
        }
    }

    public static TsOdinType resolveType(OdinSymbolTable symbolTable, OdinDeclaredIdentifier identifier) {
        OdinDeclaration declaration = PsiTreeUtil.getParentOfType(identifier, OdinDeclaration.class);
        OdinTypeResolver typeResolver = new OdinTypeResolver(0, symbolTable, declaration, identifier);
        return typeResolver.resolveTypeFromDeclaredIdentifier(symbolTable, identifier);
    }

    @Override
    public void visitProcedureLiteralType(@NotNull OdinProcedureLiteralType o) {
        this.type = resolveType(symbolTable, o.getProcedureDefinition().getProcedureSignature().getProcedureType());
    }

    private TsOdinType resolveTypeFromDeclaredIdentifier(OdinSymbolTable symbolTable, OdinDeclaredIdentifier identifier) {
        OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(identifier,
                false,
                OdinDeclaration.class);

        // TODO: do we need to recompute the symbol table for each declaration type?
        OdinSymbolTable typeSymbolTable;
        if (odinDeclaration != null) {
            typeSymbolTable = OdinSymbolTableResolver.computeSymbolTable(odinDeclaration);
            typeSymbolTable.getKnownTypes().putAll(symbolTable.getKnownTypes());
            typeSymbolTable.getSpecializedTypes().putAll(symbolTable.getSpecializedTypes());
        } else {
            typeSymbolTable = OdinSymbolTable.EMPTY;
        }
        switch (odinDeclaration) {
            case OdinConstantInitializationStatement constantInitializationStatement -> {
                OdinExpression firstExpression = constantInitializationStatement.getExpressionList().getFirst();
                OdinType declaredType = OdinInsightUtils.getDeclaredType(constantInitializationStatement);
                if ((
                        declaredType instanceof OdinStructType
                                || declaredType instanceof OdinBitFieldType
                                || declaredType instanceof OdinUnionType
                                || declaredType instanceof OdinProcedureOverloadType
                                || declaredType instanceof OdinProcedureType
                                || declaredType instanceof OdinProcedureLiteralType
                                || declaredType instanceof OdinEnumType
                )
                ) {
                    // check distinct
                    TsOdinType tsOdinType = resolveType(typeSymbolTable, identifier, odinDeclaration, declaredType);
                    tsOdinType.setDistinct(OdinInsightUtils.isDistinct(firstExpression));
                    return tsOdinType;
                }

                // Ref expression: it's a type alias
                List<OdinExpression> expressionList = constantInitializationStatement.getExpressionList();
                if (!expressionList.isEmpty()) {
                    int index = constantInitializationStatement.getDeclaredIdentifiers().indexOf(identifier);
                    if (index == -1) {
                        return TsOdinBuiltInTypes.UNKNOWN;
                    }
                    if (expressionList.size() <= index) {
                        return TsOdinBuiltInTypes.UNKNOWN;
                    }

                    OdinExpression odinExpression = expressionList.get(index);
                    // TODO this might light to a stackoverflow error because symbol table might contain the symbols
                    //  that will be needed by odinExpression as well
                    TsOdinTypeAlias typeAlias = new TsOdinTypeAlias();
                    typeAlias.setName(identifier.getText());
                    addKnownType(typeAlias, identifier, odinDeclaration, typeSymbolTable);
                    TsOdinType tsOdinType = doInferType(typeSymbolTable, odinExpression);
                    if (tsOdinType instanceof TsOdinMetaType metaType) {
                        TsOdinType resolvedMetaType = doResolveMetaType(metaType.getSymbolTable(), metaType);
                        typeAlias.setDistinct(OdinInsightUtils.isDistinct(odinExpression));
                        return createTypeAliasFromMetaType(typeAlias, identifier, resolvedMetaType, odinDeclaration, odinExpression);
                    }
                    return TsOdinBuiltInTypes.UNKNOWN;
                }
            }
            case OdinPolymorphicType polymorphicType -> {
                return doResolveType(symbolTable, identifier, odinDeclaration, polymorphicType);
            }
            case OdinParameterDeclarator odinParameterDeclarator -> {
                // Look for polymorphic type definitions like $C: typeid/...
                if (identifier.getDollar() != null) {
                    return doResolveType(symbolTable, odinParameterDeclarator.getTypeDefinition());
                }
            }
            case null, default -> {
            }
        }

        return TsOdinBuiltInTypes.UNKNOWN;
    }

    public static @NotNull TsOdinTypeAlias createTypeAliasFromMetaType(TsOdinTypeAlias typeAlias,
                                                                       OdinDeclaredIdentifier identifier,
                                                                       TsOdinType resolvedMetaType,
                                                                       OdinDeclaration odinDeclaration,
                                                                       OdinExpression odinExpression) {
        if(typeAlias != resolvedMetaType) {
            typeAlias.setAliasedType(resolvedMetaType);
        }
        typeAlias.setDeclaration(odinDeclaration);
        typeAlias.setDeclaredIdentifier(identifier);
        typeAlias.setName(identifier.getName());
        typeAlias.setPsiTypeExpression(resolvedMetaType.getPsiTypeExpression());

        if (odinExpression instanceof OdinTypeDefinitionExpression typeDefinitionExpression) {
            typeAlias.setDistinct(typeDefinitionExpression.getDistinct() != null);
            typeAlias.setPsiType(typeDefinitionExpression.getType());
        }
        typeAlias.setSymbolTable(resolvedMetaType.getSymbolTable());
        return typeAlias;
    }

    // Visitor methods
    @Override
    public void visitQualifiedType(@NotNull OdinQualifiedType qualifiedType) {
        OdinSymbolTable packageScope = symbolTable.getScopeOfImport(qualifiedType.getPackageIdentifier().getIdentifierToken().getText());
        OdinSimpleRefType simpleRefType = qualifiedType.getSimpleRefType();
        if (simpleRefType != null) {
            this.type = doResolveType(packageScope, simpleRefType);
        }
    }

    @Override
    public void visitSimpleRefType(@NotNull OdinSimpleRefType o) {
        OdinIdentifier identifier = o.getIdentifier();
        this.type = resolveIdentifier(identifier, symbolTable);
    }

    @Override
    public void visitBitFieldType(@NotNull OdinBitFieldType o) {
        TsOdinBitFieldType tsOdinBitFieldType = new TsOdinBitFieldType();
        tsOdinBitFieldType.setPsiType(o);
        initializeNamedType(tsOdinBitFieldType);
        this.type = tsOdinBitFieldType;
    }

    @Override
    public void visitCallType(@NotNull OdinCallType o) {
        OdinType type = o.getType();
        this.type = doResolveType(symbolTable, type);

        if (this.type instanceof TsOdinStructType structType) {
            // This should not be called again if type is already been visited
            // it might be a better idea to specialize the type at inference time and not at resolution time
            this.type = OdinTypeSpecializer.specializeStructOrGetCached(symbolTable, structType, o.getArgumentList());
        }

        if (this.type instanceof TsOdinUnionType unionType) {
            // This should not be called again if type is already been visited
            this.type = OdinTypeSpecializer.specializeUnionOrGetCached(symbolTable, unionType, o.getArgumentList());
        }
    }

    @Override
    public void visitMatrixType(@NotNull OdinMatrixType o) {
        if (o.getType() != null) {
            TsOdinMatrixType tsOdinMatrixType = new TsOdinMatrixType();
            TsOdinType elementType = doResolveType(symbolTable, o.getType());
            tsOdinMatrixType.setElementType(elementType);
            tsOdinMatrixType.setPsiType(o);
            tsOdinMatrixType.setSymbolTable(symbolTable);
            this.type = tsOdinMatrixType;
        }
    }

    @Override
    public void visitSliceType(@NotNull OdinSliceType o) {
        OdinType elementPsiType = o.getType();
        TsOdinSliceType tsOdinSliceType = new TsOdinSliceType();
        if (elementPsiType != null) {
            TsOdinType tsOdinElementType = OdinTypeResolver.resolveType(symbolTable, elementPsiType);
            tsOdinSliceType.setElementType(tsOdinElementType);
            tsOdinSliceType.setSymbolTable(symbolTable);
            tsOdinSliceType.setPsiType(o);
            tsOdinSliceType.setSoa(checkDirective(o.getDirectiveIdentifier(), "#soa"));

            this.type = tsOdinSliceType;
        }
    }

    private static boolean checkDirective(@Nullable OdinDirectiveIdentifier directiveHead, String hashtag) {
        boolean equals = false;
        if (directiveHead != null) {
            equals = directiveHead.getText().equals(hashtag);
        }
        return equals;
    }

    @Override
    public void visitDynamicArrayType(@NotNull OdinDynamicArrayType o) {
        OdinType elementPsiType = o.getType();
        TsOdinDynamicArray tsOdinDynamicArray = new TsOdinDynamicArray();
        if (elementPsiType != null) {
            TsOdinType tsOdinElementType = OdinTypeResolver.resolveType(symbolTable, elementPsiType);
            tsOdinDynamicArray.setElementType(tsOdinElementType);
            tsOdinDynamicArray.setSymbolTable(symbolTable);
            tsOdinDynamicArray.setPsiType(o);
            tsOdinDynamicArray.setSoa(checkDirective(o.getDirectiveIdentifier(), "#soa"));
            this.type = tsOdinDynamicArray;
        }
    }

    @Override
    public void visitMultiPointerType(@NotNull OdinMultiPointerType o) {
        TsOdinMultiPointerType tsOdinMultiPointerType = new TsOdinMultiPointerType();
        tsOdinMultiPointerType.setPsiType(o);
        TsOdinType dereferencedType = resolveType(symbolTable, Objects.requireNonNull(o.getType()));
        tsOdinMultiPointerType.setDereferencedType(dereferencedType);
        tsOdinMultiPointerType.setSymbolTable(symbolTable);
        this.type = tsOdinMultiPointerType;
    }

    @Override
    public void visitBitSetType(@NotNull OdinBitSetType o) {
        TsOdinBitSetType tsOdinBitSetType = new TsOdinBitSetType();
        OdinExpression elementTypeExpression = o.getExpression();
        TsOdinType tsOdinElementType = doResolveType(symbolTable, elementTypeExpression);
        tsOdinBitSetType.setElementType(tsOdinElementType);

        if (o.getType() != null) {
            TsOdinType tsBackingType = doResolveType(symbolTable, o.getType());
            tsOdinBitSetType.setBackingType(tsBackingType);
        }
        this.type = tsOdinBitSetType;

    }

    @Override
    public void visitUnionType(@NotNull OdinUnionType unionType) {
        TsOdinUnionType tsOdinUnionType = new TsOdinUnionType();
        tsOdinUnionType.setPsiType(unionType);
        initializeNamedType(tsOdinUnionType);

        List<TsOdinParameter> parameters = createParameters(tsOdinUnionType, unionType.getParamEntryList());
        tsOdinUnionType.setParameters(parameters);
        addKnownType(tsOdinUnionType, typeDeclaredIdentifier, typeDeclaration, tsOdinUnionType.getSymbolTable());
        OdinUnionBody unionBody = unionType.getUnionBlock().getUnionBody();
        if (unionBody != null) {
            List<OdinType> types = unionBody.getTypeList();
            for (OdinType type : types) {
                TsOdinType tsOdinType = doResolveType(tsOdinUnionType.getSymbolTable(), type);

                TsOdinUnionVariant tsOdinUnionVariant = new TsOdinUnionVariant();
                tsOdinUnionVariant.setPsiType(type);
                tsOdinUnionVariant.setType(tsOdinType);
                tsOdinUnionType.getVariants().add(tsOdinUnionVariant);
            }
        }
        this.type = tsOdinUnionType;
    }

    private static void addKnownType(TsOdinType tsOdinType,
                                     OdinDeclaredIdentifier declaredIdentifier,
                                     OdinDeclaration declaration,
                                     OdinSymbolTable symbolTable) {
        if (declaredIdentifier != null) {
            symbolTable.addKnownType(declaredIdentifier, tsOdinType);
            List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getLocalSymbols(declaration);
            symbolTable.add(localSymbols.getFirst());
        }
    }

    @Override
    public void visitArrayType(@NotNull OdinArrayType arrayType) {
        TsOdinArrayType tsOdinArrayType = new TsOdinArrayType();
        tsOdinArrayType.setSymbolTable(symbolTable);
        OdinArraySize arraySize = arrayType.getArraySize();
        tsOdinArrayType.setPsiSizeElement(arraySize);
        tsOdinArrayType.setPsiType(arrayType);
        if(arraySize.getExpression() != null) {
            Integer sizeValue = OdinExpressionEvaluator.evaluate(symbolTable, arraySize.getExpression()).asInt();
            tsOdinArrayType.setSize(sizeValue);
        }

        tsOdinArrayType.setSoa(checkDirective(arrayType.getDirectiveIdentifier(), "#soa"));
        tsOdinArrayType.setSimd(checkDirective(arrayType.getDirectiveIdentifier(), "#simd"));

        TsOdinType elementType = resolveType(symbolTable, arrayType.getTypeDefinition());
        tsOdinArrayType.setElementType(elementType);


        this.type = tsOdinArrayType;
    }

    @Override
    public void visitMapType(@NotNull OdinMapType mapType) {
        TsOdinMapType tsOdinMapType = new TsOdinMapType();
        tsOdinMapType.setSymbolTable(symbolTable);
        tsOdinMapType.setPsiType(mapType);
        TsOdinType keyType = doResolveType(symbolTable, mapType.getKeyType());
        tsOdinMapType.setKeyType(keyType);

        TsOdinType valueType = doResolveType(symbolTable, mapType.getValueType());
        tsOdinMapType.setValueType(valueType);
        this.type = tsOdinMapType;
    }

    @Override
    public void visitPointerType(@NotNull OdinPointerType odinPointerType) {
        TsOdinPointerType tsOdinPointerType = new TsOdinPointerType();
        tsOdinPointerType.setPsiType(odinPointerType);

        TsOdinType elementType = doResolveType(symbolTable, Objects.requireNonNull(odinPointerType.getType()));
        tsOdinPointerType.setDereferencedType(elementType);
        tsOdinPointerType.getSymbolTable().putAll(symbolTable);
        tsOdinPointerType.getSymbolTable().setPackagePath(symbolTable.getPackagePath());
        this.type = tsOdinPointerType;
    }

    @Override
    public void visitProcedureType(@NotNull OdinProcedureType procedureType) {
        TsOdinProcedureType tsOdinProcedureType = new TsOdinProcedureType();
        tsOdinProcedureType.setPsiType(procedureType);
        initializeNamedType(tsOdinProcedureType);
//        addContextSymbol(procedureType.getProject(), tsOdinProcedureType);

        List<TsOdinParameter> parameters = createParameters(tsOdinProcedureType, procedureType.getParamEntryList());
        tsOdinProcedureType.setParameters(parameters);

        OdinReturnParameters returnParameters = procedureType.getReturnParameters();
        if (returnParameters != null) {
            // Single return value
            OdinType type = returnParameters.getType();
            if (type != null) {
                TsOdinType tsOdinType = doResolveType(tsOdinProcedureType.getSymbolTable(), type);

                TsOdinParameter tsOdinParameter = new TsOdinParameter();
                tsOdinParameter.setType(tsOdinType);
                tsOdinParameter.setPsiType(type);
                tsOdinParameter.setIndex(0);

                tsOdinProcedureType.setReturnTypes(List.of(tsOdinType));
                tsOdinProcedureType.setReturnParameters(List.of(tsOdinParameter));
                if (type instanceof OdinPolymorphicType polymorphicType) {
                    tsOdinProcedureType.getSymbolTable().addType(tsOdinType.getName(), tsOdinType);
                    tsOdinProcedureType.getSymbolTable().add(polymorphicType.getDeclaredIdentifier());
                }
            } else {
                List<OdinParamEntry> returnParameterEntries = returnParameters.getParamEntryList();
                List<TsOdinParameter> tsOdinReturnParameters = createParameters(tsOdinProcedureType, returnParameterEntries);
                for (TsOdinParameter tsOdinReturnParameter : tsOdinReturnParameters) {
                    tsOdinProcedureType.getReturnTypes().add(tsOdinReturnParameter.getType());
                }
                tsOdinProcedureType.setReturnParameters(tsOdinReturnParameters);
            }
        }

        this.type = tsOdinProcedureType;
    }

    @Override
    public void visitStructType(@NotNull OdinStructType structType) {
        TsOdinStructType tsOdinStructType = new TsOdinStructType();
        tsOdinStructType.setPsiType(structType);
        initializeNamedType(tsOdinStructType);
        addKnownType(tsOdinStructType, this.typeDeclaredIdentifier, this.typeDeclaration, tsOdinStructType.getSymbolTable());
        List<OdinParamEntry> paramEntries = structType.getParamEntryList();

        List<TsOdinParameter> parameters = createParameters(tsOdinStructType, paramEntries);
        tsOdinStructType.setParameters(parameters);

        this.type = tsOdinStructType;
    }

    static boolean isValuePolymorphic(OdinDeclaredIdentifier nameDeclaredIdentifier) {
        return nameDeclaredIdentifier != null && nameDeclaredIdentifier.getDollar() != null;
    }

    static List<TsOdinParameter> createParameters(OdinParamEntry paramEntry, OdinParameterDeclaration parameterDeclaration, int k) {

        OdinDirective directive = paramEntry.getDirective();
        boolean anyInt = false;
        if (directive != null) {
            anyInt = checkDirective(directive.getDirectiveIdentifier(), "#any_int");
        }

        if (parameterDeclaration instanceof OdinParameterInitialization odinParameterInitialization) {
            OdinDeclaredIdentifier declaredIdentifier = odinParameterInitialization.getParameter().getDeclaredIdentifier();

            TsOdinParameter tsOdinParameter = new TsOdinParameter();
            tsOdinParameter.setPsiType(odinParameterInitialization.getTypeDefinition());
            tsOdinParameter.setIdentifier(declaredIdentifier);
            tsOdinParameter.setExplicitPolymorphicParameter(isValuePolymorphic(declaredIdentifier));
            tsOdinParameter.setDefaultValueExpression(odinParameterInitialization.getExpression());
            tsOdinParameter.setName(declaredIdentifier.getIdentifierToken().getText());
            tsOdinParameter.setIndex(k);
            tsOdinParameter.setAnyInt(anyInt);

            return List.of(tsOdinParameter);
        }

        if (parameterDeclaration instanceof OdinParameterDeclarator odinParameterDeclarator) {
            List<TsOdinParameter> tsOdinParameters = new ArrayList<>();
            for (OdinParameter odinParameter : parameterDeclaration.getParameterList()) {
                OdinDeclaredIdentifier declaredIdentifier = odinParameter.getDeclaredIdentifier();
                TsOdinParameter tsOdinParameter = new TsOdinParameter();
                tsOdinParameter.setPsiType(odinParameterDeclarator.getTypeDefinition());
                tsOdinParameter.setIdentifier(declaredIdentifier);
                tsOdinParameter.setExplicitPolymorphicParameter(isValuePolymorphic(declaredIdentifier));
                tsOdinParameter.setName(declaredIdentifier.getName());
                tsOdinParameter.setIndex(k++);
                tsOdinParameter.setAnyInt(anyInt);
                tsOdinParameters.add(tsOdinParameter);
            }
            return tsOdinParameters;
        }

        if (parameterDeclaration instanceof OdinUnnamedParameter unnamedParameter) {
            TsOdinParameter tsOdinParameterSpec = new TsOdinParameter();
            tsOdinParameterSpec.setPsiType(unnamedParameter.getTypeDefinition());
            tsOdinParameterSpec.setIndex(k);
            tsOdinParameterSpec.setAnyInt(anyInt);
            return Collections.singletonList(tsOdinParameterSpec);
        }

        return Collections.emptyList();
    }

    @Override
    public void visitEnumType(@NotNull OdinEnumType o) {
        TsOdinEnumType tsOdinEnumType = new TsOdinEnumType();
        initializeNamedType(tsOdinEnumType);

        OdinType backingType = o.getType();
        if (backingType != null) {
            TsOdinType tsOdinType = doResolveType(symbolTable, backingType);
            if (tsOdinType instanceof TsOdinBuiltInType tsOdinBuiltInType) {
                tsOdinEnumType.setBackingType(tsOdinBuiltInType);
            }
        }

        tsOdinEnumType.setPsiType(o);

        // TODO Set fields
        this.type = tsOdinEnumType;
    }

    @Override
    public void visitPolymorphicType(@NotNull OdinPolymorphicType polymorphicType) {
        TsOdinPolymorphicType tsOdinPolymorphicType = new TsOdinPolymorphicType();
        initializeNamedType(tsOdinPolymorphicType);
        tsOdinPolymorphicType.setDeclaration(polymorphicType);
        tsOdinPolymorphicType.setDeclaredIdentifier(polymorphicType.getDeclaredIdentifier());
        tsOdinPolymorphicType.setName(polymorphicType.getDeclaredIdentifier().getName());

        tsOdinPolymorphicType.setSymbolTable(symbolTable);

        tsOdinPolymorphicType.setPsiType(polymorphicType);
        this.type = tsOdinPolymorphicType;
    }

    @Override
    public void visitConstrainedType(@NotNull OdinConstrainedType constrainedType) {
        OdinType mainType = constrainedType.getTypeList().get(0);
        OdinType specializedType = constrainedType.getTypeList().get(1);
        TsOdinConstrainedType tsOdinConstrainedType = new TsOdinConstrainedType();
        tsOdinConstrainedType.setPsiType(constrainedType);
        initializeNamedType(tsOdinConstrainedType);

        TsOdinType tsOdinMainType = doResolveType(tsOdinConstrainedType.getSymbolTable(), mainType);
        TsOdinType tsOdinSpecializedType = doResolveType(tsOdinConstrainedType.getSymbolTable(), specializedType);

        tsOdinConstrainedType.setMainType(tsOdinMainType);
        tsOdinConstrainedType.setSpecializedType(tsOdinSpecializedType);

        this.type = tsOdinConstrainedType;
    }

    @Override
    public void visitProcedureOverloadType(@NotNull OdinProcedureOverloadType o) {
        TsOdinProcedureOverloadType tsOdinProcedureOverloadType = new TsOdinProcedureOverloadType();
        initializeNamedType(tsOdinProcedureOverloadType);

        for (OdinProcedureRef procedureRef : o.getProcedureRefList()) {
            TsOdinType tsOdinType = resolveType(symbolTable, procedureRef.getType());
            if (tsOdinType instanceof TsOdinProcedureType tsOdinProcedureType) {
                tsOdinProcedureOverloadType.getTargetProcedures().add(tsOdinProcedureType);
            }
        }

        this.type = tsOdinProcedureOverloadType;
    }

    @Override
    public void visitVariadicType(@NotNull OdinVariadicType o) {
        OdinType psiElementType = o.getType();
        if (psiElementType != null) {
            TsOdinType tsOdinElementType = resolveType(symbolTable, psiElementType);
            TsOdinSliceType tsOdinSliceType = new TsOdinSliceType();
            tsOdinSliceType.setElementType(tsOdinElementType);
            tsOdinSliceType.setSymbolTable(symbolTable);
            tsOdinSliceType.setPsiType(o);
            this.type = tsOdinSliceType;
        }
        super.visitVariadicType(o);
    }
}
