package com.lasagnerd.odin.annotater;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;

public class Notifier {

    public static void notify(String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Odin Notifications")
                .createNotification(message, NotificationType.INFORMATION)
                .notify(null);
    }

}
