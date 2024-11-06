package com.lasagnerd.odin.rider;

import com.intellij.ide.projectView.actions.UnmarkRootAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class OdinRiderUnmarkRootAction extends UnmarkRootAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        super.actionPerformed(e);
        OdinRiderMarkRootAction.refresh();
    }
}
