package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.insights.OdinInsightUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class OdinTypeInference {
    public static List<OdinDeclaredIdentifier> inferType(OdinExpression expression) {
        List<OdinDeclaredIdentifier> declarations = OdinInsightUtils.findDeclarationWithinScope(expression);
        OdinTypeInferVisitor visitor = new OdinTypeInferVisitor(declarations);
        expression.accept(visitor);

        return visitor.currentScope;
    }
}

class OdinTypeInferVisitor extends OdinVisitor {

    PsiElement type;

    final List<OdinDeclaredIdentifier> initialScope;

    List<OdinDeclaredIdentifier> currentScope;

    public OdinTypeInferVisitor(List<OdinDeclaredIdentifier> scope) {
        this.initialScope = scope;
        this.currentScope = scope;
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression refExpression) {
        if (refExpression.getExpression() != null) {
            refExpression.getExpression().accept(this);
        }

        // This is the top most expression, e.g. fmt.println() -> we just came in from println
        // we have to find the scope for fmt and put it on the stack, such that when we return
        // println will be found on that scope
        OdinIdentifier identifier = refExpression.getIdentifier();
        Optional<OdinDeclaredIdentifier> maybe = findDeclaredIdentifier(currentScope, identifier.getText());

        if (maybe.isPresent()) {
            OdinDeclaredIdentifier declaredIdentifier = maybe.get();
            currentScope = createScopeFromIdentifier(declaredIdentifier);
        }
    }

    private List<OdinDeclaredIdentifier> createScopeFromIdentifier(OdinDeclaredIdentifier identifier) {
        OdinDeclaration declaration = OdinInsightUtils.findFirstParentOfType(identifier,
                true,
                OdinDeclaration.class);

        // The identifier passed here, is the one we found in our current scope
        // The next step is to infer its type.

        if (declaration instanceof OdinImportDeclarationStatement importStatement) {

        }

        if (declaration instanceof OdinStructDeclarationStatement struct) {
            List<OdinDeclaredIdentifier> declaredIdentifiers = new ArrayList<>();
            OdinStructBody structBody = struct.getStructType().getStructBlock().getStructBody();
            if (structBody != null) {
                for (OdinFieldDeclarationStatement odinFieldDeclarationStatement : structBody.getFieldDeclarationStatementList()) {
                    declaredIdentifiers.addAll(odinFieldDeclarationStatement.getDeclaredIdentifiers());
                }
            }

            return declaredIdentifiers;
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
                List<OdinDeclaredIdentifier> scope = currentScope;
                for (OdinIdentifier odinIdentifier : concreteType.getIdentifierList()) {
                    Optional<OdinDeclaredIdentifier> declaredIdentifier = findDeclaredIdentifier(scope, odinIdentifier.getText());
                    if (declaredIdentifier.isPresent()) {
                        return createScopeFromIdentifier(declaredIdentifier.get());
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    public Optional<OdinDeclaredIdentifier> findDeclaredIdentifier(List<OdinDeclaredIdentifier> scope, @NotNull String name) {
        for (OdinDeclaredIdentifier odinDeclaredIdentifier : scope) {
            if (odinDeclaredIdentifier.getText().equals(name))
                return Optional.of(odinDeclaredIdentifier);
        }
        return Optional.empty();
    }
}
