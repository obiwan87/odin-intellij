package com.lasagnerd.odin.lang;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.AbstractInplaceIntroduceTest;
import com.intellij.refactoring.introduce.PsiIntroduceTarget;
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser;
import com.lasagnerd.odin.codeInsight.refactor.OdinVariableIntroducer;
import com.lasagnerd.odin.lang.psi.OdinMulExpression;

public class OdinIntroduceVariableTest extends AbstractInplaceIntroduceTest {
    public OdinIntroduceVariableTest() {
        myTestDataPath = "src/test/";
    }

    @Override
    protected String getBasePath() {
        return "refactoringTestData/";
    }

    @Override
    protected String getExtension() {
        return ".odin";
    }

    @Override
    protected OdinVariableIntroducer invokeRefactoring() {
        PsiFile file = getFile();
        OdinMulExpression mulExpression = PsiTreeUtil.findChildOfType(file, OdinMulExpression.class);

        if (mulExpression != null) {
            return OdinVariableIntroducer.createVariableIntroducer(
                    new PsiIntroduceTarget<>(mulExpression),
                    OccurrencesChooser.ReplaceChoice.NO,
                    getEditor(),
                    getProject()
            );
        }

        throw new IllegalStateException("MulExpression not found");
    }

    public void testIntroduce_variable() {
        doTest(abstractInplaceIntroducer -> {

        });
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
