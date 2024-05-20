package com.lasagnerd.odin.insights;

import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.insights.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;

import static com.lasagnerd.odin.insights.OdinImportUtils.getSymbolsOfImportedPackage;
import static com.lasagnerd.odin.insights.OdinInsightUtils.getScopeProvidedByType;

public class OdinReferenceResolver {
    public static OdinScope resolve(OdinScope scope, OdinExpression valueExpression) {
        // Add filter for referenceable elements
        TsOdinType type = OdinInferenceEngine.inferType(scope, valueExpression);
        return getScopeProvidedByType(type);
    }

    public static OdinScope resolve(OdinScope scope, OdinType type) {
        OdinQualifiedType qualifiedType = PsiTreeUtil.getParentOfType(type, false, OdinQualifiedType.class);
        if (qualifiedType != null) {
            return resolve(scope, qualifiedType);
        }
        return OdinScope.EMPTY;
    }

    public static OdinScope resolve(OdinScope scope, OdinQualifiedType qualifiedType) {
        OdinIdentifier identifier = qualifiedType.getIdentifier();
        OdinSymbol odinSymbol = scope.getSymbol(identifier.getIdentifierToken().getText());
        if (odinSymbol != null) {
            OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(odinSymbol.getDeclaredIdentifier(), false, OdinDeclaration.class);
            if (odinDeclaration instanceof OdinImportDeclarationStatement importDeclarationStatement) {
                return getSymbolsOfImportedPackage(scope.getPackagePath(), importDeclarationStatement);
            }
        }
        return OdinScope.EMPTY;
    }

}


