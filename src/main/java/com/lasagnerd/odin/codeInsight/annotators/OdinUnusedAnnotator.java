package com.lasagnerd.odin.codeInsight.annotators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import com.lasagnerd.odin.codeInsight.OdinImportUtils;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

public class OdinUnusedAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        VirtualFile sourceRootForFile = ProjectFileIndex.getInstance(element.getProject())
                .getSourceRootForFile(element.getContainingFile().getVirtualFile());
        if (sourceRootForFile == null || !VfsUtilCore.isAncestor(sourceRootForFile,
                element.getContainingFile().getVirtualFile(),
                false))
            return;

        if (element instanceof OdinImportDeclarationStatement importDeclarationStatement) {
            if (OdinImportUtils.isUnusedImport(importDeclarationStatement)) {
                // annotated entire import statement
                holder.newAnnotation(HighlightSeverity.WARNING, "Unused import statement")
                        .range(importDeclarationStatement.getTextRange())
                        .highlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL)
                        .create();

            }
        } else {
            return;
        }

        if (!(element instanceof OdinDeclaredIdentifier declaredIdentifier))
            return;

        OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(declaredIdentifier, false, OdinDeclaration.class);

        if (odinDeclaration instanceof OdinPackageDeclaration) {
            return;
        }

        if (odinDeclaration instanceof OdinImportDeclarationStatement) {
            return;
        }

        if (odinDeclaration instanceof OdinProcedureDeclarationStatement) {
            if (declaredIdentifier.getText().equals("main")) {
                return;
            }
        }

        boolean hasNoUsage = hasNoUsage(declaredIdentifier);
        if (hasNoUsage) {
            holder.newAnnotation(HighlightSeverity.WARNING, "Unused identifier")
                    .range(declaredIdentifier.getTextRange())
                    .highlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL)
                    .create();
        }
    }

    private static boolean hasNoUsage(OdinDeclaredIdentifier declaredIdentifier) {
        Query<PsiReference> search = ReferencesSearch.search(declaredIdentifier);
        return search.findFirst() == null;
    }

}
