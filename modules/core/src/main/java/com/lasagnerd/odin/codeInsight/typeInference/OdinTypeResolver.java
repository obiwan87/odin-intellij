package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.symbols.OdinBuiltinSymbolService;
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
        OdinTypeResolver typeResolver = new OdinTypeResolver(level, symbolTable, declaration, declaredIdentifier);
        type.accept(typeResolver);
        return Objects.requireNonNullElse(typeResolver.type, TsOdinBuiltInTypes.UNKNOWN);
    }

    public static @NotNull TsOdinMetaType findMetaType(OdinSymbolTable symbolTable,
                                                       @NotNull OdinType type) {
        return findMetaType(symbolTable, null, null, type);
    }

    public static @NotNull TsOdinMetaType findMetaType(OdinSymbolTable symbolTable,
                                                       OdinDeclaredIdentifier declaredIdentifier,
                                                       OdinDeclaration declaration,
                                                       @NotNull OdinType type) {

        TsOdinType tsOdinType = resolveType(symbolTable, declaredIdentifier, declaration, type);
        TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(tsOdinType.getMetaType());
        tsOdinMetaType.setName(tsOdinType.getName());
        tsOdinMetaType.setDeclaredIdentifier(tsOdinMetaType.getDeclaredIdentifier());
        tsOdinMetaType.setDeclaration(tsOdinType.getDeclaration());
        tsOdinMetaType.setSymbolTable(tsOdinType.getSymbolTable());
        tsOdinMetaType.setRepresentedType(tsOdinType);
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

            List<TsOdinParameter> parameters = createParameters(parameterDeclaration, k);
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

    private TsOdinType resolveTypeFromDeclaredIdentifier(OdinSymbolTable symbolTable, OdinDeclaredIdentifier identifier) {
        OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(identifier,
                false,
                OdinDeclaration.class);

        switch (odinDeclaration) {
            case OdinStructDeclarationStatement structDeclarationStatement -> {
                return doResolveType(symbolTable, identifier, odinDeclaration, structDeclarationStatement.getStructType());
            }
            case OdinEnumDeclarationStatement enumDeclarationStatement -> {
                return doResolveType(symbolTable, identifier, odinDeclaration, enumDeclarationStatement.getEnumType());
            }
            case OdinUnionDeclarationStatement unionDeclarationStatement -> {
                return doResolveType(symbolTable, identifier, odinDeclaration, unionDeclarationStatement.getUnionType());
            }
            case OdinProcedureDeclarationStatement procedureDeclarationStatement -> {
                return doResolveType(symbolTable, identifier, odinDeclaration, procedureDeclarationStatement.getProcedureDefinition().getProcedureType());
            }
            case OdinConstantInitializationStatement constantInitializationStatement -> {
                List<OdinExpression> expressionList = constantInitializationStatement.getExpressionsList().getExpressionList();
                if (!expressionList.isEmpty()) {
                    int index = constantInitializationStatement.getDeclaredIdentifiers().indexOf(identifier);
                    if (index == -1) {
                        return TsOdinBuiltInTypes.UNKNOWN;
                    }
                    if (expressionList.size() <= index) {
                        return TsOdinBuiltInTypes.UNKNOWN;
                    }

                    OdinExpression odinExpression = expressionList.get(index);
                    TsOdinType tsOdinType = doInferType(symbolTable, odinExpression);
                    if (tsOdinType instanceof TsOdinMetaType metaType) {
                        TsOdinType resolvedMetaType = doResolveMetaType(metaType.getSymbolTable(), metaType);
                        return createTypeAliasFromMetaType(identifier, resolvedMetaType, odinDeclaration, odinExpression);
                    }
                    return TsOdinBuiltInTypes.UNKNOWN;
                }
            }
            case OdinPolymorphicType polymorphicType -> {
                return doResolveType(symbolTable, identifier, odinDeclaration, polymorphicType);
            }
            case OdinBitsetDeclarationStatement odinBitsetDeclarationStatement -> {
                return doResolveType(symbolTable, odinBitsetDeclarationStatement.getBitSetType());
            }
            case OdinProcedureOverloadDeclarationStatement procedureOverloadDeclarationStatement -> {
                return doResolveType(symbolTable, procedureOverloadDeclarationStatement.getProcedureOverloadType());
            }
            case OdinBitFieldDeclarationStatement bitFieldDeclarationStatement -> {
                return doResolveType(symbolTable, identifier, odinDeclaration, bitFieldDeclarationStatement.getBitFieldType());
            }
            case null, default -> {
            }
        }

        return TsOdinBuiltInTypes.UNKNOWN;
    }

    public static @NotNull TsOdinTypeAlias createTypeAliasFromMetaType(OdinDeclaredIdentifier identifier,
                                                                       TsOdinType resolvedMetaType,
                                                                       OdinDeclaration odinDeclaration,
                                                                       OdinExpression odinExpression) {
        TsOdinTypeAlias typeAlias = new TsOdinTypeAlias();
        typeAlias.setAliasedType(resolvedMetaType);
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
            this.type = doResolveType(symbolTable, o.getType());
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

    @Override
    public void visitArrayType(@NotNull OdinArrayType arrayType) {
        TsOdinArrayType tsOdinArrayType = new TsOdinArrayType();
        tsOdinArrayType.setSymbolTable(symbolTable);
        tsOdinArrayType.setPsiSizeElement(arrayType.getArraySize());
        tsOdinArrayType.setPsiType(arrayType);

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

    private static void addContextSymbol(@NotNull Project project, TsOdinProcedureType tsOdinProcedureType) {
        OdinSymbol contextSymbol = OdinBuiltinSymbolService.getInstance(project).createImplicitStructSymbol("context", "Context");
        OdinSymbolTable procSymbolTable = new OdinSymbolTable();
        procSymbolTable.add(contextSymbol);
        OdinSymbolTable symbolTable1 = tsOdinProcedureType.getSymbolTable();
        procSymbolTable.setParentSymbolTable(symbolTable1);
        tsOdinProcedureType.setSymbolTable(procSymbolTable);
    }


    @Override
    public void visitStructType(@NotNull OdinStructType structType) {
        TsOdinStructType tsOdinStructType = new TsOdinStructType();
        tsOdinStructType.setPsiType(structType);
        initializeNamedType(tsOdinStructType);

        List<OdinParamEntry> paramEntries = structType.getParamEntryList();

        List<TsOdinParameter> parameters = createParameters(tsOdinStructType, paramEntries);
        tsOdinStructType.setParameters(parameters);

        this.type = tsOdinStructType;
    }

    static boolean isValuePolymorphic(OdinDeclaredIdentifier nameDeclaredIdentifier) {
        return nameDeclaredIdentifier != null && nameDeclaredIdentifier.getDollar() != null;
    }

    static List<TsOdinParameter> createParameters(OdinParameterDeclaration parameterDeclaration, int k) {

        if (parameterDeclaration instanceof OdinParameterInitialization odinParameterInitialization) {
            OdinDeclaredIdentifier declaredIdentifier = odinParameterInitialization.getParameter().getDeclaredIdentifier();

            TsOdinParameter tsOdinParameter = new TsOdinParameter();
            tsOdinParameter.setPsiType(odinParameterInitialization.getTypeDefinition());
            tsOdinParameter.setIdentifier(declaredIdentifier);
            tsOdinParameter.setExplicitPolymorphicParameter(isValuePolymorphic(declaredIdentifier));
            tsOdinParameter.setDefaultValueExpression(odinParameterInitialization.getExpression());
            tsOdinParameter.setName(declaredIdentifier.getText());
            tsOdinParameter.setIndex(k);

            return List.of(tsOdinParameter);
        }

        if (parameterDeclaration instanceof OdinParameterDeclarator odinParameterDeclarator) {
            List<TsOdinParameter> parameterSpecs = new ArrayList<>();
            for (OdinParameter odinParameter : parameterDeclaration.getParameterList()) {
                OdinDeclaredIdentifier declaredIdentifier = odinParameter.getDeclaredIdentifier();
                TsOdinParameter tsOdinParameterSpec = new TsOdinParameter();
                tsOdinParameterSpec.setPsiType(odinParameterDeclarator.getTypeDefinition());
                tsOdinParameterSpec.setIdentifier(declaredIdentifier);
                tsOdinParameterSpec.setExplicitPolymorphicParameter(isValuePolymorphic(declaredIdentifier));
                tsOdinParameterSpec.setName(declaredIdentifier.getName());
                tsOdinParameterSpec.setIndex(k++);
                parameterSpecs.add(tsOdinParameterSpec);
            }
            return parameterSpecs;
        }

        if (parameterDeclaration instanceof OdinUnnamedParameter unnamedParameter) {
            TsOdinParameter tsOdinParameterSpec = new TsOdinParameter();
            tsOdinParameterSpec.setPsiType(unnamedParameter.getTypeDefinition());
            tsOdinParameterSpec.setIndex(k);

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

    private static class OdinMetaTypeResolver extends OdinVisitor {

        private TsOdinMetaType metaType;

        public OdinMetaTypeResolver() {
        }

        public static TsOdinMetaType resolveMetaType(OdinType type) {
            OdinMetaTypeResolver odinMetaTypeResolver = new OdinMetaTypeResolver();
            type.accept(odinMetaTypeResolver);
            return odinMetaTypeResolver.metaType;
        }

        TsOdinMetaType createMetaType(OdinType type, TsOdinMetaType.MetaType metaType) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(metaType);
            tsOdinMetaType.setPsiType(type);

            return tsOdinMetaType;
        }

        @Override
        public void visitArrayType(@NotNull OdinArrayType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.ARRAY);
        }

        @Override
        public void visitSliceType(@NotNull OdinSliceType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.SLICE);
        }

        @Override
        public void visitDynamicArrayType(@NotNull OdinDynamicArrayType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.DYNAMIC_ARRAY);
        }

        @Override
        public void visitProcedureType(@NotNull OdinProcedureType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.PROCEDURE);
        }

        @Override
        public void visitPointerType(@NotNull OdinPointerType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.POINTER);
        }

        @Override
        public void visitMultiPointerType(@NotNull OdinMultiPointerType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.MULTI_POINTER);
        }

        @Override
        public void visitBitSetType(@NotNull OdinBitSetType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.BIT_SET);
        }

        @Override
        public void visitMatrixType(@NotNull OdinMatrixType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.MATRIX);
        }

        @Override
        public void visitMapType(@NotNull OdinMapType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.MAP);
        }

        @Override
        public void visitStructType(@NotNull OdinStructType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.STRUCT);
        }

        @Override
        public void visitEnumType(@NotNull OdinEnumType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.ENUM);
        }

        @Override
        public void visitUnionType(@NotNull OdinUnionType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.UNION);
        }

        @Override
        public void visitPolymorphicType(@NotNull OdinPolymorphicType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.POLYMORPHIC);
        }

        @Override
        public void visitBitFieldType(@NotNull OdinBitFieldType o) {
            this.metaType = createMetaType(o.getType(), TsOdinMetaType.MetaType.BIT_FIELD);
        }

        @Override
        public void visitQualifiedType(@NotNull OdinQualifiedType o) {
            System.out.println("qualified type visited");
        }


    }
}
