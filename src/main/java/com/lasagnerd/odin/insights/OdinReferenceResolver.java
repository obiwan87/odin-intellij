package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class OdinReferenceResolver {
    private OdinDeclaration declaration;
    private OdinIdentifier identifier;

    public OdinReferenceResolver(Scope scope) {
        this.expressionScope = scope;
        this.contextScope = scope;
        this.completionScope = scope;
    }

    final Scope expressionScope;

    // All that the currently referenced type has access to
    Scope contextScope;

    // All the identifiers that will be suggested as completions
    Scope completionScope;


    public void resolve(@NotNull OdinRefExpression refExpression) {
        if (refExpression.getExpression() instanceof OdinRefExpression child) {
            resolve(child);
        } else if(refExpression.getExpression() != null) {
            OdinTypeExpression type = OdinTypeResolver.resolve(contextScope, refExpression.getExpression());
            contextScope = OdinInsightUtils.findScope(type);
            completionScope = createCompletionScopeForType(contextScope, type);
        }

        OdinIdentifier identifier = refExpression.getIdentifier();
        PsiNamedElement namedElement = completionScope.findNamedElement(identifier.getText());

        if (namedElement != null) {
            goToDeclaration(namedElement);

            OdinReferenceResolver.printScope(identifier, completionScope);
        }
    }

    public void resolve(@NotNull OdinTypeRef typeRef) {
        OdinIdentifier packageIdentifier;
        OdinIdentifier typeIdentifier;

        if(typeRef.getIdentifierList().size() == 2) {
            packageIdentifier = typeRef.getIdentifierList().get(0);
            typeIdentifier = typeRef.getIdentifierList().get(1);
        } else {
            packageIdentifier = null;
            typeIdentifier = typeRef.getIdentifierList().get(0);
        }

        if(packageIdentifier != null) {
            PsiNamedElement namedElement = contextScope.findNamedElement(packageIdentifier.getText());
            if(namedElement instanceof OdinImportDeclarationStatement importDeclarationStatement) {
                contextScope = Scope.from(OdinInsightUtils.getDeclarationsOfImportedPackage(contextScope, importDeclarationStatement));
            }
        }
        PsiNamedElement namedElement = contextScope.findNamedElement(typeIdentifier.getText());

        declaration = OdinInsightUtils.findFirstParentOfType(namedElement,
                false,
                OdinDeclaration.class);
        identifier = typeIdentifier;
        completionScope = Scope.EMPTY;
    }

    private void goToDeclaration(PsiElement identifier) {
        OdinDeclaration declaration = OdinInsightUtils.findFirstParentOfType(identifier,
                false,
                OdinDeclaration.class);
        this.declaration = declaration;
        // The identifier passed here, is the one we found in our current scope
        // The next step is to infer its type.
        if (declaration instanceof OdinImportDeclarationStatement importStatement) {
            List<PsiNamedElement> declarationsOfImportedPackage = OdinInsightUtils
                    .getDeclarationsOfImportedPackage(contextScope, importStatement);
            completionScope = contextScope = Scope.from(declarationsOfImportedPackage);
            return;
        }

        // Get type from declaration or infer from value
        OdinTypeExpression type;
        if (declaration instanceof OdinTypedDeclaration typedDeclaration) {
            OdinTypeDefinitionExpression typeDefinition = typedDeclaration.getTypeDefinition();
            type = (OdinTypeExpression) typeDefinition.getMainType();
        } else {
            OdinExpression valueExpression = getValueExpression(identifier, declaration);
            // This should spit out a type
            type = OdinTypeResolver.resolve(contextScope, valueExpression);
        }

        // now that we have type we have to follow it back to its atomic declaration
        contextScope = OdinInsightUtils.findScope(type);
        completionScope = createCompletionScopeForType(contextScope, type);
    }

    private Scope createCompletionScopeForType(Scope scope, @Nullable OdinTypeExpression type) {
        // two cases
        if (type instanceof OdinTypeRef typeRef) {

            OdinReferenceResolver referenceResolver = new OdinReferenceResolver(scope);
            referenceResolver.resolve(typeRef);

            OdinDeclaration typeDeclaration = referenceResolver.declaration;

            // Follow the assignment chain until we find a concrete type, i.e. direct definition and not an alias
            PsiElement typeDefinition = followAssignments(identifier, declaration);

            // Type definition is one of the built-in types
            return scope;
        }

        return Scope.EMPTY;
    }

    // TODO generalize to any depth
    private static PsiElement followAssignments(PsiElement identifier, OdinDeclaration declaration) {
        return getValueExpression(identifier, declaration);
    }

    private static OdinExpression getValueExpression(PsiElement identifier, OdinDeclaration declaration) {
        if (!(identifier instanceof OdinDeclaredIdentifier declaredIdentifier))
            return null;

        if (declaration instanceof OdinVariableInitializationStatement init) {
            int index = init.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            if (index >= 0) {
                return init.getExpressionsList().getExpressionList().get(index);
            }
        }

        if (declaration instanceof OdinConstantInitializationStatement init) {
            int index = init.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            if (index >= 0) {
                return init.getExpressionsList().getExpressionList().get(index);
            }
        }

        if (declaration instanceof OdinParameterInitialization init) {
            return init.getExpressionList().get(0);
        }

        if(declaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            return structDeclarationStatement.getStructType();
        }

        return null;
    }

    public record RefResult(Scope completionScope, Scope contextScope, OdinDeclaration declaration) {
    }

    static RefResult resolve(@NotNull PsiFile originalFile, OdinRefExpression expression) {
        Scope initialScope = OdinInsightUtils.findScope(expression);

        var resolver = new OdinReferenceResolver(initialScope);
        printScope(expression, initialScope);
        resolver.resolve(expression);

        RefResult refResult = new OdinReferenceResolver.RefResult(resolver.completionScope, resolver.contextScope, resolver.declaration);
        return null;
    }

    public static void printScope(PsiElement psiElement, Scope scope) {
        System.out.printf("--------<%s>--------%n", psiElement.getText());
        for (PsiNamedElement element : scope.getNamedElements()) {
            System.out.println(element + ": " + element.getName());
        }
        System.out.printf("--------</%s>--------%n", psiElement.getText());
    }
}

