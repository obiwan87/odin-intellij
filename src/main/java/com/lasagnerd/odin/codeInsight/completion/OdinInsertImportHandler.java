package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.lasagnerd.odin.codeInsight.OdinImportInfo;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import com.lasagnerd.odin.lang.psi.OdinImportDeclarationStatement;
import com.lasagnerd.odin.lang.psi.OdinPsiElementFactory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class OdinInsertImportHandler implements InsertHandler<LookupElement> {
    private final String sourcePackagePath;
    private final String targetPackagePath;
    private final OdinFile sourceFile;

    public OdinInsertImportHandler(String sourcePackagePath, String targetPackagePath, OdinFile sourceFile) {
        this.sourcePackagePath = sourcePackagePath;
        this.targetPackagePath = targetPackagePath;
        this.sourceFile = sourceFile;
    }


    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
        if (sourcePackagePath.equals(targetPackagePath)) {
            return;
        }
        if (sourceFile == null) {
            return;
        }

        String relativePath = FileUtil
                .toSystemIndependentName(Path.of(sourcePackagePath).relativize(Path.of(targetPackagePath)).toString());

        // Check if package is already imported
        for (OdinImportDeclarationStatement importStatement : sourceFile.getFileScope().getImportStatements()) {
            OdinImportInfo importInfo = importStatement.getImportInfo();
            if (importInfo.path().equals(relativePath))
            {
                return;
            }
        }

        OdinFile odinFile = (OdinFile) context.getFile();

        OdinFileScope fileScope = odinFile.getFileScope();
        Project project = context.getProject();
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
                OdinImportDeclarationStatement anImport = OdinPsiElementFactory.getInstance(project)
                        .createImport(relativePath);
                if (!fileScope.getImportStatements().isEmpty()) {
                    OdinImportDeclarationStatement odinImportDeclarationStatement = fileScope.getImportStatements().get(fileScope.getImportStatements().size() - 1);
                    fileScope.getImportStatementsContainer().addAfter(anImport, odinImportDeclarationStatement);
                } else {
                    fileScope.getImportStatementsContainer().add(anImport);
                }
                Document document = manager.getDocument(odinFile);
                if (document != null) {
                    manager.commitDocument(document);
                }
            });
        });

        CodeStyleManager.getInstance(project).reformat(fileScope);

    }
}
