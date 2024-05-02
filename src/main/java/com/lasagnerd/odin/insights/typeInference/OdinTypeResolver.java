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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.lasagnerd.odin.insights.typeInference.OdinExpressionInferenceEngine.inferType;
import static com.lasagnerd.odin.lang.OdinLangSyntaxAnnotator.RESERVED_TYPES;

@EqualsAndHashCode(callSuper = true)
@Data
public class OdinTypeResolver extends OdinVisitor {

    @Nullable
    public static TsOdinType resolveType(OdinScope scope, OdinType typeExpression) {
        OdinTypeResolver odinTypeExpressionResolver = new OdinTypeResolver(scope);
        typeExpression.accept(odinTypeExpressionResolver);
        return odinTypeExpressionResolver.type;
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
        PsiNamedElement declaration;

        if (qualifiedType.getPackageIdentifier() != null) {
            OdinScope packageScope = scope.getScopeOfImport(qualifiedType.getPackageIdentifier().getIdentifierToken().getText());
            OdinTypeResolver odinTypeExpressionResolver = new OdinTypeResolver(packageScope);
            OdinType typeExpression = qualifiedType.getType();
            typeExpression.accept(odinTypeExpressionResolver);
            this.type = odinTypeExpressionResolver.type;
            return;
        }

        OdinIdentifier typeIdentifier = qualifiedType.getTypeIdentifier();
        String identifierText = typeIdentifier.getText();
        if (RESERVED_TYPES.contains(identifierText)) {
            type = new TsOdinBuiltInType();
            type.setName(identifierText);
        } else {
            declaration = scope.findNamedElement(typeIdentifier.getIdentifierToken().getText());
            if (!(declaration instanceof OdinDeclaredIdentifier declaredIdentifier)) {
                return;
            }
            type = resolveTypeFromDeclaredIdentifier(scope, declaredIdentifier);
        }
    }

    @Override
    public void visitUnionType(@NotNull OdinUnionType o) {
    }

    @Override
    public void visitArrayType(@NotNull OdinArrayType o) {
        TsOdinArrayType arrayType = new TsOdinArrayType();
        OdinTypeResolver odinTypeExpressionResolver = new OdinTypeResolver(scope);
        o.getTypeDefinition().accept(odinTypeExpressionResolver);
        TsOdinType elementType = odinTypeExpressionResolver.type;
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

    @Override
    public void visitProcedureType(@NotNull OdinProcedureType odinProcedureType) {
        TsOdinProcedureType procedureType = new TsOdinProcedureType();
        OdinTypeResolver odinTypeExpressionResolver = new OdinTypeResolver(scope);

        OdinReturnParameters returnParameters = odinProcedureType.getReturnParameters();
        if (returnParameters != null) {
            OdinTypeDefinitionExpression typeDefinitionExpression = returnParameters.getTypeDefinitionExpression();
            if (typeDefinitionExpression != null) {
                OdinType typeExpression = typeDefinitionExpression.getType();
                typeExpression.accept(odinTypeExpressionResolver);
                if (odinTypeExpressionResolver.type != null) {
                    // TODO add support for multiple return values
                    procedureType.setReturnTypes(List.of(odinTypeExpressionResolver.type));
                } else {
                    procedureType.setReturnTypes(Collections.emptyList());
                }
            }
        }
        // TODO arguments
        this.type = procedureType;
    }

    @Override
    public void visitStructType(@NotNull OdinStructType o) {
        TsOdinStructType structType = new TsOdinStructType();
        structType.setParentScope(scope);
        this.type = structType;

        var odinPolymorphicParameters = o.getPolymorphicParameterList();
        for (OdinPolymorphicParameter odinPolymorphicParameter : odinPolymorphicParameters) {
            List<OdinDeclarationSpec> declarationSpecs = OdinDeclarationSpecifier.getDeclarationSpecs(odinPolymorphicParameter.getParameterDeclaration());
            for (OdinDeclarationSpec declarationSpec : declarationSpecs) {
                if (!declarationSpec.isPolymorphic())
                    continue;
                TsOdinPolyParameter polyParameter = new TsOdinPolyParameter();
                polyParameter.setDeclaredIdentifier(declarationSpec.getDeclaredIdentifier());

                if (declarationSpec.getTypeDefinitionExpression() != null) {
                    TsOdinType tsOdinType = resolveType(scope, declarationSpec.getTypeDefinitionExpression().getType());
                    polyParameter.setOdinType(tsOdinType);
                }

                structType.getPolyParameters().add(polyParameter);
            }

        }
        // TODO set generic parameters
    }

    @Override
    public void visitEnumType(@NotNull OdinEnumType o) {
        TsOdinEnumType enumType = new TsOdinEnumType();
        enumType.setParentScope(scope);
        // TODO Set fields

    }

    @Override
    public void visitPolymorphicType(@NotNull OdinPolymorphicType o) {
        TsOdinPolymorphicType tsOdinPolymorphicType = new TsOdinPolymorphicType();
        tsOdinPolymorphicType.setParentScope(scope);
    }

    @Override
    public void visitConstrainedType(@NotNull OdinConstrainedType o) {
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
                    metaType.getTypeExpression().accept(this);
                    odinDeclaration = metaType.getDeclaration();
                }
            }
        }

        if (this.type != null) {
            this.type.setDeclaration(odinDeclaration);
            return this.type;
        }

        return TsOdinType.UNKNOWN;
    }
}
