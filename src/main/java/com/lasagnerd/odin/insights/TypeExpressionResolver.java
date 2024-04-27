package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.lang.typeSystem.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
class TypeExpressionResolver extends OdinVisitor {

    public static TsOdinType resolveType(Scope scope, OdinTypeExpression typeExpression) {
        TypeExpressionResolver typeExpressionResolver = new TypeExpressionResolver(scope);
        typeExpression.accept(typeExpressionResolver);
        return typeExpressionResolver.type;
    }


    Scope scope;
    private final Scope initialScope;
    TsOdinType type;

    public TypeExpressionResolver(Scope scope) {
        this.initialScope = scope;
        this.scope = scope;
    }

    @Override
    public void visitQualifiedType(@NotNull OdinQualifiedType qualifiedType) {
        PsiNamedElement declaration;

        if (qualifiedType.getPackageIdentifier() != null) {
            Scope packageScope = scope.getScopeOfImport(qualifiedType.getPackageIdentifier().getIdentifierToken().getText());
            TypeExpressionResolver typeExpressionResolver = new TypeExpressionResolver(packageScope);
            OdinTypeExpression typeExpression = qualifiedType.getTypeExpression();
            if(typeExpression != null) {
                typeExpression.accept(typeExpressionResolver);
                this.type = typeExpressionResolver.type;
            }
            return;
        }

        declaration = scope.findNamedElement(qualifiedType.getTypeIdentifier().getIdentifierToken().getText());
        if (declaration == null) {
            return;
        }
        type = createType(scope, declaration);
    }

    @Override
    public void visitUnionType(@NotNull OdinUnionType o) {
    }

    @Override
    public void visitArrayType(@NotNull OdinArrayType o) {
        TsOdinArrayType arrayType = new TsOdinArrayType();
        TypeExpressionResolver typeExpressionResolver = new TypeExpressionResolver(scope);
        o.getTypeDefinition().accept(typeExpressionResolver);
        TsOdinType elementType = typeExpressionResolver.type;
        arrayType.setElementType(elementType);
        this.type = arrayType;
    }

    @Override
    public void visitMapType(@NotNull OdinMapType o) {
        TsOdinMapType mapType = new TsOdinMapType();
        TypeExpressionResolver keyTypeExpressionResolver = new TypeExpressionResolver(scope);
        o.getKeyType().accept(keyTypeExpressionResolver);
        TsOdinType keyType = keyTypeExpressionResolver.type;
        mapType.setKeyType(keyType);

        TypeExpressionResolver valueTypeExpressionResolver = new TypeExpressionResolver(scope);
        o.getValueType().accept(valueTypeExpressionResolver);
        TsOdinType valueType = valueTypeExpressionResolver.type;
        mapType.setValueType(valueType);
        this.type = mapType;
    }

    @Override
    public void visitPointerType(@NotNull OdinPointerType odinPointerType) {
        TsOdinPointerType pointerType = new TsOdinPointerType();
        TypeExpressionResolver typeExpressionResolver = new TypeExpressionResolver(scope);
        OdinTypeExpression typeExpression = odinPointerType.getTypeExpression();

        Objects.requireNonNull(typeExpression)
                .accept(typeExpressionResolver);

        TsOdinType elementType = typeExpressionResolver.type;
        pointerType.setDereferencedType(elementType);

        this.type = pointerType;
    }

    @Override
    public void visitProcedureType(@NotNull OdinProcedureType odinProcedureType) {
        TsOdinProcedureType procedureType = new TsOdinProcedureType();
        TypeExpressionResolver typeExpressionResolver = new TypeExpressionResolver(scope);
        OdinReturnParameters returnParameters = odinProcedureType.getReturnParameters();
        if(returnParameters != null) {
            OdinTypeDefinitionExpression typeDefinitionExpression = returnParameters.getTypeDefinitionExpression();
            if(typeDefinitionExpression != null) {
                OdinTypeExpression typeExpression = typeDefinitionExpression.getMainTypeExpression();
                typeExpression.accept(typeExpressionResolver);
                if(typeExpressionResolver.type != null) {
                    procedureType.setReturnTypes(List.of(typeExpressionResolver.type));
                } else {
                    procedureType.setReturnTypes(Collections.emptyList());
                }
            }
        }
        // TODO arguments
        this.type = procedureType;
    }

    /**
     * Creates a type from a given identifier
     * @param scope the scope in which the type is defined
     * @param identifier the identifier
     * @return the type
     *
     * @see OdinDeclaredIdentifier
     * @see Scope
     * @see TsOdinType
     *
     * When we finally find the identifier of a type, we can deduce from its declaration what type it is.
     * We can then create a new type object and return it.
     *
     */

    // TODO setting the scope here doesn't feel right, as the scope might not be completely defined for polymorphic types
    private TsOdinType createType(Scope scope, PsiNamedElement identifier) {

        OdinDeclaration odinDeclaration = OdinInsightUtils.findFirstParentOfType(identifier,
                false,
                OdinDeclaration.class);

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            TsOdinStructType structType = new TsOdinStructType();
            structType.setDeclaration(structDeclarationStatement);
            structType.setParentScope(scope);
            return structType;
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            TsOdinEnumType enumType = new TsOdinEnumType();
            enumType.setDeclaration(enumDeclarationStatement);
            enumType.setParentScope(scope);
            return enumType;
        }

        if(odinDeclaration instanceof OdinConstantInitializationStatement constantInitializationStatement) {
            // Todo Add logic for alias
        }

        return TsOdinType.UNKNOWN;
    }
}
