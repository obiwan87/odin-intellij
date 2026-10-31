package com.lasagnerd.odin.runConfiguration;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.DumbAwareToggleAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public class OdinBuildProfileAction extends ComboBoxAction {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        boolean odinProject = project != null
                && com.lasagnerd.odin.settings.projectSettings.OdinSdkUtils.getLibraryPath(project).isPresent();
        e.getPresentation().setEnabledAndVisible(odinProject);
        if (odinProject) {
            e.getPresentation().setText("Odin: " + OdinBuildProfile.current(project).label());
        }
    }

    @Override
    protected @NotNull DefaultActionGroup createPopupActionGroup(@NotNull JComponent button, @NotNull DataContext context) {
        DefaultActionGroup group = new DefaultActionGroup();
        for (OdinBuildProfile profile : OdinBuildProfile.values()) {
            group.add(new DumbAwareAction(profile.label()) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    if (e.getProject() != null) {
                        profile.select(e.getProject());
                    }
                }
            });
        }
        group.addSeparator();
        group.add(new DumbAwareToggleAction("Address Sanitizer") {
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return e.getProject() != null && OdinBuildProfile.sanitizeAddress(e.getProject());
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                if (e.getProject() != null) {
                    OdinBuildProfile.setSanitizeAddress(e.getProject(), state);
                }
            }
        });
        return group;
    }
}
