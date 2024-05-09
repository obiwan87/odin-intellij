package com.lasagnerd.odin.insights.typeInference;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.insights.OdinInsightUtils;
import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.insights.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine.inferType;
import static com.lasagnerd.odin.lang.OdinLangSyntaxAnnotator.RESERVED_TYPES;

@EqualsAndHashCode(callSuper = true)
@Data
public class OdinTypeResolver extends OdinVisitor {

    public static @NotNull TsOdinType resolveType(OdinScope scope, @NotNull OdinType type) {
        OdinTypeResolver typeResolver = new OdinTypeResolver(scope);
        type.accept(typeResolver);
        return Objects.requireNonNullElse(typeResolver.type, TsOdinType.UNKNOWN);
    }

    public static @NonNull TsOdinType resolveType(OdinScope scope, OdinDeclaredIdentifier declaredIdentifier, OdinDeclaration declaration, @NotNull OdinType type) {
        OdinTypeResolver typeResolver = new OdinTypeResolver(scope, declaration, declaredIdentifier);
        type.accept(typeResolver);
        return Objects.requireNonNullElse(typeResolver.type, TsOdinType.UNKNOWN);
    }

    private static List<TsOdinParameter> createParameters(List<OdinParamEntry> paramEntries, OdinScope localScope) {
        List<TsOdinParameter> typeParameters = new ArrayList<>();
        int k = 0;
        for (var paramEntry : paramEntries) {
            OdinParameterDeclaration parameterDeclaration = paramEntry.getParameterDeclaration();

            // First, add all $Identifier expressions we encounter in this parameter to the current scope
            for (OdinPolymorphicType odinPolymorphicType : PsiTreeUtil.findChildrenOfType(paramEntry, OdinPolymorphicType.class)) {
                TsOdinType tsOdinType = resolveType(localScope, odinPolymorphicType);
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

    @Data
    static class Parameter {
        OdinDeclaredIdentifier identifier;
        OdinType type;
    }

    // Result
    TsOdinType type;

    private final OdinScope scope;
    private final OdinDeclaration typeDeclaration;
    private final OdinDeclaredIdentifier typeDeclaredIdentifier;

    public OdinTypeResolver(OdinScope scope) {
        this.scope = scope;
        this.typeDeclaredIdentifier = null;
        this.typeDeclaration = null;
    }

    public OdinTypeResolver(OdinScope scope, OdinDeclaration typeDeclaration, OdinDeclaredIdentifier typeDeclaredIdentifier) {
        this.scope = scope;
        this.typeDeclaration = typeDeclaration;
        this.typeDeclaredIdentifier = typeDeclaredIdentifier;
    }

    private void setTypeDeclaration(TsOdinType tsOdinType) {
        tsOdinType.setDeclaredIdentifier(typeDeclaredIdentifier);
        tsOdinType.setName(typeDeclaredIdentifier != null? typeDeclaredIdentifier.getName() :  null);
        tsOdinType.setDeclaration(typeDeclaration);
    }

    @Override
    public void visitQualifiedType(@NotNull OdinQualifiedType qualifiedType) {

        if (qualifiedType.getPackageIdentifier() != null) {
            OdinScope packageScope = scope.getScopeOfImport(qualifiedType.getPackageIdentifier().getIdentifierToken().getText());

            this.type = resolveType(packageScope, qualifiedType);
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

        if(this.type instanceof TsOdinUnionType unionType) {
            this.type = OdinTypeInstantiator.instantiateUnion(scope, o.getArgumentList(), unionType);
        }
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
                type = resolveTypeFromDeclaredIdentifier(scope, declaredIdentifier);
            }
        }
    }

    @Override
    public void visitUnionType(@NotNull OdinUnionType unionType) {
        TsOdinUnionType tsOdinUnionType = new TsOdinUnionType();
        tsOdinUnionType.setType(unionType);
        tsOdinUnionType.getScope().putAll(scope);
        setTypeDeclaration(tsOdinUnionType);

        List<TsOdinParameter> parameters = createParameters(unionType.getParamEntryList(), tsOdinUnionType.getScope());
        tsOdinUnionType.setParameters(parameters);

        OdinUnionBody unionBody = unionType.getUnionBlock().getUnionBody();
        if (unionBody != null) {
            List<OdinTypeDefinitionExpression> typeDefinitionExpressions = unionBody.getTypeDefinitionExpressionList();
            for (OdinTypeDefinitionExpression typeDefinitionExpression : typeDefinitionExpressions) {
                TsOdinType tsOdinType = resolveType(tsOdinUnionType.getScope(), typeDefinitionExpression.getType());

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
        TsOdinType keyType = resolveType(scope, mapType.getKeyType());
        tsOdinMapType.setKeyType(keyType);

        TsOdinType valueType = resolveType(scope, mapType.getValueType());
        tsOdinMapType.setValueType(valueType);
        this.type = tsOdinMapType;
    }

    @Override
    public void visitPointerType(@NotNull OdinPointerType odinPointerType) {
        TsOdinPointerType tsOdinPointerType = new TsOdinPointerType();
        tsOdinPointerType.setType(odinPointerType);

        TsOdinType elementType = resolveType(scope, Objects.requireNonNull(odinPointerType.getType()));
        tsOdinPointerType.setDereferencedType(elementType);

        this.type = tsOdinPointerType;
    }

    @Override
    public void visitProcedureType(@NotNull OdinProcedureType procedureType) {
        TsOdinProcedureType tsOdinProcedureType = new TsOdinProcedureType();
        tsOdinProcedureType.setType(procedureType);
        tsOdinProcedureType.getScope().putAll(scope);
        setTypeDeclaration(tsOdinProcedureType);

        List<TsOdinParameter> parameters = createParameters(procedureType.getParamEntryList(), tsOdinProcedureType.getScope());
        tsOdinProcedureType.setParameters(parameters);

        OdinReturnParameters returnParameters = procedureType.getReturnParameters();
        if (returnParameters != null) {
            // Single return value
            OdinTypeDefinitionExpression typeDefinitionExpression = returnParameters.getTypeDefinitionExpression();
            if (typeDefinitionExpression != null) {
                OdinType typeExpression = typeDefinitionExpression.getType();
                TsOdinType tsOdinType = resolveType(tsOdinProcedureType.getScope(), typeExpression);

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
        tsOdinStructType.getScope().putAll(scope);
        setTypeDeclaration(tsOdinStructType);

        List<OdinParamEntry> paramEntries = structType.getParamEntryList();

        List<TsOdinParameter> parameters = createParameters(paramEntries, tsOdinStructType.getScope());
        tsOdinStructType.setParameters(parameters);

        for (OdinFieldDeclarationStatement field : OdinInsightUtils.getStructFieldsDeclarationStatements(structType)) {
            TsOdinType tsFieldType = resolveType(tsOdinStructType.getScope(), field.getTypeDefinition().getType());
            for (OdinDeclaredIdentifier declaredIdentifier : field.getDeclaredIdentifiers()) {
                tsOdinStructType.getFields().put(declaredIdentifier.getName(), tsFieldType);
            }
        }

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

    private static @NotNull TsOdinParameter mapSpecToParameter(OdinScope scope, TsOdinParameterSpec parameterSpec, int parameterIndex) {
        TsOdinParameter tsOdinParameter = new TsOdinParameter();
        tsOdinParameter.setValueDeclaredIdentifier(parameterSpec.getValueDeclaredIdentifier());
        if(parameterSpec.getValueDeclaredIdentifier() != null) {
            tsOdinParameter.setValueName(parameterSpec.getValueDeclaredIdentifier().getName());
        }
        tsOdinParameter.setValuePolymorphic(parameterSpec.isValuePolymorphic());
        tsOdinParameter.setIndex(parameterIndex);
        if (parameterSpec.getTypeDefinitionExpression() != null) {
            TsOdinType tsOdinType = resolveType(scope, parameterSpec.getTypeDefinitionExpression().getType());
            tsOdinParameter.setType(tsOdinType);
        }
        tsOdinParameter.setTypeDefinitionExpression(parameterSpec.typeDefinitionExpression);


        return tsOdinParameter;
    }

    @Override
    public void visitEnumType(@NotNull OdinEnumType o) {
        TsOdinEnumType enumType = new TsOdinEnumType();
        enumType.setScope(scope);
        setTypeDeclaration(enumType);
        // TODO Set fields

    }

    @Override
    public void visitPolymorphicType(@NotNull OdinPolymorphicType polymorphicType) {
        TsOdinPolymorphicType tsOdinPolymorphicType = new TsOdinPolymorphicType();
        tsOdinPolymorphicType.setScope(scope);
        tsOdinPolymorphicType.setName(polymorphicType.getDeclaredIdentifier().getIdentifierToken().getText());
        tsOdinPolymorphicType.setDeclaredIdentifier(polymorphicType.getDeclaredIdentifier());
        tsOdinPolymorphicType.setDeclaration(polymorphicType);
        tsOdinPolymorphicType.setType(polymorphicType);
        this.type = tsOdinPolymorphicType;
    }

    @Override
    public void visitConstrainedType(@NotNull OdinConstrainedType constrainedType) {
        OdinType mainType = constrainedType.getTypeList().get(0);
        OdinType specializedType = constrainedType.getTypeList().get(1);

        // TODO

        super.visitConstrainedType(constrainedType);
    }

    private TsOdinType resolveTypeFromDeclaredIdentifier(OdinScope scope, OdinDeclaredIdentifier identifier) {
        OdinDeclaration odinDeclaration = OdinInsightUtils.findFirstParentOfType(identifier,
                false,
                OdinDeclaration.class);

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            return resolveType(scope, identifier,  odinDeclaration, structDeclarationStatement.getStructType());
        }

        else if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            return resolveType(scope, identifier, odinDeclaration, enumDeclarationStatement.getEnumType());
        }

        else if (odinDeclaration instanceof OdinUnionDeclarationStatement unionDeclarationStatement) {
            return resolveType(scope, identifier, odinDeclaration, unionDeclarationStatement.getUnionType());
        }

        else if (odinDeclaration instanceof OdinProcedureDeclarationStatement procedureDeclarationStatement) {
            return resolveType(scope, identifier, odinDeclaration, procedureDeclarationStatement.getProcedureType());
        }

        else if (odinDeclaration instanceof OdinConstantInitializationStatement constantInitializationStatement) {
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
                   return resolveMetaType(scope, identifier, odinDeclaration, metaType);
                }
            }
        }

        else if (odinDeclaration instanceof OdinPolymorphicType polymorphicType) {
            return resolveType(scope, identifier, odinDeclaration, polymorphicType);
        }

        return TsOdinType.UNKNOWN;
    }

    public static TsOdinType resolveMetaType(OdinScope scope, TsOdinMetaType metaType) {
        return resolveMetaType(scope, null, null, metaType);
    }

    public static TsOdinType resolveMetaType(OdinScope scope, OdinDeclaredIdentifier declaredIdentifier, OdinDeclaration declaration, TsOdinMetaType metaType) {
        if (metaType.getMetaType() == TsOdinMetaType.MetaType.BUILTIN) {
            return TsOdinBuiltInType.getBuiltInType(metaType.getName());
        } else if (metaType.getType() != null) {
            TsOdinType tsOdinType = resolveType(scope, declaredIdentifier, declaration, metaType.getType());
            tsOdinType.setDeclaration(metaType.getDeclaration());
            tsOdinType.setName(metaType.getName());
//            tsOdinType.setScope(metaType.getScope());
            tsOdinType.setDeclaredIdentifier(metaType.getDeclaredIdentifier());
            return tsOdinType;
        }
        return TsOdinType.UNKNOWN;
    }


}
