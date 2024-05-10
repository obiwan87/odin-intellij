package com.lasagnerd.odin.insights.typeInference;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.insights.OdinInsightUtils;
import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.insights.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine.doInferType;
import static com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine.inferType;
import static com.lasagnerd.odin.lang.OdinLangSyntaxAnnotator.RESERVED_TYPES;

@EqualsAndHashCode(callSuper = true)
@Data
public class OdinTypeResolver extends OdinVisitor {

    public static @NotNull TsOdinType resolveType(OdinScope scope, @NotNull OdinType type) {
        return resolveType(0, scope, null, null, type);
    }

    public static @NotNull TsOdinType resolveType(OdinScope scope,
                                                  OdinDeclaredIdentifier declaredIdentifier,
                                                  OdinDeclaration declaration,
                                                  @NotNull OdinType type) {
        return resolveType(0, scope, declaredIdentifier, declaration, type);
    }

    public static @NotNull TsOdinType resolveType(int level,
                                                  OdinScope scope,
                                                  OdinDeclaredIdentifier declaredIdentifier,
                                                  OdinDeclaration declaration,
                                                  OdinType type) {
        OdinTypeResolver typeResolver = new OdinTypeResolver(level, scope, declaration, declaredIdentifier);
        type.accept(typeResolver);
        return Objects.requireNonNullElse(typeResolver.type, TsOdinType.UNKNOWN);
    }

    public static @NotNull TsOdinMetaType findMetaType(OdinScope scope,
                                                       @NotNull OdinType type) {
        return findMetaType(scope, null, null, type);
    }

    public static @NotNull TsOdinMetaType findMetaType(OdinScope scope,
                                                       OdinDeclaredIdentifier declaredIdentifier,
                                                       OdinDeclaration declaration,
                                                       @NotNull OdinType type) {
        if (type instanceof OdinQualifiedType || type instanceof OdinSimpleRefType) {
            TsOdinType tsOdinType = resolveType(scope, declaredIdentifier, declaration, type);
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(tsOdinType.getMetaType());
            tsOdinMetaType.setName(tsOdinType.getName());
            tsOdinMetaType.setDeclaredIdentifier(tsOdinMetaType.getDeclaredIdentifier());
            tsOdinMetaType.setDeclaration(tsOdinType.getDeclaration());
            tsOdinMetaType.setScope(tsOdinType.getScope());
            tsOdinMetaType.setRepresentedType(tsOdinType);
            return tsOdinMetaType;
        }

        TsOdinMetaType tsOdinMetaType = OdinMetaTypeResolver.resolveMetaType(type);
        tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
        tsOdinMetaType.setDeclaration(declaration);
        tsOdinMetaType.setName(declaredIdentifier != null ? declaredIdentifier.getName() : null);
        return tsOdinMetaType;
    }

    public static @NotNull TsOdinType resolveMetaType(OdinScope scope, TsOdinMetaType metaType) {
        return resolveMetaType(0, scope, metaType);
    }

    public static TsOdinType resolveMetaType(int level, OdinScope scope, TsOdinMetaType metaType) {
        if (metaType.getRepresentedMetaType() == TsOdinMetaType.MetaType.BUILTIN) {
            return TsOdinBuiltInType.getBuiltInType(metaType.getName());
        } else if (metaType.getType() != null) {
            OdinDeclaredIdentifier declaredIdentifier = metaType.getDeclaredIdentifier();
            TsOdinType tsOdinType = resolveType(level, scope, declaredIdentifier, metaType.getDeclaration(), metaType.getType());
            tsOdinType.setDeclaration(metaType.getDeclaration());

//            tsOdinType.setScope(metaType.getScope());
            tsOdinType.setDeclaredIdentifier(declaredIdentifier);
            scope.addKnownType(declaredIdentifier, tsOdinType);
            if (declaredIdentifier != null) {
                tsOdinType.setName(declaredIdentifier.getName());
            }
            tsOdinType.getScope().putAll(scope);

            return tsOdinType;
        }
        return TsOdinType.UNKNOWN;
    }

