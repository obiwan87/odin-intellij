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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdinMissingImportInspection extends LocalInspectionTool {
    private static final Pattern PACKAGE_LINE = Pattern.compile("(?m)^\\s*package\\s+\\w+.*$");

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        List<ProblemDescriptor> descriptors = new ArrayList<>();
        for (OdinMissingImportAnalyzer.MissingImport missing : OdinMissingImportAnalyzer.analyze(file.getText())) {
            PsiElement element = file.findElementAt(missing.offset());
            if (element != null) {
                descriptors.add(manager.createProblemDescriptor(element,
                        "Package '" + missing.alias() + "' is not imported", isOnTheFly,
                        new LocalQuickFix[]{new AddImportFix(missing.importPath())},
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
            }
        }
        return descriptors.toArray(ProblemDescriptor.EMPTY_ARRAY);
    }

    private record AddImportFix(String importPath) implements LocalQuickFix {
        @Override
        public @NotNull String getFamilyName() {
            return "Import \"" + importPath + "\"";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            Document document = descriptor.getPsiElement().getContainingFile().getViewProvider().getDocument();
            if (document == null) {
                return;
            }
            Matcher packageLine = PACKAGE_LINE.matcher(document.getText());
            if (packageLine.find()) {
                document.insertString(packageLine.end(), "\n\nimport \"" + importPath + "\"");
            }
        }
    }
}
