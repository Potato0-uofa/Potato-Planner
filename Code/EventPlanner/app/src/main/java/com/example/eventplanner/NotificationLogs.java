package com.example.eventplanner;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
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

    private static final String STATUS_INVITED = "invited";
    private static final String STATUS_PRIVATE_WAITLIST_INVITED = "private_waitlist_invited";
    private static final String STATUS_COORGANIZER_INVITED = "coorganizer_invited";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_notifications_content);

        registrationRepository = new RegistrationRepository();
        eventRepository = new EventRepository();

        Button declineBtn = findViewById(R.id.decline_notification_button);
        Button acceptBtn = findViewById(R.id.accept_notification_button);

        String eventId = getIntent().getStringExtra("eventId");
        String status = getIntent().getStringExtra("status");
        String noticeMessage = getIntent().getStringExtra("noticeMessage");
        boolean actionable = getIntent().getBooleanExtra("actionable", false);

        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Missing event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        TextView invitationMessageView = findViewById(R.id.invitation_message);
        if (invitationMessageView != null && noticeMessage != null && !noticeMessage.trim().isEmpty()) {
            invitationMessageView.setText(noticeMessage);
        }

        if (STATUS_PRIVATE_WAITLIST_INVITED.equals(status)) {
            acceptBtn.setText("Join Waiting List");
            declineBtn.setText("Decline Invitation");
        } else if (STATUS_COORGANIZER_INVITED.equals(status)) {
            acceptBtn.setText("Accept Co-organizer Invite");
            declineBtn.setText("Decline Co-organizer Invite");
            TextView roleInfo = findViewById(R.id.co_organizer_role_info);
            if (roleInfo != null) {
                roleInfo.setVisibility(View.VISIBLE);
            }
        } else {
            acceptBtn.setText("Accept Invitation");
            declineBtn.setText("Decline Invitation");
        }

        if (!actionable && (status == null || !STATUS_INVITED.equals(status))) {
            acceptBtn.setEnabled(false);
            declineBtn.setEnabled(false);
        }

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

        // Accept action routes by invitation type.
        acceptBtn.setOnClickListener(v -> {
            if (!actionable) {
                finish();
                return;
            }

            if (STATUS_PRIVATE_WAITLIST_INVITED.equals(status)) {
                registrationRepository.updateInvitationStatus(
                        eventId,
                        userId,
                        "waitlisted",
                        new RegistrationRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                eventRepository.joinWaitingList(eventId, userId, new EventRepository.SimpleCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(NotificationLogs.this,
                                                "Joined waiting list",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(NotificationLogs.this,
                                                "Accepted but failed to join waitlist",
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
                        }
                );
                return;
            }

            if (STATUS_COORGANIZER_INVITED.equals(status)) {
                eventRepository.addCoOrganizer(eventId, userId, new EventRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        // Remove user from entrant pool if they were on it
                        eventRepository.removeEntrantCompletely(eventId, userId,
                                new EventRepository.SimpleCallback() {
                                    @Override
                                    public void onSuccess() { }

                                    @Override
                                    public void onFailure(Exception e) { }
                                });

                        // Remove the invitation from inbox
                        registrationRepository.leaveEvent(eventId, userId,
                                new RegistrationRepository.SimpleCallback() {
                                    @Override
                                    public void onSuccess() { }

                                    @Override
                                    public void onFailure(Exception e) { }
                                });

                        Toast.makeText(NotificationLogs.this,
                                "You are now a co-organizer!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(NotificationLogs.this,
                                "Accept failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }

            // Default: lottery/public invitation acceptance flow.
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
            if (!actionable) {
                finish();
                return;
            }

            if (STATUS_PRIVATE_WAITLIST_INVITED.equals(status)) {
                registrationRepository.updateInvitationStatus(
                        eventId,
                        userId,
                        "private_waitlist_declined",
                        new RegistrationRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(NotificationLogs.this,
                                        "Invitation declined",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(NotificationLogs.this,
                                        "Decline failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                );
                return;
            }

            if (STATUS_COORGANIZER_INVITED.equals(status)) {
                // Remove the invitation from inbox
                registrationRepository.leaveEvent(eventId, userId,
                        new RegistrationRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(NotificationLogs.this,
                                        "Invitation declined",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(NotificationLogs.this,
                                        "Decline failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                );
                return;
            }

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
