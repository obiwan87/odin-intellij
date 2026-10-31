package com.lasagnerd.odin.ols;

import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.content.ContentFactory;
import com.lasagnerd.odin.settings.projectSettings.OdinDebuggersConfigurable;
import com.lasagnerd.odin.settings.projectSettings.OdinSdksConfigurable;
import org.jetbrains.annotations.NotNull;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class OdinToolWindowFactory implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JBLabel olsLabel = new JBLabel(olsText());
        panel.add(olsLabel);
        panel.add(Box.createVerticalStrut(4));
        String odinRoot = System.getenv("ODIN_ROOT");
        panel.add(new JBLabel("ODIN_ROOT: " + (odinRoot == null ? "not set" : odinRoot)));
        panel.add(Box.createVerticalStrut(8));
        panel.add(button("Install OLS", AllIcons.Actions.Download, () -> installOls(project, olsLabel)));
        panel.add(button("Odin SDKs", AllIcons.General.Settings, () ->
                ShowSettingsUtil.getInstance().showSettingsDialog(project, OdinSdksConfigurable.class, null)));
        panel.add(button("Debuggers", AllIcons.Actions.StartDebugger, () ->
                ShowSettingsUtil.getInstance().showSettingsDialog(project, OdinDebuggersConfigurable.class, null)));
        toolWindow.getContentManager().addContent(
                ContentFactory.getInstance().createContent(panel, "Environment", false));
    }

    private static JButton button(String text, Icon icon, Runnable action) {
        JButton button = new JButton(text, icon);
        button.addActionListener(e -> action.run());
        return button;
    }

    private static String olsText() {
        return "OLS: " + OlsLocator.find().map(Object::toString).orElse("not found");
    }

    private static void installOls(Project project, JBLabel olsLabel) {
        new Task.Backgroundable(project, "Installing OLS", true) {
            private Exception failure;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    OlsInstaller.install();
                } catch (Exception e) {
                    failure = e;
                }
            }

            @Override
            public void onFinished() {
                olsLabel.setText(olsText());
                NotificationGroupManager.getInstance().getNotificationGroup("Odin Notifications")
                        .createNotification(failure == null
                                        ? "OLS installed. Reopen the project to start it."
                                        : "Unable to install OLS: " + failure.getMessage(),
                                failure == null ? NotificationType.INFORMATION : NotificationType.ERROR)
                        .notify(project);
            }
        }.queue();
    }
}
