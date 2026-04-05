package com.example.eventplanner;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Notification screen controller for entrant invitation actions.
 * Handles accept and decline flows with proper Firestore array updates.
 *
 * User stories covered:
 * - US 01.05.02: Entrant accepts invitation
 * - US 01.05.01 / US 01.05.03: Entrant declines invitation, replacement drawn
 */
public class NotificationLogs extends AppCompatActivity {

    private RegistrationRepository registrationRepository;
    private EventRepository eventRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_notifications_content);

        registrationRepository = new RegistrationRepository();
        eventRepository = new EventRepository();

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

        // Load event name and description into the UI
        eventRepository.fetchEventById(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Events event) {
                TextView nameView = findViewById(R.id.event_name);
                TextView descView = findViewById(R.id.event_description);
                if (nameView != null && event.getName() != null) {
                    nameView.setText(event.getName());
                }
                if (descView != null && event.getDescription() != null) {
                    descView.setText(event.getDescription());
                }
            }

            @Override
            public void onFailure(Exception e) { }
        });

        // US 01.05.02 - Accept invitation: update registration + move to chosenEntrants
        acceptBtn.setOnClickListener(v -> {
            registrationRepository.acceptInvitation(eventId, userId,
                    new RegistrationRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            // Move from pendingEntrants to chosenEntrants on the event doc
                            eventRepository.acceptEntrant(eventId, userId,
                                    new EventRepository.SimpleCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(NotificationLogs.this,
                                                    "Invitation accepted! You are registered.",
                                                    Toast.LENGTH_SHORT).show();
                                            acceptBtn.setEnabled(false);
                                            declineBtn.setEnabled(false);
                                            finish();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(NotificationLogs.this,
                                                    "Accepted but failed to update event: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(NotificationLogs.this,
                                    "Accept failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // US 01.05.01 / US 01.05.03 - Decline invitation:
        // update registration + move to cancelledEntrants + auto-draw replacement
        declineBtn.setOnClickListener(v -> {
            registrationRepository.declineInvitation(eventId, userId,
                    new RegistrationRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            // Move from pendingEntrants to cancelledEntrants
                            eventRepository.cancelEntrant(eventId, userId,
                                    new EventRepository.SimpleCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(NotificationLogs.this,
                                                    "Invitation declined",
                                                    Toast.LENGTH_SHORT).show();
                                            acceptBtn.setEnabled(false);
                                            declineBtn.setEnabled(false);

                                            // Auto-draw a replacement from the waitlist
                                            eventRepository.drawFromWaitlist(eventId, 1,
                                                    new EventRepository.SimpleCallback() {
                                                        @Override
                                                        public void onSuccess() {
                                                            // Replacement drawn silently
                                                        }

                                                        @Override
                                                        public void onFailure(Exception e) {
                                                            // No one left on waitlist, that's OK
                                                        }
                                                    });
                                            finish();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(NotificationLogs.this,
                                                    "Declined but failed to update event: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(NotificationLogs.this,
                                    "Decline failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
