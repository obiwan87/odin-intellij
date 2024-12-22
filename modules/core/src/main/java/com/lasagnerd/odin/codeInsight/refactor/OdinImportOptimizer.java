package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.lang.ImportOptimizer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinImportStatement;
import com.lasagnerd.odin.lang.psi.OdinImportStatementsContainer;
import com.lasagnerd.odin.lang.psi.OdinPsiElementFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OdinImportOptimizer implements ImportOptimizer {
    @Override
    public boolean supports(@NotNull PsiFile file) {
        return file instanceof OdinFile;
    }

    @Override
    public @NotNull Runnable processFile(@NotNull PsiFile file) {
        return () -> {
            OdinFile odinFile = (OdinFile) file;
            List<OdinImportStatement> importStatements = odinFile.getFileScope().getImportStatements();

            // Keep used imports
            List<OdinImportStatement> usedImports = importStatements.stream()
                    .filter(importDeclarationStatement -> !OdinImportUtils.isUnusedImport(importDeclarationStatement))
                    .sorted(Comparator.comparing(i -> i.getImportDeclaration().getImportInfo().fullImportPath()))
                    .collect(Collectors.toList());

            // Replace old import statements container with new one
            OdinImportStatementsContainer importStatementsContainer = odinFile.getFileScope().getImportStatementsContainer();

            // If there are no used imports remove everything
            if (usedImports.isEmpty()) {
                importStatements.forEach(PsiElement::delete);
                if (importStatementsContainer != null)
                    importStatementsContainer.delete();
                CodeStyleManager
                        .getInstance(file.getProject())
                        .reformat(odinFile.getFileScope().getPackageClause());
                return;
            }

            // Create new import statements container with sorted imports
            OdinImportStatementsContainer templateOdinStatementsContainer = OdinPsiElementFactory
                    .getInstance(file.getProject())
                    .createImportStatementsContainer(usedImports);
            PsiElement replacement;
            if (importStatementsContainer != null) {
                replacement = importStatementsContainer.replace(templateOdinStatementsContainer);
            } else {
                replacement = odinFile.getFileScope().addAfter(templateOdinStatementsContainer, odinFile.getFileScope().getEos());
            }
            // At this point we have created an imports container with all the used import. All other imports can be removed
            importStatements.forEach(PsiElement::delete);

            // Reformat import statements container
            CodeStyleManager
                    .getInstance(file.getProject())
                    .reformat(replacement);
        };
    }
}

