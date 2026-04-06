package com.example.eventplanner;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/** Utility for showing heads-up system notifications for invitation events. */
public final class NotificationHelper {

    /** Channel ID for invitation heads-up notifications. */
    public static final String CHANNEL_ID_INVITATIONS = "invitation_heads_up";

    /** Display name for the invitation notification channel. */
    private static final String CHANNEL_NAME_INVITATIONS = "Invitation Alerts";

    /** Private constructor to prevent instantiation of this utility class. */
    private NotificationHelper() {
        // Utility class
    }

    /**
     * Creates the invitation notification channel if it does not already exist.
     * No-op on API levels below Oreo.
     *
     * @param context the application context
     */
    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        NotificationChannel existing = manager.getNotificationChannel(CHANNEL_ID_INVITATIONS);
        if (existing != null) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID_INVITATIONS,
                CHANNEL_NAME_INVITATIONS,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Heads-up notifications for event invitations and notices");
        manager.createNotificationChannel(channel);
    }

    /**
     * Displays a heads-up system notification with the given title and message.
     * Requires POST_NOTIFICATIONS permission on Android 13+.
     *
     * @param context        the application context
     * @param notificationId unique ID for this notification
     * @param title          the notification title
     * @param message        the notification body text
     * @param tapIntent      the Intent to launch when the notification is tapped
     */
    public static void showHeadsUp(Context context,
                                   int notificationId,
                                   String title,
                                   String message,
                                   Intent tapIntent) {
        ensureChannel(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_INVITATIONS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setTimeoutAfter(5000L);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}


