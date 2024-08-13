package com.lasagnerd.odin.codeInsight.annotators.buildAnnotator;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdinBuildErrorsExternalAnnotator extends ExternalAnnotator<PsiFile, OdinBuildErrorResult> {

    private final OdinBuildProcessRunner odinBuildProcessRunner = OdinBuildProcessRunner.getInstance();

    @Override
    public @Nullable PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Override
    public @Nullable OdinBuildErrorResult doAnnotate(PsiFile collectedInfo) {
        Project project = collectedInfo.getProject();
        Notifier.notify("in doAnnotate");
        if (!OdinBuildProcessRunner.canRunOdinBuild(project)) {
            return null;
        }
        // save all documents for 'odin build .' to work
        ApplicationManager.getApplication().invokeAndWait(() -> FileDocumentManager.getInstance().saveAllDocuments());
        OdinBuildProcessRunner.getInstance().buildAndUpdateErrors(collectedInfo.getProject());
        return OdinBuildProcessRunner.getInstance().getErrors();
    }

    @Override
    public void apply(@NotNull PsiFile file, @Nullable OdinBuildErrorResult annotationResult, @NotNull AnnotationHolder holder) {
        if (annotationResult == null) {
            return;
        }
        if (odinBuildProcessRunner.getErrors() == null) {
            return;
        }
        String realFilePath = file.getVirtualFile().getPath();
        List<OdinBuildErrorResult.ErrorDetails> list = odinBuildProcessRunner.getErrors().getErrors()
                .stream()
                .filter(error -> error.getPos().getFile().equals(realFilePath))
                .toList();
        list.forEach(error -> {
            int column = error.getPos().getColumn();
            int endColumn = error.getPos().getEndColumn();
            TextRange errorRange = new TextRange(error.getPos().getOffset(), error.getPos().getOffset() + (endColumn - column));

            boolean isWarning = error.getType().equals("warning");
            HighlightSeverity severity =
                    isWarning ? HighlightSeverity.WARNING : HighlightSeverity.ERROR;
            ProblemHighlightType highlightType =
                    isWarning ? ProblemHighlightType.WARNING : ProblemHighlightType.ERROR;
            TextAttributesKey attributes =
                    isWarning ? CodeInsightColors.WARNINGS_ATTRIBUTES : CodeInsightColors.ERRORS_ATTRIBUTES;

            String message = error.getMsgs().get(0);
            String tooltip = String.join("\n", error.getMsgs());
            holder.newAnnotation(severity, message)
                    .tooltip(tooltip)
                    .range(errorRange)
                    .highlightType(highlightType)
                    .textAttributes(attributes)
                    .create();
        });
    }

}
