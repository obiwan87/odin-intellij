package com.lasagnerd.odin.testCreator;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testIntegration.TestCreator;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;

public class OdinTestCreator implements TestCreator {
    @Override
    public boolean isAvailable(Project project, Editor editor, PsiFile file) {
        VirtualFile containingVirtualFile = OdinInsightUtils.getContainingVirtualFile(file);
        return containingVirtualFile.getNameWithoutExtension().endsWith("_test");
    }

    @Override
    public void createTest(Project project, Editor editor, PsiFile file) {

    }
}
