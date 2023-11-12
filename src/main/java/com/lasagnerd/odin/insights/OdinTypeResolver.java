package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class OdinTypeResolver {
    public static Scope resolveScope(OdinExpression expression) {
        Scope initialScope = OdinInsightUtils.findDeclarationWithinScope(expression);
        OdinTypeInferVisitor visitor = new OdinTypeInferVisitor(initialScope);
        printScope(expression, initialScope);
        expression.accept(visitor);
        return visitor.currentScope;
    }

    public static void printScope(PsiElement psiElement, Scope scope) {
        System.out.printf("--------<%s>--------%n", psiElement.getText());
        for (PsiNamedElement element : scope.getNamedElements()) {
            System.out.println(element + ": " + element.getName());
        }
        System.out.printf("--------</%s>--------%n", psiElement.getText());
    }
}

class OdinTypeInferVisitor extends OdinVisitor {
    Scope initialScope;

    Scope currentScope;

    public OdinTypeInferVisitor(Scope scope) {

        this.initialScope = scope;
        this.currentScope = scope;
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression refExpression) {
        if (refExpression.getExpression() != null) {
            refExpression.getExpression().accept(this);
        }

        OdinIdentifier identifier = refExpression.getIdentifier();
        PsiNamedElement namedElement = currentScope.findNamedElement(identifier.getText());

        if (namedElement != null) {
            currentScope = createScopeFromIdentifier(namedElement);

            OdinTypeResolver.printScope(identifier, currentScope);
        }
    }



    private Scope createScopeFromIdentifier(PsiElement identifier) {
        OdinDeclaration declaration = OdinInsightUtils.findFirstParentOfType(identifier,
                true,
                OdinDeclaration.class);

        // The identifier passed here, is the one we found in our current scope
        // The next step is to infer its type.

        if (declaration instanceof OdinImportDeclarationStatement importStatement) {

        }

        if (declaration instanceof OdinStructDeclarationStatement struct) {
            List<PsiNamedElement> declaredIdentifiers = new ArrayList<>();
            OdinStructBody structBody = struct.getStructType().getStructBlock().getStructBody();
            if (structBody != null) {
                for (OdinFieldDeclarationStatement odinFieldDeclarationStatement : structBody.getFieldDeclarationStatementList()) {
                    declaredIdentifiers.addAll(odinFieldDeclarationStatement.getDeclaredIdentifiers());
                }
            }

            return Scope.from(declaredIdentifiers);
        }

        OdinTypeDefinitionExpression typeDefinition = null;
        if (declaration instanceof OdinTypedDeclaration typedDeclaration) {
            typeDefinition = typedDeclaration.getTypeDefinition();
        }

        if (typeDefinition != null) {
            // now that we have type we have to follow it back to its atomic declaration
            OdinType mainType = typeDefinition.getMainType();

            // two cases
            if (mainType instanceof OdinConcreteType concreteType) {
                Scope scope = initialScope;
                for (OdinIdentifier odinIdentifier : concreteType.getIdentifierList()) {
                    var declaredIdentifier = findDeclaredIdentifier(scope, odinIdentifier.getText());
                    if (declaredIdentifier != null) {
                        return createScopeFromIdentifier(declaredIdentifier);
                    }
                }
            }
        }

        return Scope.EMPTY;
    }

    public PsiNamedElement findDeclaredIdentifier(Scope scope, @NotNull String name) {
        return scope.findNamedElement(name);
    }
}

