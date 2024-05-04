package com.lasagnerd.odin.insights;

import com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.insights.typeInference.OdinTypeInferenceResult;
import com.lasagnerd.odin.lang.psi.*;

import static com.lasagnerd.odin.insights.OdinInsightUtils.*;

public class OdinReferenceResolver {
    public static OdinScope resolve(OdinScope scope, OdinExpression valueExpression) {

        OdinTypeInferenceResult typeInferenceResult = OdinInferenceEngine.inferType(scope, valueExpression);
        if (typeInferenceResult.isImport()) {
            return getDeclarationsOfImportedPackage(scope, typeInferenceResult.getImportDeclarationStatement());
        }
        if(typeInferenceResult.getType() != null) {
            return getScopeProvidedByType(typeInferenceResult.getType());
        }
        return OdinScope.EMPTY;
    }

}


