package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.lang.ImportOptimizer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinImportDeclarationStatement;
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
            List<OdinImportDeclarationStatement> importStatements = odinFile.getFileScope().getImportStatements();

            // Keep used imports
            List<OdinImportDeclarationStatement> usedImports = importStatements.stream()
                    .filter(importDeclarationStatement -> !OdinImportUtils.isUnusedImport(importDeclarationStatement))
                    .sorted(Comparator.comparing(i -> i.getImportInfo().fullImportPath()))
                    .collect(Collectors.toList());

            // Replace old import statements container with new one
            OdinImportStatementsContainer importStatementsContainer = odinFile.getFileScope().getImportStatementsContainer();
            // TODO imports can be all over the place in file scope. Gather all of them and put the
            if (importStatementsContainer == null)
                return;

            if (usedImports.isEmpty()) {
                importStatementsContainer.delete();
                CodeStyleManager
                        .getInstance(file.getProject())
                        .reformat(odinFile.getFileScope().getPackageDeclaration());
            } else {
                // Create new import statements container with sorted imports
                OdinImportStatementsContainer templateOdinStatementsContainer = OdinPsiElementFactory
                        .getInstance(file.getProject())
                        .createImportStatementsContainer(usedImports);
                PsiElement replacement = importStatementsContainer.replace(templateOdinStatementsContainer);
                // Reformat import statements container
                CodeStyleManager
                        .getInstance(file.getProject())
                        .reformat(replacement);
            }
        };
    }
}