    // Result
    TsOdinType type;

    private final int level;
    private final OdinScope scope;
    private final OdinDeclaration typeDeclaration;
    private final OdinDeclaredIdentifier typeDeclaredIdentifier;

    // avoid stackoverflow when encountering circular references
    private Set<OdinDeclaredIdentifier> visitedDeclaredIdentifiers = new HashSet<>();


    public OdinTypeResolver(int level, OdinScope scope, OdinDeclaration typeDeclaration, OdinDeclaredIdentifier typeDeclaredIdentifier) {
        this.level = level;
        this.scope = scope;
        this.typeDeclaration = typeDeclaration;
        this.typeDeclaredIdentifier = typeDeclaredIdentifier;
    }

    // resolve type calls
    public @NotNull TsOdinType doResolveType(OdinScope scope,
                                             OdinDeclaredIdentifier declaredIdentifier,
                                             OdinDeclaration declaration,
                                             @NotNull OdinType type) {
        return resolveType(level + 1, scope, declaredIdentifier, declaration, type);
    }

    public @NotNull TsOdinType doResolveType(OdinScope scope,
                                             @NotNull OdinType type) {
        return resolveType(level + 1, scope, null, null, type);
    }

    public @NotNull TsOdinType doResolveMetaType(OdinScope scope, TsOdinMetaType metaType) {
        return resolveMetaType(level + 1, scope, metaType);
    }

