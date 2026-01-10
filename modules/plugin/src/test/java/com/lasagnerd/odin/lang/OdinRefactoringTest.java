package com.lasagnerd.odin.lang;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class OdinRefactoringTest extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/refactoringTestData";
    }

    public void testDummy() {
        myFixture.configureByFiles("introduce_variable.odin");
        PsiFile file = myFixture.getFile();
        System.out.println(file.getFileType());
    }
}
