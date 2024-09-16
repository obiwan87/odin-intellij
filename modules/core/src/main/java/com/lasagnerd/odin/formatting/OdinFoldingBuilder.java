package com.lasagnerd.odin.formatting;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.lasagnerd.odin.lang.psi.OdinPsiElement;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import com.lasagnerd.odin.lang.psi.OdinVisitor;
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
            public void visitPsiElement(@NotNull OdinPsiElement psiElement) {
                if (psiElement.getChildren().length >= 2) {
                    if (psiElement.getFirstChild().getNode().getElementType() == OdinTypes.BLOCK_START) {
                        if (psiElement.getLastChild().getNode().getElementType() == OdinTypes.BLOCK_END) {
                            descriptors.add(new FoldingDescriptor(psiElement.getNode(), psiElement.getTextRange(), null));
                        }
                    }
                }

                for (PsiElement child : psiElement.getChildren()) {
                    child.accept(this);
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
        return "{...}";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
