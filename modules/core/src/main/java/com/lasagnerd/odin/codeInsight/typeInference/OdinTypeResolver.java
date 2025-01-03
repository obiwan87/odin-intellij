package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.evaluation.OdinExpressionEvaluator;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinDeclarationSymbolResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
public class OdinTypeResolver extends OdinVisitor {

    private boolean substitutionMode;

    public record OdinTypeResolverParameters(OdinContext context,
                                             OdinDeclaredIdentifier declaredIdentifier,
                                             OdinDeclaration declaration,
                                             boolean substitutionMode) {

    }

    public static @NotNull TsOdinType resolveType(OdinTypeResolverParameters typeResolverParameters, OdinType type) {
        if (type == null)
            return TsOdinBuiltInTypes.UNKNOWN;

        OdinContext context = initializeContext(typeResolverParameters.context(), type);

        OdinTypeResolver typeResolver = new OdinTypeResolver(0,
                context,
                typeResolverParameters.declaration(),
                typeResolverParameters.declaredIdentifier());
        typeResolver.substitutionMode = typeResolverParameters.substitutionMode;
        type.accept(typeResolver);
        return Objects.requireNonNullElse(typeResolver.type, TsOdinBuiltInTypes.UNKNOWN);
    }

    public static @NotNull TsOdinType resolveType(OdinContext context, OdinSymbol symbol) {
        OdinType type = symbol.getPsiType();
        OdinDeclaration declaration = symbol.getDeclaration();
        OdinDeclaredIdentifier declaredIdentifier;
        if (symbol.getDeclaredIdentifier() instanceof OdinDeclaredIdentifier) {
            declaredIdentifier = (OdinDeclaredIdentifier) symbol.getDeclaredIdentifier();
        } else {
            declaredIdentifier = null;
        }

        return resolveType(0, context, declaredIdentifier, declaration, type);
    }

    public static @NotNull TsOdinType resolveType(@NotNull OdinContext context, @NotNull OdinType type) {
        return resolveType(0, context, null, null, type);
    }

    public static @NotNull TsOdinType resolveType(@NotNull OdinContext context,
                                                  OdinDeclaredIdentifier declaredIdentifier,
                                                  OdinDeclaration declaration,
                                                  @NotNull OdinType type) {
        return resolveType(0, context, declaredIdentifier, declaration, type);
    }

    public static @NotNull TsOdinType resolveType(int level,
                                                  OdinContext context,
                                                  OdinDeclaredIdentifier declaredIdentifier,
                                                  OdinDeclaration declaration,
                                                  OdinType type) {
        return resolveType(level, context, declaredIdentifier, declaration, false, type);
    }

    public static @NotNull TsOdinType resolveType(int level,
                                                  OdinContext context,
                                                  OdinDeclaredIdentifier declaredIdentifier,
                                                  OdinDeclaration declaration,
                                                  boolean substitutionMode,
                                                  OdinType type) {
        OdinTypeResolverParameters typeResolverParameters = new OdinTypeResolverParameters(context, declaredIdentifier, declaration, substitutionMode);
        if (!substitutionMode) {
            return type.getResolvedType(typeResolverParameters);
        }
        return resolveType(typeResolverParameters, type);
    }

    public static @NotNull OdinContext initializeContext(OdinContext context, PsiElement element) {
        if (context == null) {
            context = new OdinContext(OdinImportService.getInstance(element.getProject()).getPackagePath(element));
        }
        return context;
    }

    public static @NotNull TsOdinTypeReference findTypeReference(OdinContext context,
                                                                 OdinTypeDefinitionExpression expression,
                                                                 @NotNull OdinType type) {
        return findTypeReference(context, null, null, expression, type);
    }

    public static @NotNull TsOdinTypeReference findTypeReference(@NotNull OdinContext context,
                                                                 OdinDeclaredIdentifier declaredIdentifier,
                                                                 OdinDeclaration declaration,
                                                                 OdinExpression firstExpression,
                                                                 @NotNull OdinType type) {

        TsOdinType tsOdinType = type.getResolvedType(new OdinTypeResolverParameters(context, declaredIdentifier, declaration, false));
        return createTypeReference(tsOdinType, firstExpression);
    }

    public static @NotNull TsOdinTypeReference createTypeReference(TsOdinType tsOdinType, OdinExpression firstExpression) {
        boolean distinct = OdinInsightUtils.isDistinct(firstExpression);
        return createTypeReference(tsOdinType, distinct);
    }

