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
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import com.lasagnerd.odin.lang.psi.OdinImportDeclarationStatement;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

public class OdinInsertImportHandler implements InsertHandler<LookupElement> {
    private final String sourcePackagePath;
    private final OdinImport odinImport;
    private final OdinFile sourceFile;

    public OdinInsertImportHandler(OdinImport odinImport, String sourcePackagePath, OdinFile sourceFile) {
        this.sourcePackagePath = sourcePackagePath;
        this.odinImport = odinImport;
        this.sourceFile = sourceFile;
    }


    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
        if (odinImport == null)
            return;

        if (sourcePackagePath.equals(odinImport.path())) {
            return;
        }
        if (sourceFile == null) {
            return;
        }

        String targetPackagePath = odinImport.path();

        String importPath = odinImport.fullImportPath();

        // Check if package is already imported
        for (OdinImportDeclarationStatement importStatement : sourceFile.getFileScope().getImportStatements()) {
            OdinImport importInfo = importStatement.getImportInfo();
            if (importInfo.path().equals(importPath)) {
                return;
            }
        }

        OdinFile odinFile = (OdinFile) context.getFile();

        OdinFileScope fileScope = odinFile.getFileScope();
        Project project = context.getProject();
        ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
            OdinImportUtils.insertImport(project,
                    odinImport.alias(),
                    importPath,
                    fileScope);
            Document document = manager.getDocument(odinFile);
            if (document != null) {
                manager.commitDocument(document);
            }
        }));

        CodeStyleManager.getInstance(project).reformat(fileScope);
    }
}
