package com.lasagnerd.odin.insights;

import com.lasagnerd.odin.lang.psi.*;

import static com.lasagnerd.odin.insights.OdinInsightUtils.*;

public class OdinReferenceResolver {
    public static OdinScope resolve(OdinScope scope, OdinExpression valueExpression) {

        TypeInferenceResult typeInferenceResult = OdinExpressionTypeResolver.inferType(scope, valueExpression);
        if (typeInferenceResult.isImport) {
            return getDeclarationsOfImportedPackage(scope, typeInferenceResult.importDeclarationStatement);
        }
        if(typeInferenceResult.type != null) {
            return getScopeProvidedByType(typeInferenceResult.type);
        }
        return OdinScope.EMPTY;
    }

}


