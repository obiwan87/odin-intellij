package com.lasagnerd.odin.codeInsight;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.lang.OdinLexerAdapter;
import com.lasagnerd.odin.lang.OdinParserDefinition;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinFindUsagesProvider implements FindUsagesProvider {

    @Override
    public @Nullable WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(
                new OdinLexerAdapter(), TokenSet.create(OdinTypes.IDENTIFIER_TOKEN),
                OdinParserDefinition.COMMENT_TOKENS,
                OdinParserDefinition.STRING_LITERAL_ELEMENTS
        );
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return PsiTreeUtil.getParentOfType(psiElement, false, OdinDeclaration.class) != null;
    }

    @Override
    public @Nullable @NonNls String getHelpId(@NotNull PsiElement psiElement) {
        return "";
    }

    @Override
    public @Nls @NotNull String getType(@NotNull PsiElement psiElement) {
        OdinDeclaredIdentifier declaredIdentifier = PsiTreeUtil.getParentOfType(psiElement, false, OdinDeclaredIdentifier.class);

        if(declaredIdentifier == null) {
            if(psiElement instanceof OdinImportDeclarationStatement) {
                return getHumanReadableName(OdinSymbolType.PACKAGE_REFERENCE);
            }
            return getHumanReadableName(OdinSymbolType.UNKNOWN);
        }
        OdinSymbolType symbolType = OdinInsightUtils.classify(declaredIdentifier);
        return symbolType != null ? getHumanReadableName(symbolType) : getHumanReadableName(OdinSymbolType.UNKNOWN);
    }

    private String getHumanReadableName(@NotNull OdinSymbolType symbolType) {
        return switch (symbolType) {
            case UNKNOWN -> "Unknown";
            case PARAMETER -> "Parameter";
            case FIELD -> "Field";
            case PROCEDURE -> "Procedure";
            case PROCEDURE_OVERLOAD -> "Procedure overload";
            case STRUCT -> "Struct";
            case UNION -> "Union";
            case ENUM_FIELD -> "Enum Field";
            case ENUM -> "Enum";
            case CONSTANT -> "Constant";
            case VARIABLE -> "Variable";
            case PACKAGE_REFERENCE -> "Package";
            case POLYMORPHIC_TYPE -> "Polymorphic Type";
            case LABEL -> "Label";
            case FOREIGN_IMPORT -> "Foreign Import";
            case SWIZZLE_FIELD -> "Swizzle Field";
            case BIT_SET -> "Bit set";
        };
    }

    @Override
    public @Nls @NotNull String getDescriptiveName(@NotNull PsiElement psiElement) {
        return psiElement.getText();
    }

    @Override
    public @Nls @NotNull String getNodeText(@NotNull PsiElement psiElement, boolean b) {
        return psiElement.getText();
    }
}
