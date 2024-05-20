package com.lasagnerd.odin.insights;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.OdinLexerAdapter;
import com.lasagnerd.odin.lang.OdinParserDefinition;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinTypes;
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

        OdinTypeType typeType = OdinInsightUtils.classify(declaredIdentifier);
        return typeType != null ? typeType.getHumanReadableName() : OdinTypeType.UNKNOWN.getHumanReadableName();
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
