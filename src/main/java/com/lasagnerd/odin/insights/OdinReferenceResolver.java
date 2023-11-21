package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OdinReferenceResolver {
        public static Scope resolve(Scope scope, OdinExpression valueExpression) {
            OdinTypedNode odinTypedNode = new OdinTypedNode(scope);
            valueExpression.accept(odinTypedNode);
            return OdinTypedNode.getCompletionScopeOfType(scope, odinTypedNode.typeIdentifier);
        }
}

class OdinTypedNode extends OdinVisitor {

    final Scope scope;

    // This is not enough information. Some types, such as multi-value return types, are not declared identifiers
    OdinDeclaredIdentifier typeIdentifier;

    public OdinTypedNode(Scope scope) {
        this.scope = scope;
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression o) {
        Scope localScope;
        if (o.getExpression() != null) {
            // solve for expression first. This defines the scope
            // extract scope
            OdinTypedNode odinTypedNode = new OdinTypedNode(this.scope);
            o.getExpression().accept(odinTypedNode);
            localScope = getCompletionScopeOfType(this.scope, odinTypedNode.typeIdentifier);
        } else {
            localScope = this.scope;
        }

        // using current scope, find identifier declaration and extract type
        PsiNamedElement namedElement = localScope.findNamedElement(o.getIdentifier().getText());
        if (namedElement instanceof OdinDeclaredIdentifier declaredIdentifier) {
            OdinDeclaration odinDeclaration = OdinInsightUtils.findFirstParentOfType(declaredIdentifier, true, OdinDeclaration.class);
            typeIdentifier = getValueExpression(declaredIdentifier, odinDeclaration);
        }
    }

    private OdinDeclaredIdentifier getValueExpression(OdinDeclaredIdentifier declaredIdentifier, OdinDeclaration odinDeclaration) {
        if(odinDeclaration instanceof OdinVariableDeclarationStatement declarationStatement) {
            OdinExpression mainType = declarationStatement.getTypeDefinition().getMainType();
            return getDeclaredIdentifierOfTypeRef(mainType);
        }

        if(odinDeclaration instanceof OdinVariableInitializationStatement initializationStatement) {
            if(initializationStatement.getTypeDefinition() != null) {
                return getDeclaredIdentifierOfTypeRef(initializationStatement.getTypeDefinition().getMainType());
            }

            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            OdinExpression odinExpression = initializationStatement.getExpressionsList().getExpressionList().get(index);
            OdinTypedNode odinTypedNode = new OdinTypedNode(this.scope);
            odinExpression.accept(odinTypedNode);
            return odinTypedNode.typeIdentifier;
        }

        if(odinDeclaration instanceof OdinConstantInitializationStatement initializationStatement) {
            if(initializationStatement.getTypeDefinition() != null) {
                return getDeclaredIdentifierOfTypeRef(initializationStatement.getTypeDefinition().getMainType());
            }
            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            OdinExpression odinExpression = initializationStatement.getExpressionsList().getExpressionList().get(index);
            OdinTypedNode odinTypedNode = new OdinTypedNode(this.scope);
            odinExpression.accept(odinTypedNode);
            return odinTypedNode.typeIdentifier;
        }

        if(odinDeclaration instanceof OdinFieldDeclarationStatement fieldDeclarationStatement) {
            OdinExpression mainType;
            if(fieldDeclarationStatement.getExpression() instanceof OdinTypeExpression expr) {
                mainType = expr;
            } else {

                OdinTypeDefinitionExpression typeDefinition = fieldDeclarationStatement.getTypeDefinition();
                mainType = typeDefinition.getMainType();
            }

            return getDeclaredIdentifierOfTypeRef(mainType);
        }

        if(odinDeclaration instanceof OdinParameterDeclaration parameterDeclaration) {

        }

        if(odinDeclaration instanceof OdinParameterInitialization parameterInitialization) {

        }

        return null;
    }

    @Override
    public void visitCompoundLiteralExpression(@NotNull OdinCompoundLiteralExpression o) {
        if(o.getCompoundLiteral() instanceof OdinCompoundLiteralTyped typed) {
            typeIdentifier = getDeclaredIdentifierOfTypeRef(typed.getTypeExpression());
        }
    }

    private OdinDeclaredIdentifier getDeclaredIdentifierOfTypeRef(@Nullable OdinExpression typeExpression) {
        if(typeExpression instanceof OdinTypeRef) {
            return (OdinDeclaredIdentifier) this.scope.findNamedElement(typeExpression.getText());
        }

        return null;
    }

    @Override
    public void visitCallExpression(@NotNull OdinCallExpression o) {
        // Get type of expression. If it is callable, retrieve the return type and set that as result


    }

    @Override
    public void visitIndexExpression(@NotNull OdinIndexExpression o) {
        // get type of expression. IF it is indexable (array, matrix, bitset, map), retrieve the indexed type and set that as result


    }

    @Override
    public void visitDereferenceExpression(@NotNull OdinDereferenceExpression o) {
        // get type of expression. If it is a pointer type, retrieve the type without the pointer and set that as result
        super.visitDereferenceExpression(o);
    }

    static Scope getCompletionScopeOfType(Scope scope, PsiElement identifier) {
        if (!(identifier instanceof OdinDeclaredIdentifier))
            return null;

        OdinDeclaration odinDeclaration = OdinInsightUtils.findFirstParentOfType(identifier,
                false,
                OdinDeclaration.class);

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            return scope.with(getStructFields(structDeclarationStatement));
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            return scope.with(getEnumFields(enumDeclarationStatement));
        }

        if (odinDeclaration instanceof OdinImportDeclarationStatement importDeclarationStatement) {
            return OdinInsightUtils.getDeclarationsOfImportedPackage(scope, importDeclarationStatement);
        }

        return Scope.EMPTY;
    }

    @NotNull
    private static List<PsiNamedElement> getEnumFields(OdinEnumDeclarationStatement enumDeclarationStatement) {
        OdinEnumBody enumBody = enumDeclarationStatement.getEnumType()
                .getEnumBlock()
                .getEnumBody();

        if (enumBody == null)
            return Collections.emptyList();

        return enumBody
                .getEnumValueDeclarationList()
                .stream()
                .map(OdinEnumValueDeclaration::getDeclaredIdentifier)
                .collect(Collectors.toList());
    }

    private static List<PsiNamedElement> getStructFields(OdinStructDeclarationStatement structDeclarationStatement) {
        OdinStructBody structBody = structDeclarationStatement
                .getStructType()
                .getStructBlock()
                .getStructBody();

        if (structBody == null) {
            return Collections.emptyList();
        }

        return structBody.getFieldDeclarationStatementList().stream()
                .flatMap(x -> x.getDeclaredIdentifiers().stream())
                .collect(Collectors.toList());
    }
}