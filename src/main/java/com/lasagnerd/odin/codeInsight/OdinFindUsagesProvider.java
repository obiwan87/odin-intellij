package com.lasagnerd.odin.codeInsight;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
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

import static com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType.*;

public class OdinFindUsagesProvider implements FindUsagesProvider {

    public static boolean isVariableDeclaration(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, true, OdinVariableDeclarationStatement.class) != null
                || PsiTreeUtil.getParentOfType(element, true, OdinVariableInitializationStatement.class) != null;
    }

    public static boolean isProcedureDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinProcedureDeclarationStatement;
    }

    public static boolean isProcedureOverloadDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinProcedureOverloadDeclarationStatement;
    }

    public static boolean isConstantDeclaration(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, true, OdinConstantInitializationStatement.class) != null;
    }

    public static boolean isStructDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinStructDeclarationStatement;
    }

    public static boolean isEnumDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinEnumDeclarationStatement;
    }

    public static boolean isUnionDeclaration(PsiElement element) {
        return element.getParent() instanceof OdinUnionDeclarationStatement;
    }

    private static boolean isFieldDeclaration(PsiNamedElement element) {
        return element.getParent() instanceof OdinFieldDeclarationStatement;
    }

    private static boolean isPackageDeclaration(PsiNamedElement element) {
        return element instanceof OdinImportDeclarationStatement
                || element.getParent() instanceof OdinImportDeclarationStatement;
    }

    public static OdinSymbolType classify(PsiNamedElement element) {
        if (isStructDeclaration(element)) {
            return STRUCT;
        } else if (isEnumDeclaration(element)) {
            return ENUM;
        } else if (isUnionDeclaration(element)) {
            return UNION;
        } else if (isProcedureDeclaration(element)) {
            return PROCEDURE;
        } else if (isVariableDeclaration(element)) {
            return VARIABLE;
        } else if (isConstantDeclaration(element)) {
            return CONSTANT;
        } else if (isProcedureOverloadDeclaration(element)) {
            return PROCEDURE_OVERLOAD;
        } else if (isPackageDeclaration(element)) {
            return PACKAGE_REFERENCE;
        } else if (isFieldDeclaration(element)) {
            return FIELD;
        } else if (isParameterDeclaration(element)) {
            return PARAMETER;
        } else {
            return UNKNOWN;
        }
    }

    public static boolean isParameterDeclaration(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, true, OdinDeclaration.class) instanceof OdinParameterDeclaration;
    }

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
        OdinSymbolType symbolType = classify(declaredIdentifier);
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
