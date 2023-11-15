package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class OdinReferenceResolver extends OdinVisitor {
    final PsiFile originalFile;

    public OdinReferenceResolver(PsiFile originalFile, Scope scope) {
        this.originalFile = originalFile;

        this.expressionScope = scope;
        this.contextScope = scope;
        this.completionScope = scope;
    }

    final Scope expressionScope;

    // All that the currently referenced type has access to
    Scope contextScope;

    // All the identifiers that will be suggested as completions
    Scope completionScope;


    @Override
    public void visitRefExpression(@NotNull OdinRefExpression refExpression) {
        if (refExpression.getExpression() != null) {
            refExpression.getExpression().accept(this);
        }

        OdinIdentifier identifier = refExpression.getIdentifier();
        PsiNamedElement namedElement = completionScope.findNamedElement(identifier.getText());

        if (namedElement != null) {
            createScopeFromIdentifier(namedElement);

            OdinReferenceResolver.printScope(identifier, completionScope);
        }
    }

    @Override
    public void visitCompoundLiteralExpression(@NotNull OdinCompoundLiteralExpression o) {
        OdinCompoundLiteral compoundLiteral = o.getCompoundLiteral();
        if (compoundLiteral.getType() != null) {
            completionScope = createScopeForTypeDefinition(compoundLiteral.getType());
        }
    }

    private void createScopeFromIdentifier(PsiElement identifier) {
        OdinDeclaration declaration = OdinInsightUtils.findFirstParentOfType(identifier,
                false,
                OdinDeclaration.class);

        // The identifier passed here, is the one we found in our current scope
        // The next step is to infer its type.
        if (declaration instanceof OdinImportDeclarationStatement importStatement) {
            List<PsiNamedElement> declarationsOfImportedPackage = OdinInsightUtils
                    .getDeclarationsOfImportedPackage(contextScope, importStatement);
            completionScope = contextScope = Scope.from(declarationsOfImportedPackage);
            return;
        }

//        if (declaration instanceof OdinStructDeclarationStatement struct) {
//            List<PsiNamedElement> declaredIdentifiers = new ArrayList<>();
//            OdinStructBody structBody = struct.getStructType().getStructBlock().getStructBody();
//            if (structBody != null) {
//                for (OdinFieldDeclarationStatement odinFieldDeclarationStatement : structBody.getFieldDeclarationStatementList()) {
//                    declaredIdentifiers.addAll(odinFieldDeclarationStatement.getDeclaredIdentifiers());
//                }
//            }
//            contextScope = OdinInsightUtils.findScope(declaration);
//            completionScope = Scope.from(declaredIdentifiers);
//            return;
//        }

        // Get type from declaration or infer from value
        OdinType type;
        if (declaration instanceof OdinTypedDeclaration typedDeclaration) {
            OdinTypeDefinitionExpression typeDefinition = typedDeclaration.getTypeDefinition();
            type = typeDefinition.getMainType();
        } else {
            OdinExpression valueExpression = getValueExpression(identifier, declaration);
            // This should spit out a type
            type = OdinTypeResolver.resolve(originalFile, valueExpression);
        }

        // now that we have type we have to follow it back to its atomic declaration
        completionScope = createScopeForTypeDefinition(type);
        contextScope = OdinInsightUtils.findScope(type);
    }

    private Scope createScopeForTypeDefinition(@Nullable OdinType type) {

        // two cases
        if (type instanceof OdinConcreteType concreteType) {
            Scope scope = completionScope;
            for (OdinIdentifier odinIdentifier : concreteType.getIdentifierList()) {
                var declaredIdentifier = findDeclaredIdentifier(scope, odinIdentifier.getText());
                if (declaredIdentifier != null) {
                    createScopeFromIdentifier(declaredIdentifier);
                }
            }

            return scope;
        }

        return Scope.EMPTY;
    }

    private OdinExpression getValueExpression(PsiElement identifier, OdinDeclaration declaration) {
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

        return null;
    }


    public PsiNamedElement findDeclaredIdentifier(Scope scope, @NotNull String name) {
        return scope.findNamedElement(name);
    }


    public record RefResult(Scope completionScope, Scope contextScope, OdinDeclaration declaration) {
    }

    static RefResult resolve(@NotNull PsiFile originalFile, OdinExpression expression) {
        Scope initialScope = OdinInsightUtils.findScope(expression);
        var resolver = new OdinReferenceResolver(originalFile, initialScope);
        printScope(expression, initialScope);
        expression.accept(resolver);
        return null;
    }

    public static Scope getCompletions(PsiFile original, OdinExpression expression) {
        return resolve(original, expression).completionScope;
    }

    public static void printScope(PsiElement psiElement, Scope scope) {
        System.out.printf("--------<%s>--------%n", psiElement.getText());
        for (PsiNamedElement element : scope.getNamedElements()) {
            System.out.println(element + ": " + element.getName());
        }
        System.out.printf("--------</%s>--------%n", psiElement.getText());
    }
}

