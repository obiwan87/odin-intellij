package com.lasagnerd.odin.codeInsight.memory;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OdinMemorySafetyInspection extends LocalInspectionTool {
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        List<ProblemDescriptor> descriptors = new ArrayList<>();
        for (OdinMemoryAnalyzer.Problem problem : OdinMemoryAnalyzer.analyze(file.getText())) {
            PsiElement element = file.findElementAt(problem.offset());
            if (element == null) {
                continue;
            }
            LocalQuickFix[] fixes = problem.kind() == OdinMemoryAnalyzer.Kind.LEAK
                    ? new LocalQuickFix[]{new AddDeferredReleaseFix(problem.deleter(), problem.name())}
                    : LocalQuickFix.EMPTY_ARRAY;
            descriptors.add(manager.createProblemDescriptor(element, problem.message(), isOnTheFly, fixes,
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
        }
        return descriptors.toArray(ProblemDescriptor.EMPTY_ARRAY);
    }

    private record AddDeferredReleaseFix(String deleter, String name) implements LocalQuickFix {
        @Override
        public @NotNull String getFamilyName() {
            return "Add 'defer " + deleter + "(" + name + ")'";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            Document document = element.getContainingFile().getViewProvider().getDocument();
            if (document == null) {
                return;
            }
            int line = document.getLineNumber(element.getTextOffset());
            int start = document.getLineStartOffset(line);
            int end = document.getLineEndOffset(line);
            String text = document.getText().substring(start, end);
            String indent = text.substring(0, text.length() - text.stripLeading().length());
            document.insertString(end, "\n" + indent + "defer " + deleter + "(" + name + ")");
        }
    }
}
