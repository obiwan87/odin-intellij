package com.lasagnerd.odin.insights.typeInference;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.insights.OdinDeclarationSpec;
import com.lasagnerd.odin.insights.OdinDeclarationSpecifier;
import com.lasagnerd.odin.insights.OdinInsightUtils;
import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.insights.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine.inferType;
import static com.lasagnerd.odin.lang.OdinLangSyntaxAnnotator.RESERVED_TYPES;

@EqualsAndHashCode(callSuper = true)
@Data
public class OdinTypeResolver extends OdinVisitor {

    @Nullable
    public static TsOdinType resolveType(OdinScope scope, OdinType type) {
        OdinTypeResolver typeResolver = new OdinTypeResolver(scope);
        type.accept(typeResolver);
        return typeResolver.type;
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
            OdinScope currentScope = structType.getLocalScope();
            this.type = OdinTypeInstantiator.instantiateStruct(currentScope, o.getArgumentList(), structType);
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
    public void visitArrayType(@NotNull OdinArrayType o) {
        TsOdinArrayType arrayType = new TsOdinArrayType();
        TsOdinType elementType = resolveType(scope, o.getTypeDefinition());
        arrayType.setElementType(elementType);
        this.type = arrayType;
    }

    @Override
    public void visitMapType(@NotNull OdinMapType o) {
        TsOdinMapType mapType = new TsOdinMapType();
        OdinTypeResolver keyOdinTypeExpressionResolver = new OdinTypeResolver(scope);
        o.getKeyType().accept(keyOdinTypeExpressionResolver);
        TsOdinType keyType = keyOdinTypeExpressionResolver.type;
        mapType.setKeyType(keyType);

        OdinTypeResolver valueOdinTypeExpressionResolver = new OdinTypeResolver(scope);
        o.getValueType().accept(valueOdinTypeExpressionResolver);
        TsOdinType valueType = valueOdinTypeExpressionResolver.type;
        mapType.setValueType(valueType);
        this.type = mapType;
    }

    @Override
    public void visitPointerType(@NotNull OdinPointerType odinPointerType) {
        TsOdinPointerType pointerType = new TsOdinPointerType();
        OdinTypeResolver odinTypeExpressionResolver = new OdinTypeResolver(scope);
        var typeExpression = odinPointerType.getType();

        Objects.requireNonNull(typeExpression)
                .accept(odinTypeExpressionResolver);

        TsOdinType elementType = odinTypeExpressionResolver.type;
        pointerType.setDereferencedType(elementType);

        this.type = pointerType;
    }
    /*
        struct($Key: typeid, $Value: $S) {
            field1: [Value]Key,
            field2: Key
        }
     */

    @Override
    public void visitProcedureType(@NotNull OdinProcedureType odinProcedureType) {
        TsOdinProcedureType procedureType = new TsOdinProcedureType();
        procedureType.setType(odinProcedureType);

        List<OdinParamEntry> paramEntries = odinProcedureType.getParamEntryList();
        OdinScope newScope = populateParameters(paramEntries, procedureType, scope);
        procedureType.setLocalScope(newScope);

        OdinReturnParameters returnParameters = odinProcedureType.getReturnParameters();
        if (returnParameters != null) {
            OdinTypeDefinitionExpression typeDefinitionExpression = returnParameters.getTypeDefinitionExpression();
            if (typeDefinitionExpression != null) {
                OdinType typeExpression = typeDefinitionExpression.getType();
                TsOdinType tsOdinType = resolveType(newScope, typeExpression);
                if (tsOdinType != null) {
                    // TODO add support for multiple return values
                    procedureType.setReturnTypes(List.of(tsOdinType));
                } else {
                    procedureType.setReturnTypes(Collections.emptyList());
                }
            }
        }

        this.type = procedureType;
    }

    @Override
    public void visitStructType(@NotNull OdinStructType o) {
        TsOdinStructType structType = new TsOdinStructType();
        structType.setType(o);

        this.type = structType;

        List<OdinParamEntry> paramEntries = o.getParamEntryList();
        OdinScope localScope = populateParameters(paramEntries, structType, scope);
        structType.setLocalScope(localScope);
    }

    private static OdinScope populateParameters(List<OdinParamEntry> paramEntries, TsOdinType baseType, OdinScope currentScope) {
        List<TsOdinParameter> typeParameters = baseType.getParameters();
        OdinScope newScope = new OdinScope();
        newScope.putAll(currentScope);
        int k = 0;
        for (var paramEntry : paramEntries) {
            OdinParameterDeclaration parameterDeclaration = paramEntry.getParameterDeclaration();

            List<OdinDeclarationSpec> declarationSpecs = OdinDeclarationSpecifier.getDeclarationSpecs(parameterDeclaration);
            for (OdinDeclarationSpec declarationSpec : declarationSpecs) {
                TsOdinParameter tsOdinParameter = mapSpecToParameter(newScope, declarationSpec, k);
                k++;

                TsOdinType tsOdinType = tsOdinParameter.getType();
                if(tsOdinType != null) {
                    Map<String, TsOdinType> unresolvedPolymorphicParameters = tsOdinType.getUnresolvedPolymorphicParameters();
                    baseType.getUnresolvedPolymorphicParameters().putAll(unresolvedPolymorphicParameters);
                    for (Map.Entry<String, TsOdinType> entry : unresolvedPolymorphicParameters.entrySet()) {
                        newScope.addType(entry.getKey(), entry.getValue());
                        newScope.add(entry.getValue().getDeclaredIdentifier());
                    }
                }
                if (declarationSpec.isValuePolymorphic()) {
                    // This is the case for $T: typeid -> the declared identifier has to be known to the procedure
                    // (e.g. for future parameters or for return type)
                    if (tsOdinType != null && tsOdinType.isTypeId()) {
                        // add to type scope
                        TsOdinPolymorphicType valuePolymorphicType = new TsOdinPolymorphicType();
                        String name = declarationSpec.getValueDeclaredIdentifier().getName();
                        valuePolymorphicType.setName(name);
                        valuePolymorphicType.setDeclaredIdentifier(declarationSpec.getValueDeclaredIdentifier());
                        newScope.addType(valuePolymorphicType.getName(), valuePolymorphicType);
                        baseType.getUnresolvedPolymorphicParameters().put(name, valuePolymorphicType);
                    }
                    newScope.add(declarationSpec.getValueDeclaredIdentifier());
                }

                if (declarationSpec.isTypePolymorphic()) {
                    newScope.add(declarationSpec.getPolymorphicTypeDeclaredIdentifier());
                    newScope.addType(declarationSpec.getPolymorphicTypeDeclaredIdentifier().getName(),
                            tsOdinType);
                    baseType.getUnresolvedPolymorphicParameters().put(declarationSpec.getPolymorphicTypeDeclaredIdentifier().getName(), tsOdinType);
                }

                typeParameters.add(tsOdinParameter);
            }
        }

        return newScope;
    }

    private static @NotNull TsOdinParameter mapSpecToParameter(OdinScope scope, OdinDeclarationSpec declarationSpec, int parameterIndex) {
        TsOdinParameter tsOdinParameter = new TsOdinParameter();
        tsOdinParameter.setValueDeclaredIdentifier(declarationSpec.getValueDeclaredIdentifier());
        tsOdinParameter.setValueName(declarationSpec.getValueDeclaredIdentifier().getName());
        tsOdinParameter.setValuePolymorphic(declarationSpec.isValuePolymorphic());
        tsOdinParameter.setIndex(parameterIndex);
        if (declarationSpec.getTypeDefinitionExpression() != null) {
            TsOdinType tsOdinType = resolveType(scope, declarationSpec.getTypeDefinitionExpression().getType());
            tsOdinParameter.setType(tsOdinType);
        }
        return tsOdinParameter;
    }

    @Override
    public void visitEnumType(@NotNull OdinEnumType o) {
        TsOdinEnumType enumType = new TsOdinEnumType();
        enumType.setLocalScope(scope);
        // TODO Set fields

    }

    @Override
    public void visitPolymorphicType(@NotNull OdinPolymorphicType o) {
        TsOdinPolymorphicType tsOdinPolymorphicType = new TsOdinPolymorphicType();
        tsOdinPolymorphicType.setLocalScope(scope);
        tsOdinPolymorphicType.setName(o.getDeclaredIdentifier().getIdentifierToken().getText());
        this.type = tsOdinPolymorphicType;
    }

    @Override
    public void visitConstrainedType(@NotNull OdinConstrainedType o) {
        OdinType mainType = o.getTypeList().get(0);
        OdinType specializedType = o.getTypeList().get(1);

        // TODO

        super.visitConstrainedType(o);
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
            if (tsOdinType != null) {
                tsOdinType.setDeclaration(metaType.getDeclaration());
                tsOdinType.setName(metaType.getName());
                tsOdinType.setLocalScope(metaType.getLocalScope());
                tsOdinType.setDeclaredIdentifier(metaType.getDeclaredIdentifier());
                return tsOdinType;
            }
        }
        return null;
    }
}