    public static @NotNull TsOdinTypeReference createTypeReference(TsOdinType tsOdinType, boolean distinct) {
        TsOdinTypeReference tsOdinTypeReference = new TsOdinTypeReference(tsOdinType.getTypeReferenceKind());
        tsOdinTypeReference.setName(tsOdinType.getName());
        tsOdinTypeReference.setDeclaredIdentifier(tsOdinTypeReference.getDeclaredIdentifier());
        tsOdinTypeReference.setDeclaration(tsOdinType.getDeclaration());
        tsOdinTypeReference.setContext(tsOdinType.getContext());
        tsOdinTypeReference.setRepresentedType(tsOdinType);
        tsOdinTypeReference.setPsiType(tsOdinType.getPsiType());
        tsOdinTypeReference.setDistinct(distinct);
        return tsOdinTypeReference;
    }

    public static @NotNull TsOdinType resolveTypeReference(OdinContext context, TsOdinTypeReference typeReference) {
        return resolveTypeReference(0, context, typeReference);
    }

    public static TsOdinType resolveTypeReference(int level, OdinContext context, TsOdinTypeReference typeReference) {
        if (typeReference.getRepresentedType() instanceof TsOdinBuiltInType) {
            return TsOdinBuiltInTypes.getBuiltInType(typeReference.getName());
        } else {
            TsOdinType tsOdinType;
            if (typeReference.getRepresentedType() != null) {
                return typeReference.getRepresentedType();
            } else if (typeReference.getPsiType() != null) {
                OdinDeclaredIdentifier declaredIdentifier = typeReference.getDeclaredIdentifier();
                tsOdinType = resolveType(level,
                        context,
                        declaredIdentifier,
                        typeReference.getDeclaration(),
                        typeReference.getPsiType());
                tsOdinType.setDeclaration(typeReference.getDeclaration());
                tsOdinType.setDeclaredIdentifier(declaredIdentifier);
                context.addKnownType(declaredIdentifier, tsOdinType);
                if (declaredIdentifier != null) {
                    tsOdinType.setName(declaredIdentifier.getName());
                }
                tsOdinType.getContext().merge(context);
                typeReference.setRepresentedType(tsOdinType);
                return tsOdinType;
            } else if (typeReference.getTargetTypeKind() == TsOdinTypeKind.ALIAS) {

                TsOdinTypeAlias typeAlias = new TsOdinTypeAlias();
                typeReference.setRepresentedType(typeAlias);

                if (typeReference.getTypeExpression() instanceof OdinTypeDefinitionExpression typeDefinitionExpression) {
                    typeAlias.setDistinct(typeDefinitionExpression.getDistinct() != null);
                    typeAlias.setPsiType(typeDefinitionExpression.getType());
                } else {
                    typeAlias.setPsiTypeExpression(typeReference.getTypeExpression());
                }

                typeAlias.setDeclaration(typeReference.getDeclaration());
                typeAlias.setName(typeReference.getName());
                typeAlias.setContext(typeReference.getContext());

                TsOdinType aliasedType = resolveTypeReference(level + 1,
                        typeReference.getAliasedTypeReference().getContext(),
                        typeReference.getAliasedTypeReference());
                typeAlias.setAliasedType(aliasedType);

                return typeAlias;
            }
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    // Result
    TsOdinType type;

    private final int level;
    private final OdinContext context;
    private final OdinDeclaration typeDeclaration;
    private final OdinDeclaredIdentifier typeDeclaredIdentifier;

    // avoid stackoverflow when encountering circular references
    private final Set<OdinDeclaredIdentifier> visitedDeclaredIdentifiers = new HashSet<>();


    public OdinTypeResolver(int level, OdinContext context, OdinDeclaration typeDeclaration, OdinDeclaredIdentifier typeDeclaredIdentifier) {
        this.level = level;
        this.context = context;
        this.typeDeclaration = typeDeclaration;
        this.typeDeclaredIdentifier = typeDeclaredIdentifier;
    }

    /**
     * Creates a new scope for a given package identifier defined within this scope.
     *
     * @param context           The symbol table to use
     * @param packageIdentifier The identifier that is used to reference the package
     * @return A new scope with all the declared symbols of the referenced package
     */

    public static OdinContext getImportContext(OdinContext context, String packageIdentifier) {
        OdinSymbol odinSymbol = context.getSymbol(packageIdentifier);
        if (odinSymbol != null) {
            PsiNamedElement declaredIdentifier = odinSymbol.getDeclaredIdentifier();
            OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(declaredIdentifier, OdinDeclaration.class, false);
            if (odinDeclaration instanceof OdinImportDeclaration importDeclaration) {
                return OdinImportUtils.getSymbolsOfImportedPackage(context, context.getPackagePath(), importDeclaration).asContext();
            }
        }
        return new OdinContext();
    }

    // resolve type calls
    public @NotNull TsOdinType doResolveType(OdinContext context,
                                             OdinDeclaredIdentifier declaredIdentifier,
                                             OdinDeclaration declaration,
                                             @NotNull OdinType type) {
        return resolveType(level + 1, context, declaredIdentifier, declaration, this.substitutionMode, type);
    }

    public @NotNull TsOdinType doResolveType(OdinContext context,
                                             @NotNull OdinType type) {
        return resolveType(level + 1, context, null, null, this.substitutionMode, type);
    }

    public @NotNull TsOdinType doResolveTypeReference(OdinContext context, TsOdinTypeReference typeReference) {
        return resolveTypeReference(level + 1, context, typeReference);
    }

    public @NotNull TsOdinType doResolveType(OdinContext context, OdinExpression odinExpression) {
        TsOdinType tsOdinType = odinExpression.getInferredType(context);
        if (tsOdinType instanceof TsOdinTypeReference tsOdinTypeReference) {
            return doResolveTypeReference(context, tsOdinTypeReference);
        }
        return tsOdinType;
    }

    // logging
    @SuppressWarnings("unused")
    public void log(String message) {
//        System.out.println("\t".repeat(level) + message);
    }

    private List<TsOdinParameter> createParameters(TsOdinType baseType, List<OdinParamEntry> paramEntries) {
        OdinContext localContext = baseType.getContext();
        List<TsOdinParameter> typeParameters = new ArrayList<>();
        int k = 0;
        for (var paramEntry : paramEntries) {
            OdinParameterDeclaration parameterDeclaration = paramEntry.getParameterDeclaration();

            // First, add all $Identifier expressions we encounter in this parameter to the current scope

            // Value polymorphism
            for (OdinPolymorphicType odinPolymorphicType : PsiTreeUtil.findChildrenOfType(paramEntry, OdinPolymorphicType.class)) {
                TsOdinType tsOdinType = doResolveType(localContext, odinPolymorphicType);
                localContext.addPolymorphicType(tsOdinType.getName(), tsOdinType);
                PsiNamedElement namedElement = odinPolymorphicType.getDeclaredIdentifier();
                localContext.getSymbolTable().add(namedElement);
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
                    valuePolymorphicType.setDeclaration(parameterDeclaration);
                    localContext.addPolymorphicType(valuePolymorphicType.getName(), valuePolymorphicType);
                    localContext.getSymbolTable().add(declaredIdentifier);
                    if (baseType instanceof TsOdinGenericType tsOdinGenericType) {
                        tsOdinGenericType.getPolymorphicParameters().put(valuePolymorphicType.getName(), valuePolymorphicType);
                    }
                }
            }

            List<TsOdinParameter> parameters = createParameters(paramEntry, parameterDeclaration, k);
            for (var tsOdinParameter : parameters) {
                if (tsOdinParameter.getPsiType() != null) {
                    TsOdinType tsOdinType = doResolveType(localContext,
                            tsOdinParameter.getPsiType());

                    tsOdinParameter.setType(tsOdinType);
                } else if (tsOdinParameter.getDefaultValueExpression() != null) {
                    TsOdinType tsOdinType = tsOdinParameter.getDefaultValueExpression().getInferredType();
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
            this.context.addKnownType(typeDeclaredIdentifier, tsOdinType);
        }
        tsOdinType.getContext().merge(context);
        tsOdinType.getContext().setUseKnowledge(context.isUseKnowledge());
        tsOdinType.getContext().setPackagePath(context.getPackagePath());
        log("Initialized " + tsOdinType.getClass().getSimpleName() + " with name " + name);
    }

    private TsOdinType resolveIdentifier(OdinIdentifier typeIdentifier, OdinContext context) {
        PsiNamedElement declaration;
        String identifierText = typeIdentifier.getText();

        TsOdinType polymorphicType = context.getPolymorphicType(identifierText);
        if (polymorphicType != null) {
            return polymorphicType;
        } else {
            OdinSymbol symbol = typeIdentifier.getReferencedSymbol(context);

            declaration = symbol != null ? symbol.getDeclaredIdentifier() : null;

            if (!(declaration instanceof OdinDeclaredIdentifier declaredIdentifier)) {
                return TsOdinBuiltInTypes.UNKNOWN;
            } else {
                var knownType = context.getKnownTypes().get(declaredIdentifier);
                if (knownType != null) {
                    log("Cache hit for type: " + knownType.getLabel());
                    return knownType;
                } else {
                    return resolveTypeFromDeclaredIdentifier(context, declaredIdentifier);
                }
            }
        }
    }

    public static TsOdinType resolveType(OdinContext context, OdinDeclaredIdentifier identifier) {
        OdinDeclaration declaration = PsiTreeUtil.getParentOfType(identifier, OdinDeclaration.class);
        OdinTypeResolver typeResolver = new OdinTypeResolver(0, context, declaration, identifier);
        return typeResolver.resolveTypeFromDeclaredIdentifier(context, identifier);
    }

    @Override
    public void visitProcedureLiteralType(@NotNull OdinProcedureLiteralType o) {
        this.type = doResolveType(
                context,
                this.typeDeclaredIdentifier,
                this.typeDeclaration,
                o.getProcedureDefinition().getProcedureSignature().getProcedureType());
    }

    private TsOdinType resolveTypeFromDeclaredIdentifier(OdinContext context, OdinDeclaredIdentifier declaredIdentifier) {
        OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(declaredIdentifier,
                false,
                OdinDeclaration.class);

        TsOdinType builtinType = OdinDeclaredIdentifierMixin.tryGetBuiltinType(declaredIdentifier);
        if (builtinType instanceof TsOdinTypeReference typeReference) {
            return typeReference.referencedType();
        }

        OdinContext typeContext;
        if (odinDeclaration != null) {
            typeContext = initializeContext(context, odinDeclaration);
        } else {
            typeContext = new OdinContext();
        }
        switch (odinDeclaration) {
            case OdinConstantInitDeclaration constantInitDeclaration -> {
                if (constantInitDeclaration.getExpressionList().isEmpty())
                    return TsOdinBuiltInTypes.UNKNOWN;

                OdinExpression firstExpression = constantInitDeclaration.getExpressionList().getFirst();
                OdinType declaredType = OdinInsightUtils.getDeclaredType(constantInitDeclaration);

                // Check whether this constant is a "pure" type definition, or if it is a type alias
                if (isTypeDefinition(declaredType)) {
                    // check distinct
                    TsOdinType tsOdinType = doResolveType(typeContext, declaredIdentifier, odinDeclaration, declaredType);
                    tsOdinType.setDistinct(OdinInsightUtils.isDistinct(firstExpression));
                    return tsOdinType;
                }

                // Here we have a type alias
                List<OdinExpression> expressionList = constantInitDeclaration.getExpressionList();
                if (!expressionList.isEmpty()) {
                    int index = constantInitDeclaration.getDeclaredIdentifiers().indexOf(declaredIdentifier);
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
                    typeAlias.setName(declaredIdentifier.getText());
                    addKnownType(typeAlias, declaredIdentifier, odinDeclaration, typeContext);
                    TsOdinType tsOdinType = odinExpression.getInferredType(typeContext);
                    if (tsOdinType instanceof TsOdinTypeReference typeReference) {
                        TsOdinType resolvedTypeReference = doResolveTypeReference(typeReference.getContext(), typeReference);
                        typeAlias.setDistinct(OdinInsightUtils.isDistinct(odinExpression));
                        return createTypeAliasFromTypeReference(typeAlias, declaredIdentifier, resolvedTypeReference, odinDeclaration, odinExpression);
                    }
                    return TsOdinBuiltInTypes.UNKNOWN;
                }
            }
            case OdinPolymorphicType polymorphicType -> {
                return doResolveType(context, declaredIdentifier, odinDeclaration, polymorphicType);
            }
            case OdinParameterDeclarator odinParameterDeclarator -> {
                // Look for polymorphic type definitions like $C: typeid/...
                if (declaredIdentifier.getDollar() != null) {
                    return doResolveType(context, odinParameterDeclarator.getTypeDefinition());
                }
            }
            case null, default -> {
            }
        }

        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private static boolean isTypeDefinition(OdinType declaredType) {
        return declaredType instanceof OdinStructType
                || declaredType instanceof OdinBitFieldType
                || declaredType instanceof OdinUnionType
                || declaredType instanceof OdinProcedureGroupType
                || declaredType instanceof OdinProcedureType
                || declaredType instanceof OdinProcedureLiteralType
                || declaredType instanceof OdinEnumType;
    }

    public static @NotNull TsOdinTypeAlias createTypeAliasFromTypeReference(TsOdinTypeAlias typeAlias,
                                                                            OdinDeclaredIdentifier identifier,
                                                                            TsOdinType resolvedTypeReference,
                                                                            OdinDeclaration odinDeclaration,
                                                                            OdinExpression odinExpression) {
        if (typeAlias != resolvedTypeReference) {
            typeAlias.setAliasedType(resolvedTypeReference);
        }
        typeAlias.setDeclaration(odinDeclaration);
        typeAlias.setDeclaredIdentifier(identifier);
        typeAlias.setName(identifier.getName());
        typeAlias.setPsiTypeExpression(resolvedTypeReference.getPsiTypeExpression());

        if (odinExpression instanceof OdinTypeDefinitionExpression typeDefinitionExpression) {
            typeAlias.setDistinct(typeDefinitionExpression.getDistinct() != null);
            typeAlias.setPsiType(typeDefinitionExpression.getType());
        }
        typeAlias.setContext(resolvedTypeReference.getContext());
        return typeAlias;
    }

    // Visitor methods
    @Override
    public void visitQualifiedType(@NotNull OdinQualifiedType qualifiedType) {
        OdinContext packageScope = getImportContext(context, qualifiedType.getPackageIdentifier().getIdentifierToken().getText());
        OdinSimpleRefType simpleRefType = qualifiedType.getSimpleRefType();
        if (simpleRefType != null) {
            this.type = doResolveType(packageScope, simpleRefType);
        }
    }

    @Override
    public void visitSimpleRefType(@NotNull OdinSimpleRefType o) {
        OdinIdentifier identifier = o.getIdentifier();
        this.type = resolveIdentifier(identifier, context);
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
        this.type = doResolveType(context, type);

        if (this.type instanceof TsOdinStructType structType) {
            // This should not be called again if type is already been visited
            // it might be a better idea to specialize the type at inference time and not at resolution time
            this.type = OdinTypeSpecializer.specializeStructOrGetCached(context, structType, o.getArgumentList());
        }

        if (this.type instanceof TsOdinUnionType unionType) {
            // This should not be called again if type is already been visited
            this.type = OdinTypeSpecializer.specializeUnionOrGetCached(context, unionType, o.getArgumentList());
        }
    }

    @Override
    public void visitMatrixType(@NotNull OdinMatrixType o) {
        if (o.getType() != null) {
            TsOdinMatrixType tsOdinMatrixType = new TsOdinMatrixType();
            TsOdinType elementType = doResolveType(context, o.getType());
            tsOdinMatrixType.setElementType(elementType);
            tsOdinMatrixType.setPsiType(o);
            tsOdinMatrixType.setContext(context);
            @NotNull List<OdinArraySize> arraySizeList = o.getArraySizeList();

            for (int i = 0; i < arraySizeList.size(); i++) {
                OdinArraySize arraySize = arraySizeList.get(i);
                if (arraySize.getExpression() != null) {
                    Integer sizeValue = OdinExpressionEvaluator.evaluate(context, arraySize.getExpression()).toInt();
                    if (i == 1) {
                        tsOdinMatrixType.setColumnsExpression(arraySize.getExpression());
                        tsOdinMatrixType.setColumns(sizeValue);
                    }
                    if (i == 0) {
                        tsOdinMatrixType.setRowsExpression(arraySize.getExpression());
                        tsOdinMatrixType.setRows(sizeValue);
                    }
                }
            }


            this.type = tsOdinMatrixType;
        }
    }

    @Override
    public void visitSliceType(@NotNull OdinSliceType o) {
        OdinType elementPsiType = o.getType();
        TsOdinSliceType tsOdinSliceType = new TsOdinSliceType();
        if (elementPsiType != null) {
            TsOdinType tsOdinElementType = doResolveType(context, elementPsiType);
            tsOdinSliceType.setElementType(tsOdinElementType);
            tsOdinSliceType.setContext(context);
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
            TsOdinType tsOdinElementType = doResolveType(context, elementPsiType);
            tsOdinDynamicArray.setElementType(tsOdinElementType);
            tsOdinDynamicArray.setContext(context);
            tsOdinDynamicArray.setPsiType(o);
            tsOdinDynamicArray.setSoa(checkDirective(o.getDirectiveIdentifier(), "#soa"));
            this.type = tsOdinDynamicArray;
        }
    }

    @Override
    public void visitMultiPointerType(@NotNull OdinMultiPointerType o) {
        TsOdinMultiPointerType tsOdinMultiPointerType = new TsOdinMultiPointerType();
        tsOdinMultiPointerType.setPsiType(o);
        TsOdinType dereferencedType = doResolveType(context, Objects.requireNonNull(o.getType()));
        tsOdinMultiPointerType.setDereferencedType(dereferencedType);
        tsOdinMultiPointerType.setContext(context);
        this.type = tsOdinMultiPointerType;
    }

    @Override
    public void visitBitSetType(@NotNull OdinBitSetType o) {
        TsOdinBitSetType tsOdinBitSetType = new TsOdinBitSetType();
        tsOdinBitSetType.setPsiType(o);
        initializeNamedType(tsOdinBitSetType);
        OdinExpression elementTypeExpression = o.getExpression();
        if (elementTypeExpression != null) {
            TsOdinType tsOdinElementType = doResolveType(context, elementTypeExpression);
            tsOdinBitSetType.setElementType(tsOdinElementType);
        }

        if (o.getType() != null) {
            TsOdinType tsBackingType = doResolveType(context, o.getType());
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
        addKnownType(tsOdinUnionType, typeDeclaredIdentifier, typeDeclaration, tsOdinUnionType.getContext());
        OdinUnionBlock unionBlock = unionType.getUnionBlock();
        if (unionBlock != null) {
            OdinUnionBody unionBody = unionBlock.getUnionBody();
            if (unionBody != null) {
                List<OdinType> types = unionBody.getTypeList();
                for (OdinType type : types) {
                    TsOdinType tsOdinType = doResolveType(tsOdinUnionType.getContext(), type);

                    TsOdinUnionVariant tsOdinUnionVariant = new TsOdinUnionVariant();
                    tsOdinUnionVariant.setPsiType(type);
                    tsOdinUnionVariant.setType(tsOdinType);
                    tsOdinUnionType.getVariants().add(tsOdinUnionVariant);
                }
            }
        }
        this.type = tsOdinUnionType;
    }

    private static void addKnownType(TsOdinType tsOdinType,
                                     OdinDeclaredIdentifier declaredIdentifier,
                                     OdinDeclaration declaration,
                                     OdinContext context) {
        if (declaredIdentifier != null) {
            context.addKnownType(declaredIdentifier, tsOdinType);
            List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getSymbols(declaration);
            OdinSymbol odinSymbol = localSymbols.getFirst();
            context.getSymbolTable().add(odinSymbol, true);
        }
    }

    @Override
    public void visitArrayType(@NotNull OdinArrayType arrayType) {
        TsOdinArrayType tsOdinArrayType = new TsOdinArrayType();
        tsOdinArrayType.setContext(context);
        OdinArraySize arraySize = arrayType.getArraySize();
        tsOdinArrayType.setPsiSizeElement(arraySize);
        tsOdinArrayType.setPsiType(arrayType);
        if (arraySize.getExpression() != null) {
            Integer sizeValue = OdinExpressionEvaluator.evaluate(context, arraySize.getExpression()).asInt();
            tsOdinArrayType.setSize(sizeValue);
        }

        tsOdinArrayType.setSoa(checkDirective(arrayType.getDirectiveIdentifier(), "#soa"));
        tsOdinArrayType.setSimd(checkDirective(arrayType.getDirectiveIdentifier(), "#simd"));

        TsOdinType elementType = doResolveType(context, arrayType.getTypeDefinition());
        tsOdinArrayType.setElementType(elementType);


        this.type = tsOdinArrayType;
    }

    @Override
    public void visitMapType(@NotNull OdinMapType mapType) {
        TsOdinMapType tsOdinMapType = new TsOdinMapType();
        tsOdinMapType.setContext(context);
        tsOdinMapType.setPsiType(mapType);
        TsOdinType keyType = doResolveType(context, mapType.getKeyType());
        tsOdinMapType.setKeyType(keyType);

        TsOdinType valueType = doResolveType(context, mapType.getValueType());
        tsOdinMapType.setValueType(valueType);
        this.type = tsOdinMapType;
    }

    @Override
    public void visitPointerType(@NotNull OdinPointerType odinPointerType) {
        TsOdinPointerType tsOdinPointerType = new TsOdinPointerType();
        tsOdinPointerType.setPsiType(odinPointerType);

        TsOdinType elementType = doResolveType(context, Objects.requireNonNull(odinPointerType.getType()));
        tsOdinPointerType.setDereferencedType(elementType);
        tsOdinPointerType.getContext().merge(context);
        tsOdinPointerType.getContext().setPackagePath(context.getPackagePath());
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
                TsOdinType tsOdinType = doResolveType(tsOdinProcedureType.getContext(), type);

                TsOdinParameter tsOdinParameter = new TsOdinParameter();
                tsOdinParameter.setType(tsOdinType);
                tsOdinParameter.setPsiType(type);
                tsOdinParameter.setIndex(0);

                tsOdinProcedureType.setReturnTypes(List.of(tsOdinType));
                tsOdinProcedureType.setReturnParameters(List.of(tsOdinParameter));
                if (type instanceof OdinPolymorphicType polymorphicType) {
                    tsOdinProcedureType.getContext().addPolymorphicType(tsOdinType.getName(), tsOdinType);
                    PsiNamedElement namedElement = polymorphicType.getDeclaredIdentifier();
                    tsOdinProcedureType.getContext().getSymbolTable().add(namedElement);
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
        addKnownType(tsOdinStructType, this.typeDeclaredIdentifier, this.typeDeclaration, tsOdinStructType.getContext());
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
            tsOdinParameter.setParameterDeclaration(odinParameterInitialization);
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
                tsOdinParameter.setParameterDeclaration(odinParameterDeclarator);
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
            TsOdinParameter tsOdinParameter = new TsOdinParameter();
            tsOdinParameter.setPsiType(unnamedParameter.getTypeDefinition());
            tsOdinParameter.setIndex(k);
            tsOdinParameter.setParameterDeclaration(unnamedParameter);
            tsOdinParameter.setAnyInt(anyInt);
            return Collections.singletonList(tsOdinParameter);
        }

        return Collections.emptyList();
    }

    @Override
    public void visitEnumType(@NotNull OdinEnumType o) {
        TsOdinEnumType tsOdinEnumType = new TsOdinEnumType();
        initializeNamedType(tsOdinEnumType);

        OdinType backingType = o.getType();
        if (backingType != null) {
            TsOdinType tsOdinType = doResolveType(context, backingType);
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

        tsOdinPolymorphicType.setContext(context);

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

        TsOdinType tsOdinMainType = doResolveType(tsOdinConstrainedType.getContext(), mainType);
        TsOdinType tsOdinSpecializedType = doResolveType(tsOdinConstrainedType.getContext(), specializedType);

        tsOdinConstrainedType.setMainType(tsOdinMainType);
        tsOdinConstrainedType.setSpecializedType(tsOdinSpecializedType);

        this.type = tsOdinConstrainedType;
    }

    @Override
    public void visitProcedureGroupType(@NotNull OdinProcedureGroupType o) {
        TsOdinProcedureGroup tsOdinProcedureGroup = new TsOdinProcedureGroup();
        initializeNamedType(tsOdinProcedureGroup);

        for (OdinProcedureRef procedureRef : o.getProcedureRefList()) {
            TsOdinType tsOdinType = doResolveType(context, procedureRef.getType());
            if (tsOdinType instanceof TsOdinProcedureType tsOdinProcedureType) {
                tsOdinProcedureGroup.getProcedures().add(tsOdinProcedureType);
            }
        }

        this.type = tsOdinProcedureGroup;
    }

    @Override
    public void visitVariadicType(@NotNull OdinVariadicType o) {
        OdinType psiElementType = o.getType();
        if (psiElementType != null) {
            TsOdinType tsOdinElementType = doResolveType(context, psiElementType);
            TsOdinSliceType tsOdinSliceType = new TsOdinSliceType();
            tsOdinSliceType.setElementType(tsOdinElementType);
            tsOdinSliceType.setContext(context);
            tsOdinSliceType.setPsiType(o);
            this.type = tsOdinSliceType;
        }
        super.visitVariadicType(o);
    }
}
