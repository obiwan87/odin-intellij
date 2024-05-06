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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine.inferType;
import static com.lasagnerd.odin.lang.OdinLangSyntaxAnnotator.RESERVED_TYPES;

@EqualsAndHashCode(callSuper = true)
@Data
public class OdinTypeResolver extends OdinVisitor {

    public static @NotNull TsOdinType resolveType(OdinScope scope, OdinType type) {
        OdinTypeResolver typeResolver = new OdinTypeResolver(scope);
        type.accept(typeResolver);
        return Objects.requireNonNullElse(typeResolver.type, TsOdinType.UNKNOWN);
    }

    @Data
    static class Parameter {
        OdinDeclaredIdentifier identifier;
        OdinType type;
    }

    OdinScope scope;
    private final OdinScope initialScope;
    TsOdinType type;

    public OdinTypeResolver(OdinScope scope) {
        this.initialScope = scope;
        this.scope = scope;
    }

    @Override
    public void visitQualifiedType(@NotNull OdinQualifiedType qualifiedType) {

        if (qualifiedType.getPackageIdentifier() != null) {
            OdinScope packageScope = scope.getScopeOfImport(qualifiedType.getPackageIdentifier().getIdentifierToken().getText());
            OdinTypeResolver odinTypeExpressionResolver = new OdinTypeResolver(packageScope);
            OdinType typeExpression = qualifiedType.getType();
            typeExpression.accept(odinTypeExpressionResolver);
            this.type = odinTypeExpressionResolver.type;
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
            OdinScope localScope = structType.getScope();
            this.type = OdinTypeInstantiator.instantiateStruct(scope, o.getArgumentList(), structType);
        }
    }

    private void resolveIdentifier(OdinIdentifier typeIdentifier) {
        PsiNamedElement declaration;
        String identifierText = typeIdentifier.getText();
        if (RESERVED_TYPES.contains(identifierText)) {
            type = new TsOdinBuiltInType();
            type.setName(identifierText);
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
    public void visitUnionType(@NotNull OdinUnionType o) {
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
        OdinTypeResolver keyOdinTypeExpressionResolver = new OdinTypeResolver(scope);
        mapType.getKeyType().accept(keyOdinTypeExpressionResolver);
        TsOdinType keyType = keyOdinTypeExpressionResolver.type;
        tsOdinMapType.setKeyType(keyType);

        OdinTypeResolver valueOdinTypeExpressionResolver = new OdinTypeResolver(scope);
        mapType.getValueType().accept(valueOdinTypeExpressionResolver);
        TsOdinType valueType = valueOdinTypeExpressionResolver.type;
        tsOdinMapType.setValueType(valueType);
        this.type = tsOdinMapType;
    }

    @Override
    public void visitPointerType(@NotNull OdinPointerType odinPointerType) {
        TsOdinPointerType tsOdinPointerType = new TsOdinPointerType();
        tsOdinPointerType.setType(odinPointerType);
        OdinTypeResolver odinTypeExpressionResolver = new OdinTypeResolver(scope);
        var typeExpression = odinPointerType.getType();

        Objects.requireNonNull(typeExpression)
                .accept(odinTypeExpressionResolver);

        TsOdinType elementType = odinTypeExpressionResolver.type;
        tsOdinPointerType.setDereferencedType(elementType);

        this.type = tsOdinPointerType;
    }

    @Override
    public void visitProcedureType(@NotNull OdinProcedureType procedureType) {
        TsOdinProcedureType tsOdinProcedureType = new TsOdinProcedureType();
        tsOdinProcedureType.setType(procedureType);
        tsOdinProcedureType.getScope().putAll(scope);

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
            structDeclarationStatement.getStructType().accept(this);
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            enumDeclarationStatement.getEnumType().accept(this);
        }

        if (odinDeclaration instanceof OdinUnionDeclarationStatement unionDeclarationStatement) {
            unionDeclarationStatement.getUnionType().accept(this);
        }

        if (odinDeclaration instanceof OdinProcedureDeclarationStatement procedureDeclarationStatement) {
            procedureDeclarationStatement.getProcedureType().accept(this);
        }

        if (odinDeclaration instanceof OdinConstantInitializationStatement constantInitializationStatement) {
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
                    this.type = resolveMetaType(scope, metaType);
                    odinDeclaration = metaType.getDeclaration();
                }
            }
        }

        if (odinDeclaration instanceof OdinPolymorphicType polymorphicType) {
            polymorphicType.accept(this);
        }

        if (this.type != null) {
            this.type.setDeclaration(odinDeclaration);
            this.type.setName(identifier.getName());
            this.type.setDeclaredIdentifier(identifier);
            return this.type;
        }

        return TsOdinType.UNKNOWN;
    }

    public static TsOdinType resolveMetaType(OdinScope scope, TsOdinMetaType metaType) {
        if (metaType.getMetaType() == TsOdinMetaType.MetaType.BUILTIN) {
            TsOdinBuiltInType tsOdinBuiltInType = new TsOdinBuiltInType();
            tsOdinBuiltInType.setName(metaType.getName());
            return tsOdinBuiltInType;
        } else if (metaType.getType() != null) {
            TsOdinType tsOdinType = resolveType(scope, metaType.getType());
            tsOdinType.setDeclaration(metaType.getDeclaration());
            tsOdinType.setName(metaType.getName());
//            tsOdinType.setScope(metaType.getScope());
            tsOdinType.setDeclaredIdentifier(metaType.getDeclaredIdentifier());
            return tsOdinType;
        }
        return null;
    }
}
