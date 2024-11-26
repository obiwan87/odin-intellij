package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinEnumType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinPackageReferenceType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;

import static com.lasagnerd.odin.codeInsight.imports.OdinImportUtils.getSymbolsOfImportedPackage;

public class OdinReferenceResolver {
    public static OdinSymbolTable resolve(OdinExpression valueExpression) {
        // Add filter for referenceable elements
        TsOdinType type = valueExpression.getInferredType();
        if (type instanceof TsOdinMetaType metaType) {
            TsOdinType tsOdinType = metaType.representedType().baseType(true);
            if (tsOdinType instanceof TsOdinEnumType) {
                return OdinInsightUtils.getTypeElements(valueExpression.getProject(), tsOdinType);
            }
        }
        if (type instanceof TsOdinPackageReferenceType packageReferenceType) {
            return OdinInsightUtils.getPackageReferenceSymbols(valueExpression.getProject(),
                    packageReferenceType,
                    false);
        }
        return OdinInsightUtils.getTypeElements(valueExpression.getProject(), type, true);
    }

    public static OdinSymbolTable resolve(OdinType type) {
        OdinQualifiedType qualifiedType = PsiTreeUtil.getParentOfType(type, false, OdinQualifiedType.class);
        if (qualifiedType != null) {
            return resolve(qualifiedType);
        }
        return OdinSymbolTable.EMPTY;
    }

    public static OdinSymbolTable resolve(OdinQualifiedType qualifiedType) {
        OdinIdentifier identifier = qualifiedType.getIdentifier();
        OdinSymbol odinSymbol = identifier.getReferencedSymbol();
        if (odinSymbol != null) {
            OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(odinSymbol.getDeclaredIdentifier(), false, OdinDeclaration.class);
            if (odinDeclaration instanceof OdinImportDeclarationStatement importDeclarationStatement) {
                return getSymbolsOfImportedPackage(OdinImportService.getInstance(qualifiedType.getProject()).getPackagePath(qualifiedType), importDeclarationStatement);
            }
        }
        return OdinSymbolTable.EMPTY;
    }

}


