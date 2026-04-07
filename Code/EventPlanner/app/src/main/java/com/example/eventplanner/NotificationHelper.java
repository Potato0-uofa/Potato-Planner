package com.example.eventplanner;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Utility for showing heads-up system notifications for invitation events. */
public final class NotificationHelper {

    /** Channel ID for invitation heads-up notifications. */
    public static final String CHANNEL_ID_INVITATIONS = "invitation_heads_up";

    /** Display name for the invitation notification channel. */
    private static final String CHANNEL_NAME_INVITATIONS = "Invitation Alerts";
    private static final String PREFS_SYSTEM_NOTIFICATIONS = "system_notification_seen";

    private static final Set<String> SYSTEM_NOTIFY_STATUSES = new HashSet<>(Arrays.asList(
            "invited",
            "private_waitlist_invited",
            "coorganizer_invited",
            "accepted",
            "declined",
            "cancelled",
            "waitlisted"
    ));

    private static ListenerRegistration userListener;
    private static ListenerRegistration registrationListener;
    private static boolean listenerInitialized = false;
    private static String cachedDeviceId;

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
     * Starts a single app-wide Firestore listener so system heads-up notifications
     * can appear no matter which activity page is currently shown.
     */
    public static synchronized void startGlobalListener(Context context) {
        Context appContext = context.getApplicationContext();
        ensureChannel(appContext);

        if (listenerInitialized) {
            return;
        }
        listenerInitialized = true;

        cachedDeviceId = Settings.Secure.getString(
                appContext.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        if (cachedDeviceId == null || cachedDeviceId.trim().isEmpty()) {
            return;
        }

        userListener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(cachedDeviceId)
                .addSnapshotListener((userSnapshot, userError) -> {
                    if (userError != null) return;

                    boolean enabled = true;
                    if (userSnapshot != null && userSnapshot.exists()) {
                        User user = userSnapshot.toObject(User.class);
                        if (user != null) {
                            enabled = user.isNotificationsEnabled();
                        }
                    }

                    if (!enabled) {
                        if (registrationListener != null) {
                            registrationListener.remove();
                            registrationListener = null;
                        }
                        NotificationManagerCompat.from(appContext).cancelAll();
                        return;
                    }

                    if (registrationListener != null) {
                        return;
                    }

                    registrationListener = FirebaseFirestore.getInstance()
                            .collection("registrations")
                            .whereEqualTo("userId", cachedDeviceId)
                            .addSnapshotListener((snapshot, error) -> {
                                if (error != null || snapshot == null) return;

                                for (DocumentChange change : snapshot.getDocumentChanges()) {
                                    if (change.getType() == DocumentChange.Type.ADDED
                                            || change.getType() == DocumentChange.Type.MODIFIED) {
                                        maybeShowFromRegistration(appContext, change.getDocument());
                                    }
                                }
                            });
                });
    }

    private static void maybeShowFromRegistration(Context appContext, DocumentSnapshot doc) {
        String status = doc.getString("status");
        if (status == null || !SYSTEM_NOTIFY_STATUSES.contains(status)) {
            return;
        }

        Timestamp updatedAt = doc.getTimestamp("updatedAt");
        long nowMark = updatedAt != null ? updatedAt.toDate().getTime() : System.currentTimeMillis();

        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_SYSTEM_NOTIFICATIONS, Context.MODE_PRIVATE);
        String key = "doc_" + doc.getId();
        long seenMark = prefs.getLong(key, 0L);
        if (nowMark <= seenMark) {
            return;
        }

        String eventId = doc.getString("eventId");
        String noticeMessage = doc.getString("lastNoticeMessage");
        String title = buildTitle(status, doc.getString("lastNoticeType"));
        String message = (noticeMessage != null && !noticeMessage.trim().isEmpty())
                ? noticeMessage
                : "You have a new event notification.";

        Intent tapIntent;
        if ("invited".equals(status)
                || "private_waitlist_invited".equals(status)
                || "coorganizer_invited".equals(status)) {
            tapIntent = new Intent(appContext, NotificationLogs.class);
            tapIntent.putExtra("eventId", eventId);
            tapIntent.putExtra("status", status);
            tapIntent.putExtra("noticeMessage", noticeMessage);
            tapIntent.putExtra("actionable", true);
        } else {
            tapIntent = new Intent(appContext, InvitationsActivity.class);
        }
        tapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showHeadsUp(appContext, Math.abs(doc.getId().hashCode()), title, message, tapIntent);
        prefs.edit().putLong(key, nowMark).apply();
    }

    private static String buildTitle(String status, String noticeType) {
        if ("lottery_lose".equals(noticeType)) return "Lottery result";
        if ("selected_notice".equals(noticeType)) return "Organizer message";
        if ("cancelled_notice".equals(noticeType)) return "Cancelled entrants notice";
        if ("waitlist_notice".equals(noticeType)) return "Waitlist notice";

        switch (status) {
            case "invited":
                return "You won the lottery";
            case "private_waitlist_invited":
                return "Private event invite";
            case "coorganizer_invited":
                return "Co-organizer invite";
            case "accepted":
                return "Invitation accepted";
            case "declined":
                return "Invitation declined";
            case "cancelled":
                return "Event registration update";
            case "waitlisted":
                return "Waitlist update";
            default:
                return "Event notification";
        }
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


