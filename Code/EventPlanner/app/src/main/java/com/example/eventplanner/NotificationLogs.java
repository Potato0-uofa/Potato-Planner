package com.example.eventplanner;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


/**
 * Notification screen controller for entrant invitation actions.
 * Current implementation wires decline action to Firestore registration status updates.
 *
 * User story covered:
 * - US 01.05.03: Entrant declines invitation
 */
public class NotificationLogs extends AppCompatActivity {

    /** Repository used to update registration records for invitation actions. */
    private RegistrationRepository registrationRepository;

    /**
     * Initializes the notification action UI and binds the decline button handler.
     * <p>
     * This screen expects an {@code eventId} extra in the launching intent. If no event ID
     * is provided, a fallback test value is used by the current implementation.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_notifications_content);

        registrationRepository = new RegistrationRepository();

        Button declineBtn = findViewById(R.id.decline_notification_button);
        Button acceptBtn = findViewById(R.id.accept_notification_button);

        String eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Missing event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        String finalEventId = eventId;

        acceptBtn.setOnClickListener(v -> {
            registrationRepository.acceptInvitation(
                    finalEventId,
                    userId,
                    new RegistrationRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(
                                    NotificationLogs.this,
                                    "Invitation accepted",
                                    Toast.LENGTH_SHORT
                            ).show();
                            acceptBtn.setEnabled(false);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(
                                    NotificationLogs.this,
                                    "Accept failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        });



        declineBtn.setOnClickListener(v -> {

            registrationRepository.declineInvitation(
                    finalEventId,
                    userId,
                    new RegistrationRepository.SimpleCallback() {
                        /**
                         * Displays a confirmation message after the invitation is declined.
                         */
                        @Override
                        public void onSuccess() {
                            Toast.makeText(
                                    NotificationLogs.this,
                                    "Invitation declined",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        /**
                         * Displays an error message if declining the invitation fails.
                         *
                         * @param e exception describing the failure
                         */
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(
                                    NotificationLogs.this,
                                    "Decline failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        });
    }
}