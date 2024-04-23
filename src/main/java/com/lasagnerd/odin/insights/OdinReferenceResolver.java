package com.lasagnerd.odin.insights;

import com.lasagnerd.odin.lang.psi.*;

public class OdinReferenceResolver {
    public static Scope resolve(Scope scope, OdinExpression valueExpression) {

        ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(scope);
        valueExpression.accept(expressionTypeInference);
        if (expressionTypeInference.isImport) {
            return OdinInsightUtils.getDeclarationsOfImportedPackage(scope, expressionTypeInference.importDeclarationStatement);
        }
        if(expressionTypeInference.type != null) {
            return expressionTypeInference.type.getScope();
        }
        return ExpressionTypeInference.getCompletionScopeOfType(scope, expressionTypeInference.typeIdentifier);
    }

}


