package com.lasagnerd.odin.codeInsight;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.lang.OdinLexerAdapter;
import com.lasagnerd.odin.lang.OdinSyntaxHighlighter;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinImportStatement;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.*;

public class OdinFindUsagesProvider implements FindUsagesProvider {

    @Override
    public @Nullable WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(
                new OdinLexerAdapter(), TokenSet.create(OdinTypes.IDENTIFIER_TOKEN),
                OdinSyntaxHighlighter.COMMENT_TOKENS,
                OdinSyntaxHighlighter.STRING_LITERAL_ELEMENTS
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
            if (psiElement instanceof OdinImportStatement) {
                return getHumanReadableName(OdinSymbolType.PACKAGE_REFERENCE);
            }
            return getHumanReadableName(OdinSymbolType.UNKNOWN);
        }
        OdinSymbolType symbolType = OdinInsightUtils.classify(declaredIdentifier);
        return symbolType != null ? getHumanReadableName(symbolType) : getHumanReadableName(OdinSymbolType.UNKNOWN);
    }

    @Contract(pure = true)
    private @NotNull String getHumanReadableName(@NotNull OdinSymbolType symbolType) {
        return switch (symbolType) {
            case UNKNOWN -> "Unknown";
            case TYPE_ALIAS -> "Type Alias";
            case BUILTIN_TYPE -> "Built-In type";
            case PARAMETER -> "Parameter";
            case STRUCT_FIELD -> "Struct Field";
            case BIT_FIELD_FIELD -> "Bit-Field Field";
            case ALLOCATOR_FIELD -> "Allocator Field";
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
            case BIT_FIELD -> "Bit Field";
            case OBJC_CLASS -> "Objective-C Class";
            case OBJC_MEMBER -> "Objective-C Member";
            case SOA_FIELD -> "Soa Field";
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