    public @NotNull TsOdinType doResolveType(OdinScope scope, OdinExpression odinExpression) {
        TsOdinType tsOdinType = doInferType(scope, odinExpression);
        if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
            return doResolveMetaType(scope, tsOdinMetaType);
        }
        return tsOdinType;
    }

    // logging
    @SuppressWarnings("unused")
    public void log(String message) {
//        System.out.println("\t".repeat(level) + message);
    }

    private List<TsOdinParameter> createParameters(List<OdinParamEntry> paramEntries, OdinScope localScope) {
        List<TsOdinParameter> typeParameters = new ArrayList<>();
        int k = 0;
        for (var paramEntry : paramEntries) {
            OdinParameterDeclaration parameterDeclaration = paramEntry.getParameterDeclaration();

            // First, add all $Identifier expressions we encounter in this parameter to the current scope
            for (OdinPolymorphicType odinPolymorphicType : PsiTreeUtil.findChildrenOfType(paramEntry, OdinPolymorphicType.class)) {
                TsOdinType tsOdinType = doResolveType(localScope, odinPolymorphicType);
                localScope.addType(tsOdinType.getName(), tsOdinType);
                localScope.add(odinPolymorphicType.getDeclaredIdentifier());
            }

            for (OdinDeclaredIdentifier declaredIdentifier : PsiTreeUtil.findChildrenOfType(paramEntry, OdinDeclaredIdentifier.class)) {
                if (declaredIdentifier.getDollar() != null) {
                    TsOdinPolymorphicType valuePolymorphicType = new TsOdinPolymorphicType();
                    String name = declaredIdentifier.getName();
                    valuePolymorphicType.setName(name);
                    valuePolymorphicType.setDeclaredIdentifier(declaredIdentifier);
                    localScope.addType(valuePolymorphicType.getName(), valuePolymorphicType);
                    localScope.add(declaredIdentifier);
                }
            }

            List<TsOdinParameterSpec> declarationSpecs = TsOdinParameterSpec.from(parameterDeclaration);
            for (var declarationSpec : declarationSpecs) {
                TsOdinParameter tsOdinParameter = mapSpecToParameter(localScope, declarationSpec, k);
                typeParameters.add(tsOdinParameter);
                k++;
            }
        }

        return typeParameters;
    }

    private void initializeNamedType(TsOdinType tsOdinType) {

        tsOdinType.setDeclaredIdentifier(typeDeclaredIdentifier);
        String name = typeDeclaredIdentifier != null ? typeDeclaredIdentifier.getName() : null;
        tsOdinType.setName(name);
        tsOdinType.setDeclaration(typeDeclaration);
        if (typeDeclaredIdentifier != null) {
            this.scope.addKnownType(typeDeclaredIdentifier, tsOdinType);
        }
        tsOdinType.getScope().putAll(scope);
        tsOdinType.getScope().setPackagePath(scope.getPackagePath());
        log("Initialized " + tsOdinType.getClass().getSimpleName() + " with name " + name);
    }

    private void resolveIdentifier(OdinIdentifier typeIdentifier) {
        PsiNamedElement declaration;
        String identifierText = typeIdentifier.getText();
        if (RESERVED_TYPES.contains(identifierText)) {
            type = TsOdinBuiltInType.getBuiltInType(identifierText);

        } else {
            TsOdinType scopeType = scope.getType(typeIdentifier.getIdentifierToken().getText());
            if (scopeType != null) {
                type = scopeType;
            } else {
                declaration = scope.getNamedElement(typeIdentifier.getIdentifierToken().getText());
                if (!(declaration instanceof OdinDeclaredIdentifier declaredIdentifier)) {
                    return;
                }
                var knownType = scope.getKnownTypes().get(declaredIdentifier);
                if (knownType != null) {
                    log("Cache hit for type: " + knownType.getLabel());
                    this.type = knownType;
                } else {
                    type = resolveTypeFromDeclaredIdentifier(scope, declaredIdentifier);
                }
            }
        }
    }

    private TsOdinType resolveTypeFromDeclaredIdentifier(OdinScope scope, OdinDeclaredIdentifier identifier) {
        OdinDeclaration odinDeclaration = OdinInsightUtils.findFirstParentOfType(identifier,
                false,
                OdinDeclaration.class);

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            return doResolveType(scope, identifier, odinDeclaration, structDeclarationStatement.getStructType());
        } else if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            return doResolveType(scope, identifier, odinDeclaration, enumDeclarationStatement.getEnumType());
        } else if (odinDeclaration instanceof OdinUnionDeclarationStatement unionDeclarationStatement) {
            return doResolveType(scope, identifier, odinDeclaration, unionDeclarationStatement.getUnionType());
        } else if (odinDeclaration instanceof OdinProcedureDeclarationStatement procedureDeclarationStatement) {
            return doResolveType(scope, identifier, odinDeclaration, procedureDeclarationStatement.getProcedureType());
        } else if (odinDeclaration instanceof OdinConstantInitializationStatement constantInitializationStatement) {
            List<OdinExpression> expressionList = constantInitializationStatement.getExpressionsList().getExpressionList();
            if (!expressionList.isEmpty()) {
                int index = constantInitializationStatement.getDeclaredIdentifiers().indexOf(identifier);
                if (index == -1) {
                    return TsOdinType.UNKNOWN;
                }
                if (expressionList.size() <= index) {
                    return TsOdinType.UNKNOWN;
                }

                OdinExpression odinExpression = expressionList.get(index);
                OdinTypeInferenceResult typeInferenceResult = inferType(scope, odinExpression);
                if (typeInferenceResult.getType() instanceof TsOdinMetaType metaType) {
                    return doResolveMetaType(scope, metaType);
                }
                return typeInferenceResult.getType();
            }
        } else if (odinDeclaration instanceof OdinPolymorphicType polymorphicType) {
            return doResolveType(scope, identifier, odinDeclaration, polymorphicType);
        } else if (odinDeclaration instanceof OdinBitsetDeclarationStatement odinBitsetDeclarationStatement) {
            return doResolveType(scope, odinBitsetDeclarationStatement.getBitSetType());
        }

        return TsOdinType.UNKNOWN;
    }

    // Visitor methods
    @Override
    public void visitQualifiedType(@NotNull OdinQualifiedType qualifiedType) {

        if (qualifiedType.getPackageIdentifier() != null) {
            OdinScope packageScope = scope.getScopeOfImport(qualifiedType.getPackageIdentifier().getIdentifierToken().getText());

            this.type = doResolveType(packageScope, qualifiedType.getType());
            return;
        }

        OdinIdentifier typeIdentifier = qualifiedType.getTypeIdentifier();
        resolveIdentifier(typeIdentifier);
    }

    @Override
    public void visitSimpleRefType(@NotNull OdinSimpleRefType o) {
        OdinIdentifier identifier = o.getIdentifier();
        resolveIdentifier(identifier);
    }

    @Override
    public void visitCallType(@NotNull OdinCallType o) {
        OdinIdentifier identifier = o.getIdentifier();
        resolveIdentifier(identifier);

        if (this.type instanceof TsOdinStructType structType) {
            this.type = OdinTypeInstantiator.instantiateStruct(scope, o.getArgumentList(), structType);
        }

        if (this.type instanceof TsOdinUnionType unionType) {
            this.type = OdinTypeInstantiator.instantiateUnion(scope, o.getArgumentList(), unionType);
        }
    }

    @Override
    public void visitMatrixType(@NotNull OdinMatrixType o) {
        OdinExpression typeExpression = o.getExpressionList().get(o.getExpressionList().size() - 1);
        if (typeExpression instanceof OdinTypeDefinitionExpression typeDefinitionExpression) {
            this.type = doResolveType(scope, typeDefinitionExpression.getType());
        }
    }

    @Override
    public void visitMultiPointerType(@NotNull OdinMultiPointerType o) {
        TsOdinMultiPointerType tsOdinMultiPointerType = new TsOdinMultiPointerType();
        tsOdinMultiPointerType.setType(o);
        TsOdinType dereferencedType = resolveType(scope, Objects.requireNonNull(o.getType()));
        tsOdinMultiPointerType.setDereferencedType(dereferencedType);

        this.type = tsOdinMultiPointerType;
    }

    @Override
    public void visitBitSetType(@NotNull OdinBitSetType o) {
        TsOdinBitSetType tsOdinBitSetType = new TsOdinBitSetType();
        OdinExpression elementTypeExpression = o.getExpressionList().get(0);
        TsOdinType tsOdinElementType = doResolveType(scope, elementTypeExpression);
        tsOdinBitSetType.setElementType(tsOdinElementType);

        if (o.getExpressionList().size() > 1) {
            TsOdinType tsBackingType = doResolveType(scope, ((OdinTypeDefinitionExpression) o.getExpressionList().get(1)).getType());
            tsOdinBitSetType.setBackingType(tsBackingType);
        }
        this.type = tsOdinBitSetType;

    }

    @Override
    public void visitUnionType(@NotNull OdinUnionType unionType) {
        TsOdinUnionType tsOdinUnionType = new TsOdinUnionType();
        tsOdinUnionType.setType(unionType);
        initializeNamedType(tsOdinUnionType);

        List<TsOdinParameter> parameters = createParameters(unionType.getParamEntryList(), tsOdinUnionType.getScope());
        tsOdinUnionType.setParameters(parameters);

        OdinUnionBody unionBody = unionType.getUnionBlock().getUnionBody();
        if (unionBody != null) {
            List<OdinTypeDefinitionExpression> typeDefinitionExpressions = unionBody.getTypeDefinitionExpressionList();
            for (OdinTypeDefinitionExpression typeDefinitionExpression : typeDefinitionExpressions) {
                TsOdinType tsOdinType = doResolveType(tsOdinUnionType.getScope(), typeDefinitionExpression.getType());

                TsOdinUnionVariant tsOdinUnionVariant = new TsOdinUnionVariant();
                tsOdinUnionVariant.setTypeDefinitionExpression(typeDefinitionExpression);
                tsOdinUnionVariant.setType(tsOdinType);
                tsOdinUnionType.getVariants().add(tsOdinUnionVariant);
            }
        }
        this.type = tsOdinUnionType;
    }

    @Override
    public void visitArrayType(@NotNull OdinArrayType arrayType) {
        TsOdinArrayType tsOdinArrayType = new TsOdinArrayType();
        tsOdinArrayType.setType(arrayType);
        TsOdinType elementType = resolveType(scope, arrayType.getTypeDefinition());
        tsOdinArrayType.setElementType(elementType);

        this.type = tsOdinArrayType;
    }

    @Override
    public void visitMapType(@NotNull OdinMapType mapType) {
        TsOdinMapType tsOdinMapType = new TsOdinMapType();
        tsOdinMapType.setType(mapType);
        TsOdinType keyType = doResolveType(scope, mapType.getKeyType());
        tsOdinMapType.setKeyType(keyType);

        TsOdinType valueType = doResolveType(scope, mapType.getValueType());
        tsOdinMapType.setValueType(valueType);
        this.type = tsOdinMapType;
    }

    @Override
    public void visitPointerType(@NotNull OdinPointerType odinPointerType) {
        TsOdinPointerType tsOdinPointerType = new TsOdinPointerType();
        tsOdinPointerType.setType(odinPointerType);

        TsOdinType elementType = doResolveType(scope, Objects.requireNonNull(odinPointerType.getType()));
        tsOdinPointerType.setDereferencedType(elementType);

        this.type = tsOdinPointerType;
    }

    @Override
    public void visitProcedureType(@NotNull OdinProcedureType procedureType) {
        TsOdinProcedureType tsOdinProcedureType = new TsOdinProcedureType();
        tsOdinProcedureType.setType(procedureType);
        initializeNamedType(tsOdinProcedureType);

        List<TsOdinParameter> parameters = createParameters(procedureType.getParamEntryList(), tsOdinProcedureType.getScope());
        tsOdinProcedureType.setParameters(parameters);

        OdinReturnParameters returnParameters = procedureType.getReturnParameters();
        if (returnParameters != null) {
            // Single return value
            OdinTypeDefinitionExpression typeDefinitionExpression = returnParameters.getTypeDefinitionExpression();
            if (typeDefinitionExpression != null) {
                OdinType typeExpression = typeDefinitionExpression.getType();
                TsOdinType tsOdinType = doResolveType(tsOdinProcedureType.getScope(), typeExpression);

                TsOdinParameter tsOdinParameter = new TsOdinParameter();
                tsOdinParameter.setType(tsOdinType);
                tsOdinParameter.setTypeDefinitionExpression(typeDefinitionExpression);
                tsOdinParameter.setIndex(0);

                tsOdinProcedureType.setReturnTypes(List.of(tsOdinType));
                tsOdinProcedureType.setReturnParameters(List.of(tsOdinParameter));
            } else {
                List<OdinParamEntry> returnParameterEntries = returnParameters.getParamEntryList();
                List<TsOdinParameter> tsOdinReturnParameters = createParameters(returnParameterEntries, tsOdinProcedureType.getScope());
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
        tsOdinStructType.setType(structType);
        initializeNamedType(tsOdinStructType);

        List<OdinParamEntry> paramEntries = structType.getParamEntryList();

        List<TsOdinParameter> parameters = createParameters(paramEntries, tsOdinStructType.getScope());
        tsOdinStructType.setParameters(parameters);

        this.type = tsOdinStructType;
    }

    @Data
    private static class TsOdinParameterSpec {
        OdinDeclaredIdentifier valueDeclaredIdentifier;
        OdinExpression valueExpression;
        OdinTypeDefinitionExpression typeDefinitionExpression;

        boolean isVariadic;

        boolean isValuePolymorphic() {
            return valueDeclaredIdentifier != null && valueDeclaredIdentifier.getDollar() != null;
        }

        static List<TsOdinParameterSpec> from(OdinParameterDeclaration parameterDeclaration) {

            if (parameterDeclaration instanceof OdinParameterInitialization odinParameterInitialization) {
                TsOdinParameterSpec tsOdinParameterSpec = new TsOdinParameterSpec();
                tsOdinParameterSpec.setTypeDefinitionExpression(odinParameterInitialization.getTypeDefinition());
                tsOdinParameterSpec.setValueExpression(odinParameterInitialization.getExpression());
                tsOdinParameterSpec.setValueDeclaredIdentifier(odinParameterInitialization.getParameter().getDeclaredIdentifier());
                return List.of(tsOdinParameterSpec);
            }

            if (parameterDeclaration instanceof OdinParameterDecl odinParameterDecl) {
                List<TsOdinParameterSpec> parameterSpecs = new ArrayList<>();
                for (OdinParameter odinParameter : parameterDeclaration.getParameterList()) {
                    TsOdinParameterSpec tsOdinParameterSpec = new TsOdinParameterSpec();
                    tsOdinParameterSpec.setTypeDefinitionExpression(odinParameterDecl.getTypeDefinition());
                    tsOdinParameterSpec.setValueDeclaredIdentifier(odinParameter.getDeclaredIdentifier());
                    parameterSpecs.add(tsOdinParameterSpec);
                }
                return parameterSpecs;
            }

            if (parameterDeclaration instanceof OdinUnnamedParameter unnamedParameter) {
                TsOdinParameterSpec tsOdinParameterSpec = new TsOdinParameterSpec();
                tsOdinParameterSpec.setTypeDefinitionExpression(unnamedParameter.getTypeDefinition());
                return Collections.singletonList(tsOdinParameterSpec);
            }

            return Collections.emptyList();
        }
    }

    private @NotNull TsOdinParameter mapSpecToParameter(OdinScope scope, TsOdinParameterSpec parameterSpec, int parameterIndex) {
        TsOdinParameter tsOdinParameter = new TsOdinParameter();
        tsOdinParameter.setValueDeclaredIdentifier(parameterSpec.getValueDeclaredIdentifier());
        if (parameterSpec.getValueDeclaredIdentifier() != null) {
            tsOdinParameter.setValueName(parameterSpec.getValueDeclaredIdentifier().getName());
        }
        tsOdinParameter.setValuePolymorphic(parameterSpec.isValuePolymorphic());
        tsOdinParameter.setIndex(parameterIndex);
        if (parameterSpec.getTypeDefinitionExpression() != null) {
            TsOdinType tsOdinType = doResolveType(scope, parameterSpec.getTypeDefinitionExpression().getType());
            tsOdinParameter.setType(tsOdinType);
        }
        tsOdinParameter.setTypeDefinitionExpression(parameterSpec.typeDefinitionExpression);


        return tsOdinParameter;
    }

    @Override
    public void visitEnumType(@NotNull OdinEnumType o) {
        TsOdinEnumType tsOdinEnumType = new TsOdinEnumType();
        initializeNamedType(tsOdinEnumType);

        if (o.getType() != null) {
            TsOdinType tsOdinType = doResolveType(scope, o.getType());
            if (tsOdinType instanceof TsOdinBuiltInType tsOdinBuiltInType) {
                tsOdinEnumType.setBackingType(tsOdinBuiltInType);
            }
        }

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

        tsOdinPolymorphicType.setScope(scope);

        tsOdinPolymorphicType.setType(polymorphicType);
        this.type = tsOdinPolymorphicType;
    }

    @Override
    public void visitConstrainedType(@NotNull OdinConstrainedType constrainedType) {
        OdinType mainType = constrainedType.getTypeList().get(0);
        OdinType specializedType = constrainedType.getTypeList().get(1);
        TsOdinConstrainedType tsOdinConstrainedType = new TsOdinConstrainedType();
        tsOdinConstrainedType.setType(constrainedType);
        initializeNamedType(tsOdinConstrainedType);

        TsOdinType tsOdinMainType = doResolveType(tsOdinConstrainedType.getScope(), mainType);
        TsOdinType tsOdinSpecializedType = doResolveType(tsOdinConstrainedType.getScope(), specializedType);

        tsOdinConstrainedType.setMainType(tsOdinMainType);
        tsOdinConstrainedType.setSpecializedType(tsOdinSpecializedType);

        this.type = tsOdinConstrainedType;
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
            tsOdinMetaType.setType(type);

            return tsOdinMetaType;
        }

        @Override
        public void visitArrayType(@NotNull OdinArrayType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.ARRAY);
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
        public void visitQualifiedType(@NotNull OdinQualifiedType o) {

        }
    }
}
