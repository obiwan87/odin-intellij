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
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdinFoldingBuilder extends FoldingBuilderEx {

    public static final Pattern REGION_PATTERN = Pattern.compile("^region (.*)$");

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {

        List<FoldingDescriptor> descriptors = new ArrayList<>();
        List<CodeRegion> codeRegions = new ArrayList<>();

        OdinVisitor odinVisitor = new OdinVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement psiElement) {
                if (psiElement instanceof PsiComment) {
                    IElementType elementType = PsiUtilCore.getElementType(psiElement);
                    if (elementType == OdinTypes.MULTILINE_BLOCK_COMMENT) {
                        descriptors.add(new FoldingDescriptor(psiElement.getNode(), psiElement.getTextRange(), null));
                    }

                    if (elementType == OdinTypes.LINE_COMMENT) {
                        String region = psiElement.getText().trim().replaceAll("^//", "").trim();

                        if (region.equals("endregion")) {
                            if (!codeRegions.isEmpty()) {
                                CodeRegion last = codeRegions.getLast();
                                Objects.requireNonNull(last.getStart());
                                if (last.getEnd() != null) {
                                    throw new IllegalStateException("Region with set end found in code regions");
                                }

                                last.setEnd(psiElement);
                                FoldingDescriptor foldingDescriptor = new FoldingDescriptor(
                                        last.start,
                                        last.start.getTextRange().getStartOffset(),
                                        psiElement.getTextRange().getEndOffset(),
                                        null,
                                        "// region " + last.getName()
                                );

                                codeRegions.remove(last);
                                descriptors.add(foldingDescriptor);
                            }
                        } else {
                            Matcher matcher = REGION_PATTERN.matcher(region);
                            if (matcher.find()) {
                                String regionName = matcher.group(1);
                                CodeRegion codeRegion = new CodeRegion(regionName.trim(), psiElement);
                                codeRegions.add(codeRegion);
                            }
                        }
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

    @Data
    static class CodeRegion {
        String name;
        PsiElement start;
        PsiElement end;

        public CodeRegion(String name, PsiElement start) {
            this.name = name;
            this.start = start;
        }

        boolean isValid() {
            return name != null && start != null && end != null;
        }
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
