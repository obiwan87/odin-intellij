package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinRunLineMarkerContributor extends RunLineMarkerContributor {
    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if(element.getNode().getElementType() != OdinTypes.IDENTIFIER_TOKEN) {
            return null;
        }

        if (!(element.getParent() instanceof OdinDeclaredIdentifier)) {
            return null;
        }

        if (!OdinInsightUtils.isProcedureDeclaration(element.getParent())) {
            return null;
        }

        if (element.getText().equals("main")) {
            AnAction[] actions = ExecutorAction.getActions(0);
            return new Info(
                    AllIcons.RunConfigurations.TestState.Run,
                    new AnAction[]{ actions[0], actions[1], actions[actions.length - 1] },
                    (ignored) -> "Run " + element.getContainingFile().getName()
            );

        }
        return null;
    }
}
