package com.lasagnerd.odin.formatting;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OdinFoldingBuilder extends FoldingBuilderEx {
    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {

        List<FoldingDescriptor> descriptors = new ArrayList<>();

        OdinVisitor odinVisitor = new OdinVisitor() {

            @Override
            public void visitElement(@NotNull PsiElement psiElement) {
                if (psiElement instanceof PsiComment) {
                    IElementType elementType = PsiUtilCore.getElementType(psiElement);
                    if (elementType == OdinTypes.MULTILINE_BLOCK_COMMENT) {
                        descriptors.add(new FoldingDescriptor(psiElement.getNode(), psiElement.getTextRange(), null));
                    }
                }
            }

            @Override
            public void visitPsiElement(@NotNull OdinPsiElement psiElement) {

                if (psiElement.getChildren().length >= 2) {

                    if (psiElement instanceof OdinBlock
                            || psiElement instanceof OdinCompoundValue
                            || psiElement instanceof OdinProcedureGroupBlock
                            || psiElement instanceof OdinSwitchBody
                            || psiElement instanceof OdinForeignBlock
                            || psiElement instanceof OdinStructBlock
                            || psiElement instanceof OdinEnumBlock
                            || psiElement instanceof OdinUnionBlock
                            || psiElement instanceof OdinBitFieldBlock

                    ) {
                        descriptors.add(new FoldingDescriptor(psiElement.getNode(), psiElement.getTextRange(), null));
                    }
                }

                PsiElement elem = psiElement.getFirstChild();
                while (elem != null) {
                    if (!(elem instanceof PsiWhiteSpace)) {
                        elem.accept(this);
                    }
                    elem = elem.getNextSibling();
                }
            }

            @Override
            public void visitFile(@NotNull PsiFile file) {
                for (PsiElement child : file.getChildren()) {
                    child.accept(this);
                }
            }
        };

        root.accept(odinVisitor);

        return descriptors.toArray(new FoldingDescriptor[0]);
    }

    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        if (node.getElementType() == OdinTypes.MULTILINE_BLOCK_COMMENT) {
            return "/*...*/";
        }
        return "{...}";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
