package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public final class OdinReleaseCheckActivity implements StartupActivity.DumbAware {
    private static final long DAY=24L*60*60*1000;
    @Override public void runActivity(@NotNull Project project){OdinSdkRegistryState state=OdinSdkRegistryService.getInstance().getState();
        if(!state.automaticallyCheckForReleases||System.currentTimeMillis()-state.lastReleaseCheck<DAY)return;
        ApplicationManager.getApplication().executeOnPooledThread(()->{try{var releases=OdinReleaseService.fetchReleases(false);if(releases.isEmpty())return;OdinRelease latest=releases.get(0);
            boolean installed=OdinSdkRegistryService.getInstance().getSdks().stream().anyMatch(s->latest.tagName().equals(s.releaseTag));if(installed||latest.tagName().equals(state.lastNotifiedRelease))return;
            state.lastNotifiedRelease=latest.tagName();Notification notification=NotificationGroupManager.getInstance().getNotificationGroup("Odin Notifications")
                    .createNotification("Odin SDK update available","Odin SDK version "+latest.tagName()+" is now available.",NotificationType.INFORMATION);
            notification.addAction(NotificationAction.createSimple("Download",()->ShowSettingsUtil.getInstance().showSettingsDialog(project,OdinSdksConfigurable.class)));
            notification.addAction(NotificationAction.createSimple("View release notes",()->BrowserUtil.browse(latest.htmlUrl())));notification.notify(project);
        }catch(Exception e){OdinReleaseService.logCheckFailure(e);}});}
}
