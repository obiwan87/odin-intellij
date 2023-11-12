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
    public void visitAutoCastExpression(@NotNull OdinAutoCastExpression o) {
        super.visitAutoCastExpression(o);
    }

    @Override
    public void visitBinaryExpression(@NotNull OdinBinaryExpression o) {
        super.visitBinaryExpression(o);
    }

    @Override
    public void visitCallExpression(@NotNull OdinCallExpression o) {
        super.visitCallExpression(o);
    }

    @Override
    public void visitCastExpression(@NotNull OdinCastExpression o) {
        super.visitCastExpression(o);
    }

    @Override
    public void visitCompoundLiteralExpression(@NotNull OdinCompoundLiteralExpression o) {
        super.visitCompoundLiteralExpression(o);
    }

    @Override
    public void visitDereferenceExpression(@NotNull OdinDereferenceExpression o) {
        super.visitDereferenceExpression(o);
    }

    @Override
    public void visitElvisExpression(@NotNull OdinElvisExpression o) {
        super.visitElvisExpression(o);
    }

    @Override
    public void visitExpressionsList(@NotNull OdinExpressionsList o) {
        super.visitExpressionsList(o);
    }

    @Override
    public void visitIndexExpression(@NotNull OdinIndexExpression o) {
        super.visitIndexExpression(o);
    }

    @Override
    public void visitLiteralExpression(@NotNull OdinLiteralExpression o) {
        super.visitLiteralExpression(o);
    }

    @Override
    public void visitMaybeExpression(@NotNull OdinMaybeExpression o) {
        super.visitMaybeExpression(o);
    }

    @Override
    public void visitOrBreakExpression(@NotNull OdinOrBreakExpression o) {
        super.visitOrBreakExpression(o);
    }

    @Override
    public void visitOrContinueExpression(@NotNull OdinOrContinueExpression o) {
        super.visitOrContinueExpression(o);
    }

    @Override
    public void visitOrReturnExpression(@NotNull OdinOrReturnExpression o) {
        super.visitOrReturnExpression(o);
    }

    @Override
    public void visitParExpressionType(@NotNull OdinParExpressionType o) {
        super.visitParExpressionType(o);
    }

    @Override
    public void visitParenthesizedExpression(@NotNull OdinParenthesizedExpression o) {
        super.visitParenthesizedExpression(o);
    }

    @Override
    public void visitProcedureExpression(@NotNull OdinProcedureExpression o) {
        super.visitProcedureExpression(o);
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

    public record Q() {

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

    private void getScopeForIdentifier(OdinIdentifier odinIdentifier) {
    }

    @Override
    public void visitSliceExpression(@NotNull OdinSliceExpression o) {
        super.visitSliceExpression(o);
    }

    @Override
    public void visitTagStatementExpression(@NotNull OdinTagStatementExpression o) {
        super.visitTagStatementExpression(o);
    }

    @Override
    public void visitTernaryIfExpression(@NotNull OdinTernaryIfExpression o) {
        super.visitTernaryIfExpression(o);
    }

    @Override
    public void visitTernaryWhenExpression(@NotNull OdinTernaryWhenExpression o) {
        super.visitTernaryWhenExpression(o);
    }

    @Override
    public void visitTransmuteExpression(@NotNull OdinTransmuteExpression o) {
        super.visitTransmuteExpression(o);
    }

    @Override
    public void visitTripleDashLiteralExpression(@NotNull OdinTripleDashLiteralExpression o) {
        super.visitTripleDashLiteralExpression(o);
    }

    @Override
    public void visitTypeAssertionExpression(@NotNull OdinTypeAssertionExpression o) {
        super.visitTypeAssertionExpression(o);
    }

    @Override
    public void visitTypeDefinitionExpression(@NotNull OdinTypeDefinitionExpression o) {
        super.visitTypeDefinitionExpression(o);
    }

    @Override
    public void visitUnaryAndExpression(@NotNull OdinUnaryAndExpression o) {
        super.visitUnaryAndExpression(o);
    }

    @Override
    public void visitUnaryDotExpression(@NotNull OdinUnaryDotExpression o) {
        super.visitUnaryDotExpression(o);
    }

    @Override
    public void visitUnaryMinusExpression(@NotNull OdinUnaryMinusExpression o) {
        super.visitUnaryMinusExpression(o);
    }

    @Override
    public void visitUnaryNotExpression(@NotNull OdinUnaryNotExpression o) {
        super.visitUnaryNotExpression(o);
    }

    @Override
    public void visitUnaryPlusExpression(@NotNull OdinUnaryPlusExpression o) {
        super.visitUnaryPlusExpression(o);
    }

    @Override
    public void visitUnaryRangeExpression(@NotNull OdinUnaryRangeExpression o) {
        super.visitUnaryRangeExpression(o);
    }

    @Override
    public void visitUnaryTildeExpression(@NotNull OdinUnaryTildeExpression o) {
        super.visitUnaryTildeExpression(o);
    }

    @Override
    public void visitUninitializedExpression(@NotNull OdinUninitializedExpression o) {
        super.visitUninitializedExpression(o);
    }

    public Optional<OdinDeclaredIdentifier> findDeclaredIdentifier(List<OdinDeclaredIdentifier> scope, @NotNull String name) {
        for (OdinDeclaredIdentifier odinDeclaredIdentifier : scope) {
            if (odinDeclaredIdentifier.getText().equals(name))
                return Optional.of(odinDeclaredIdentifier);
        }
        return Optional.empty();
    }
}
