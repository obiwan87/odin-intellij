package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.insights.typeInference.OdinTypeInferenceResult;
import com.lasagnerd.odin.lang.psi.*;

import static com.lasagnerd.odin.insights.OdinInsightUtils.*;

public class OdinReferenceResolver {
    public static OdinScope resolve(OdinScope scope, OdinExpression valueExpression) {
        // Add filter for referenceable elements
        OdinTypeInferenceResult typeInferenceResult = OdinInferenceEngine.inferType(scope, valueExpression);
        if (typeInferenceResult.isImport()) {
            return getDeclarationsOfImportedPackage(scope, typeInferenceResult.getImportDeclarationStatement());
        }
        if(typeInferenceResult.getType() != null) {
            return getScopeProvidedByType(typeInferenceResult.getType());
        }
        return OdinScope.EMPTY;
    }

    public static OdinScope resolve(OdinScope scope, OdinType type) {
        OdinQualifiedType qualifiedType = findFirstParentOfType(type, false, OdinQualifiedType.class);
        if(qualifiedType != null) {
            return resolve(scope, qualifiedType);
        }
        return OdinScope.EMPTY;
    }

    public static OdinScope resolve(OdinScope scope, OdinQualifiedType qualifiedType) {
        OdinIdentifier identifier = qualifiedType.getIdentifier();
        PsiNamedElement namedElement = scope.getNamedElement(identifier.getIdentifierToken().getText());
        OdinDeclaration odinDeclaration = OdinInsightUtils.findFirstParentOfType(namedElement, false, OdinDeclaration.class);
        if(odinDeclaration instanceof OdinImportDeclarationStatement importDeclarationStatement) {
            return getDeclarationsOfImportedPackage(scope, importDeclarationStatement);
        }
        return OdinScope.EMPTY;
    }

}


