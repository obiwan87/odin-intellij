package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import com.lasagnerd.odin.lang.psi.OdinImportStatement;
import org.jetbrains.annotations.NotNull;

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

        String importPath = odinImport.fullImportPath();

        // Check if package is already imported
        for (OdinImportStatement importStatement : sourceFile.getFileScope().getImportStatements()) {
            OdinImport importInfo = importStatement.getImportDeclaration().getImportInfo();
            if (importInfo.fullImportPath().equals(this.odinImport.fullImportPath())) {
                return;
            }
        }

        OdinFile odinFile = (OdinFile) context.getFile();

        OdinFileScope fileScope = odinFile.getFileScope();
        Project project = context.getProject();
        ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
            PsiElement psiElement = OdinImportUtils.insertImport(project,
                    odinImport.alias(),
                    importPath,
                    fileScope);
            CodeStyleManager.getInstance(project).reformat(psiElement);

        }));

//        CodeStyleManager.getInstance(project).reformat(fileScope);
    }
}
