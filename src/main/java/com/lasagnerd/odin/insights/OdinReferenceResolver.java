package com.lasagnerd.odin.insights;

import com.lasagnerd.odin.lang.psi.*;

import static com.lasagnerd.odin.insights.OdinInsightUtils.*;

public class OdinReferenceResolver {
    public static Scope resolve(Scope scope, OdinExpression valueExpression) {

        OdinExpressionTypeResolver odinExpressionTypeResolver = new OdinExpressionTypeResolver(scope);
        valueExpression.accept(odinExpressionTypeResolver);
        if (odinExpressionTypeResolver.isImport) {
            return getDeclarationsOfImportedPackage(scope, odinExpressionTypeResolver.importDeclarationStatement);
        }
        if(odinExpressionTypeResolver.type != null) {
            return getScopeProvidedByType(odinExpressionTypeResolver.type);
        }
        return Scope.EMPTY;
    }

}


