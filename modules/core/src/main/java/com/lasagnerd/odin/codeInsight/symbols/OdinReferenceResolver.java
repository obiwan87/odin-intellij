package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinEnumType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;

import static com.lasagnerd.odin.codeInsight.imports.OdinImportUtils.getSymbolsOfImportedPackage;

public class OdinReferenceResolver {
    public static OdinSymbolTable resolve(OdinSymbolTable symbolTable, OdinExpression valueExpression) {
        // Add filter for referenceable elements
        TsOdinType type = OdinInferenceEngine.inferType(symbolTable, valueExpression);
        if(type instanceof TsOdinMetaType metaType) {
            TsOdinType tsOdinType = metaType.representedType().baseType(true);
            if(tsOdinType instanceof TsOdinEnumType) {
                return OdinInsightUtils.getTypeElements(valueExpression.getProject(), tsOdinType);
            }
        }
        return OdinInsightUtils.getTypeElements(valueExpression.getProject(), type, true);
    }

    public static OdinSymbolTable resolve(OdinSymbolTable symbolTable, OdinType type) {
        OdinQualifiedType qualifiedType = PsiTreeUtil.getParentOfType(type, false, OdinQualifiedType.class);
        if (qualifiedType != null) {
            return resolve(symbolTable, qualifiedType);
        }
        return OdinSymbolTable.EMPTY;
    }

    public static OdinSymbolTable resolve(OdinSymbolTable symbolTable, OdinQualifiedType qualifiedType) {
        OdinIdentifier identifier = qualifiedType.getIdentifier();
        OdinSymbol odinSymbol = symbolTable.getSymbol(identifier.getIdentifierToken().getText());
        if (odinSymbol != null) {
            OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(odinSymbol.getDeclaredIdentifier(), false, OdinDeclaration.class);
            if (odinDeclaration instanceof OdinImportDeclarationStatement importDeclarationStatement) {
                return getSymbolsOfImportedPackage(symbolTable.getPackagePath(), importDeclarationStatement);
            }
        }
        return OdinSymbolTable.EMPTY;
    }

}


