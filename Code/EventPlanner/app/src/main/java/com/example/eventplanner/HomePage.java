package com.example.eventplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Main home screen with navigation bar and upcoming event notifications. */
public class HomePage extends AppCompatActivity {

    private final UserRepository userRepository = new UserRepository();
    private ImageButton notificationButton;
    private ListenerRegistration registrationListener;
    private String deviceId;
    private boolean userNotificationsEnabled = true;

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

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this,
                            "System notifications are disabled. You can enable them in settings.",
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        notificationButton = findViewById(R.id.notification_button_home);
        notificationButton.setVisibility(View.VISIBLE);
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        NotificationHelper.ensureChannel(this);
        ensureNotificationPermission();

        findViewById(R.id.new_event_button_home).setOnClickListener(v -> {
            EventTypeFragment fragment = new EventTypeFragment();
            fragment.show(getSupportFragmentManager(), "NewEventFragment");
        });

        findViewById(R.id.qr_button_home).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, SearchScreen.class));
        });

        findViewById(R.id.home_button_home).setOnClickListener(v -> {
            // Do nothing since already on home page
        });


        findViewById(R.id.browse_button_home).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, BrowseEventsActivity.class));
        });

        findViewById(R.id.profile_button_home).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, Profile.class));
        });

        notificationButton.setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, InvitationsActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset state when coming back to the page
        notificationButton.setVisibility(View.VISIBLE);
        checkAndScheduleNotification();
    }

    private void checkAndScheduleNotification() {
        userRepository.getUserByDeviceId(deviceId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    userNotificationsEnabled = user.isNotificationsEnabled();
                } else {
                    // Default to enabled when user profile is not initialized.
                    userNotificationsEnabled = true;
                }

                if (userNotificationsEnabled) {
                    NotificationHelper.ensureChannel(HomePage.this);
                    startSystemNotificationListener();
                } else {
                    stopSystemNotificationListener();
                    NotificationManagerCompat.from(HomePage.this).cancelAll();
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Keep default behavior on transient profile lookup failures.
                userNotificationsEnabled = true;
                startSystemNotificationListener();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSystemNotificationListener();
    }

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void startSystemNotificationListener() {
        if (!userNotificationsEnabled) {
            stopSystemNotificationListener();
            return;
        }

        if (registrationListener != null) {
            registrationListener.remove();
            registrationListener = null;
        }

        registrationListener = FirebaseFirestore.getInstance()
                .collection("registrations")
                .whereEqualTo("userId", deviceId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        return;
                    }

                    for (DocumentChange change : snapshot.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED
                                || change.getType() == DocumentChange.Type.MODIFIED) {
                            maybeShowHeadsUp(change.getDocument());
                        }
                    }
                });
    }

    private void stopSystemNotificationListener() {
        if (registrationListener != null) {
            registrationListener.remove();
            registrationListener = null;
        }
    }

    private void maybeShowHeadsUp(DocumentSnapshot doc) {
        if (!userNotificationsEnabled) {
            return;
        }

        String status = doc.getString("status");
        if (status == null || !SYSTEM_NOTIFY_STATUSES.contains(status)) {
            return;
        }

        Timestamp updatedAt = doc.getTimestamp("updatedAt");
        long nowMark = updatedAt != null ? updatedAt.toDate().getTime() : System.currentTimeMillis();

        SharedPreferences prefs = getSharedPreferences(PREFS_SYSTEM_NOTIFICATIONS, MODE_PRIVATE);
        String key = "doc_" + doc.getId();
        long seenMark = prefs.getLong(key, 0L);
        if (nowMark <= seenMark) {
            return;
        }

        String noticeMessage = doc.getString("lastNoticeMessage");
        String eventId = doc.getString("eventId");
        String title = buildTitleForStatus(status);
        String message = (noticeMessage != null && !noticeMessage.trim().isEmpty())
                ? noticeMessage
                : "You have a new update for your event invitation.";

        Intent tapIntent;
        if ("invited".equals(status) || "private_waitlist_invited".equals(status) || "coorganizer_invited".equals(status)) {
            tapIntent = new Intent(this, NotificationLogs.class);
            tapIntent.putExtra("eventId", eventId);
            tapIntent.putExtra("status", status);
            tapIntent.putExtra("noticeMessage", noticeMessage);
            tapIntent.putExtra("actionable", true);
        } else {
            tapIntent = new Intent(this, InvitationsActivity.class);
        }
        tapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationHelper.showHeadsUp(
                this,
                Math.abs(doc.getId().hashCode()),
                title,
                message,
                tapIntent
        );

        prefs.edit().putLong(key, nowMark).apply();
    }

    private String buildTitleForStatus(String status) {
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
}
