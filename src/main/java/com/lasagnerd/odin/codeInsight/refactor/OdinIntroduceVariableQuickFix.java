package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.lang.psi.OdinEos;
import com.lasagnerd.odin.lang.psi.OdinExpressionStatement;
import org.jetbrains.annotations.NotNull;

public class OdinIntroduceVariableQuickFix extends PsiElementBaseIntentionAction {

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Introduce variable";
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        OdinIntroduceVariableHandler introduceVariableHandler = new OdinIntroduceVariableHandler(element);
        introduceVariableHandler.invoke(project, editor, element.getContainingFile(), null);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        OdinExpressionStatement parent = PsiTreeUtil.getParentOfType(element, OdinExpressionStatement.class);
        if(parent == null) {
            OdinEos eos = PsiTreeUtil.getParentOfType(element, false, OdinEos.class);
            if(eos != null) {
                PsiElement elementAt = element.getContainingFile().findElementAt(eos.getTextOffset() - 1);
                if(elementAt != null) {
                    return isAvailable(project, editor, elementAt);
                }
            }
        }
        return parent != null;
    }


    @Override
    public @NotNull @IntentionName String getText() {
        return getFamilyName();
    }
}
