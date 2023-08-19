package com.lasagnerd.odin.lang;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinBraceMatcher implements PairedBraceMatcher {

    @Override
    public BracePair @NotNull [] getPairs() {
        return new BracePair[] {
                new BracePair(OdinTypes.LPAREN, OdinTypes.RPAREN, false),
                new BracePair(OdinTypes.LBRACKET, OdinTypes.RBRACKET, false),
                new BracePair(OdinTypes.LBRACE, OdinTypes.RBRACE, false),
        };
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType context) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile psiFile, int offset) {
        PsiElement psiElement = psiFile.findElementAt(offset);

        if(psiElement != null && psiElement.getParent() != null) {
            return psiElement.getParent().getTextRange().getStartOffset();
        }
        return offset;
    }
}
