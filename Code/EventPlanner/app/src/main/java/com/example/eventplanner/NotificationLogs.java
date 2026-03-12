package com.example.eventplanner;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationLogs extends AppCompatActivity {

    private RegistrationRepository registrationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_notifications_content);

        registrationRepository = new RegistrationRepository();

        Button declineBtn = findViewById(R.id.decline_notification_button);

        String eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.trim().isEmpty()) {
            eventId = "TEST_EVENT_ID"; // replace with real id in test
        }

        String userId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        String finalEventId = eventId;
        declineBtn.setOnClickListener(v -> {
            registrationRepository.declineInvitation(
                    finalEventId,
                    userId,
                    new RegistrationRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(
                                    NotificationLogs.this,
                                    "Invitation declined",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

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



