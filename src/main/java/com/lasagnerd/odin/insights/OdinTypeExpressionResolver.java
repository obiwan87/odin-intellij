package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.lang.typeSystem.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.lasagnerd.odin.lang.OdinLangSyntaxAnnotator.RESERVED_TYPES;

@EqualsAndHashCode(callSuper = true)
@Data
class OdinTypeExpressionResolver extends OdinVisitor {

    @Nullable
    public static TsOdinType resolveType(OdinScope scope, OdinTypeExpression typeExpression) {
        OdinTypeExpressionResolver odinTypeExpressionResolver = new OdinTypeExpressionResolver(scope);
        typeExpression.accept(odinTypeExpressionResolver);
        return odinTypeExpressionResolver.type;
    }


    OdinScope scope;
    private final OdinScope initialScope;
    TsOdinType type;

    public OdinTypeExpressionResolver(OdinScope scope) {
        this.initialScope = scope;
        this.scope = scope;
    }

    @Override
    public void visitQualifiedType(@NotNull OdinQualifiedType qualifiedType) {
        PsiNamedElement declaration;

        if (qualifiedType.getPackageIdentifier() != null) {
            OdinScope packageScope = scope.getScopeOfImport(qualifiedType.getPackageIdentifier().getIdentifierToken().getText());
            OdinTypeExpressionResolver odinTypeExpressionResolver = new OdinTypeExpressionResolver(packageScope);
            OdinTypeExpression typeExpression = qualifiedType.getTypeExpression();
            if (typeExpression != null) {
                typeExpression.accept(odinTypeExpressionResolver);
                this.type = odinTypeExpressionResolver.type;
            }
            return;
        }

        OdinIdentifier typeIdentifier = qualifiedType.getTypeIdentifier();
        String identifierText = typeIdentifier.getText();
        if (RESERVED_TYPES.contains(identifierText)) {
            type = new TsBuiltInType();
            type.setName(identifierText);
        } else {
            declaration = scope.findNamedElement(typeIdentifier.getIdentifierToken().getText());
            if (declaration == null) {
                return;
            }
            type = createType(scope, declaration);
        }
    }

    @Override
    public void visitUnionType(@NotNull OdinUnionType o) {
    }

    @Override
    public void visitArrayType(@NotNull OdinArrayType o) {
        TsOdinArrayType arrayType = new TsOdinArrayType();
        OdinTypeExpressionResolver odinTypeExpressionResolver = new OdinTypeExpressionResolver(scope);
        o.getTypeDefinition().accept(odinTypeExpressionResolver);
        TsOdinType elementType = odinTypeExpressionResolver.type;
        arrayType.setElementType(elementType);
        this.type = arrayType;
    }

    @Override
    public void visitMapType(@NotNull OdinMapType o) {
        TsOdinMapType mapType = new TsOdinMapType();
        OdinTypeExpressionResolver keyOdinTypeExpressionResolver = new OdinTypeExpressionResolver(scope);
        o.getKeyType().accept(keyOdinTypeExpressionResolver);
        TsOdinType keyType = keyOdinTypeExpressionResolver.type;
        mapType.setKeyType(keyType);

        OdinTypeExpressionResolver valueOdinTypeExpressionResolver = new OdinTypeExpressionResolver(scope);
        o.getValueType().accept(valueOdinTypeExpressionResolver);
        TsOdinType valueType = valueOdinTypeExpressionResolver.type;
        mapType.setValueType(valueType);
        this.type = mapType;
    }

    @Override
    public void visitPointerType(@NotNull OdinPointerType odinPointerType) {
        TsOdinPointerType pointerType = new TsOdinPointerType();
        OdinTypeExpressionResolver odinTypeExpressionResolver = new OdinTypeExpressionResolver(scope);
        OdinTypeExpression typeExpression = odinPointerType.getTypeExpression();

        Objects.requireNonNull(typeExpression)
                .accept(odinTypeExpressionResolver);

        TsOdinType elementType = odinTypeExpressionResolver.type;
        pointerType.setDereferencedType(elementType);

        this.type = pointerType;
    }

    @Override
    public void visitProcedureType(@NotNull OdinProcedureType odinProcedureType) {
        TsOdinProcedureType procedureType = new TsOdinProcedureType();
        OdinTypeExpressionResolver odinTypeExpressionResolver = new OdinTypeExpressionResolver(scope);


        OdinParamEntries paramEntries = odinProcedureType.getParamEntries();
        if (paramEntries != null) {
            for (OdinParamEntry odinParamEntry : paramEntries.getParamEntryList()) {

            }
        }

        OdinReturnParameters returnParameters = odinProcedureType.getReturnParameters();
        if (returnParameters != null) {
            OdinTypeDefinitionExpression typeDefinitionExpression = returnParameters.getTypeDefinitionExpression();
            if (typeDefinitionExpression != null) {
                OdinTypeExpression typeExpression = typeDefinitionExpression.getMainTypeExpression();
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
            List<OdinParameter> parameters = odinPolymorphicParameter.getParameterDeclaration().getParameterList();
        }

        // TODO set generic parameters
    }

    @Override
    public void visitEnumType(@NotNull OdinEnumType o) {
        TsOdinEnumType enumType = new TsOdinEnumType();
        enumType.setParentScope(scope);
        // TODO Set fields

    }

    /**
     * Creates a type from a given identifier
     *
     * @param scope      the scope in which the type is defined
     * @param identifier the identifier
     * @return the type
     * @see OdinDeclaredIdentifier
     * @see OdinScope
     * @see TsOdinType
     * <p>
     * When we finally find the identifier of a type, we can deduce from its declaration what type it is.
     * We can then create a new type object and return it.
     */

    // TODO setting the scope here doesn't feel right, as the scope might not be completely defined for polymorphic types
    private TsOdinType createType(OdinScope scope, PsiNamedElement identifier) {

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
                TypeInferenceResult typeInferenceResult = OdinExpressionTypeResolver.inferType(scope, odinExpression);
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
