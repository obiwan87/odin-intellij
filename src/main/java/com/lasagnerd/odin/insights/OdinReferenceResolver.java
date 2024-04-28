package com.lasagnerd.odin.insights;

import com.lasagnerd.odin.lang.psi.*;

import static com.lasagnerd.odin.insights.OdinInsightUtils.*;

public class OdinReferenceResolver {
    public static Scope resolve(Scope scope, OdinExpression valueExpression) {

        ExpressionTypeInference expressionTypeInference = new ExpressionTypeInference(scope);
        valueExpression.accept(expressionTypeInference);
        if (expressionTypeInference.isImport) {
            return getDeclarationsOfImportedPackage(scope, expressionTypeInference.importDeclarationStatement);
        }
        if(expressionTypeInference.type != null) {
            return getScopeProvidedByType(expressionTypeInference.type);
        }
        return Scope.EMPTY;
    }

}


