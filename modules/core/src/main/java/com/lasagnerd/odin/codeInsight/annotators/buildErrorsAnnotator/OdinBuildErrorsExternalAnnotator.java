package com.lasagnerd.odin.codeInsight.annotators.buildErrorsAnnotator;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.lasagnerd.odin.projectSettings.OdinProjectSettingsService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdinBuildErrorsExternalAnnotator extends ExternalAnnotator<PsiFile, OdinBuildErrorResult> {

    @Override
    public @Nullable PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Override
    public @Nullable OdinBuildErrorResult doAnnotate(PsiFile file) {
        if(!OdinProjectSettingsService.getInstance(file.getProject()).isOdinCheckerEnabled())
            return null;

        Project project = file.getProject();
        if (!OdinBuildProcessRunner.canRunOdinBuild(project)) {
            return null;
        }

        ApplicationManager.getApplication().invokeAndWait(() -> FileDocumentManager.getInstance().saveAllDocuments());
        return OdinBuildProcessRunner.getInstance().buildAndUpdateErrors(file.getProject(), file);
    }

    @Override
    public void apply(@NotNull PsiFile file, @Nullable OdinBuildErrorResult buildErrorResult, @NotNull AnnotationHolder holder) {
        if (buildErrorResult == null) {
            return;
        }

        String realFilePath = file.getVirtualFile().getPath();
        List<OdinBuildErrorResult.ErrorDetails> errorDetails = buildErrorResult
                .getErrors()
                .stream()
                .filter(error -> error.getPos() != null)
                .filter(error -> error.getPos().getFile().equals(realFilePath))
                .toList();

        for (OdinBuildErrorResult.ErrorDetails error : errorDetails) {
            int column = error.getPos().getColumn();
            int endColumn = error.getPos().getEndColumn();
            int lineStartOffset = file.getFileDocument().getLineStartOffset(error.getPos().getLine() - 1);

            TextRange errorRange = new TextRange(lineStartOffset + column - 1, lineStartOffset + endColumn);

            boolean isWarning = error.getType().equals("warning");
            HighlightSeverity severity =
                    isWarning ? HighlightSeverity.WARNING : HighlightSeverity.ERROR;
            ProblemHighlightType highlightType =
                    isWarning ? ProblemHighlightType.WARNING : ProblemHighlightType.ERROR;
            TextAttributesKey attributes =
                    isWarning ? CodeInsightColors.WARNINGS_ATTRIBUTES : CodeInsightColors.ERRORS_ATTRIBUTES;

            String message = error.getMsgs().getFirst();
            // replace tab with 4 spaces for HTML formatting
            String tooltip = String.join("<br>", error.getMsgs()).replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
            holder.newAnnotation(severity, message)
                    .tooltip(tooltip)
                    .range(errorRange)
                    .highlightType(highlightType)
                    .textAttributes(attributes)
                    .create();
        }
    }

}
