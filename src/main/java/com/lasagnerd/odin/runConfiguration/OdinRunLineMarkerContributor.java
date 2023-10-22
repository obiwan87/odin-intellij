package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.ExpUiIcons;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.lang.psi.OdinProcedureDeclarationStatement;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinRunLineMarkerContributor extends RunLineMarkerContributor {
    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        boolean injectedFragment = element.getProject().getService(InjectedLanguageManager.class)
                .isInjectedFragment(element.getContainingFile());

        if (injectedFragment) {
            return null;
        }

        if (element.getNode().getElementType() != OdinTypes.IDENTIFIER) {
            return null;
        }

        if (!(element.getParent() instanceof OdinProcedureDeclarationStatement)) {
            return null;
        }

        if (element.getText().equals("main")) {
            AnAction[] actions = ExecutorAction.getActions(1);
            return new Info(
                    ExpUiIcons.Gutter.Run,
                    (ignored) -> "Run " + element.getContainingFile().getName(),
                    actions[0], actions[actions.length - 1]
            );

        }
        return null;
    }
}
