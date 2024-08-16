package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.lang.psi.OdinExpressionStatement;
import com.lasagnerd.odin.lang.psi.OdinPsiElementFactory;
import com.lasagnerd.odin.lang.psi.OdinVariableInitializationStatement;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OdinIntroduceVariableQuickFix extends PsiElementBaseIntentionAction {

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Introduce variable";
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        OdinExpressionStatement odinExpressionStatement = PsiTreeUtil.getParentOfType(element, OdinExpressionStatement.class);
        if (odinExpressionStatement != null) {
            OdinVariableInitializationStatement statement = OdinPsiElementFactory
                    .getInstance(project).createVariableInitializationStatement("value", "1");

            statement.getExpressionsList().getExpressionList().getFirst()
                    .replace(Objects.requireNonNull(odinExpressionStatement.getExpression()));
            element.replace(statement);
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, OdinExpressionStatement.class) != null;
    }


    @Override
    public @NotNull @IntentionName String getText() {
        return getFamilyName();
    }
}
