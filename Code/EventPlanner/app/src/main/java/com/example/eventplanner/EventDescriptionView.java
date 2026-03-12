package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.ListenerRegistration;

public class EventDescriptionView extends AppCompatActivity {

    private EventRepository eventRepository;
    private WaitingList waitingList;
    private ListenerRegistration waitlistListener;
    private TextView tvWaitlistCount;
    private String eventId;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        eventRepository = new EventRepository();
        waitingList = new WaitingList();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            eventId = "test_event_1"; // Default for demo
        }

        String eventName = getIntent().getStringExtra("eventName");
        String eventDescription = getIntent().getStringExtra("eventDescription");

        // Set the data to your views
        tvWaitlistCount = findViewById(R.id.Waitlist_Count);
        ((TextView) findViewById(R.id.event_name)).setText(eventName != null ? eventName : "Test Event");
        ((TextView) findViewById(R.id.event_details)).setText(eventDescription != null ? eventDescription : "Test Description");

        Button btnJoinEvent = findViewById(R.id.join_event_button);
        btnJoinEvent.setOnClickListener(v -> {
            eventRepository.joinWaitingList(eventId, deviceId, new EventRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(EventDescriptionView.this, "Joined Waitlist!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(EventDescriptionView.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        setupNavigation();
        startListeningToWaitlist();
    }

    private void startListeningToWaitlist() {
        waitlistListener = eventRepository.listenToWaitlistCount(eventId, new EventRepository.CountCallback() {
            @Override
            public void onSuccess(int count) {
                waitingList.setCloudCount(count);
                tvWaitlistCount.setText("Waitlist: " + waitingList.getCount() + " people");
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDescriptionView.this, "Error loading waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
        findViewById(R.id.home_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, HomePage.class));
        });

        findViewById(R.id.search_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, SearchScreen.class));
        });

        findViewById(R.id.browse_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, NonAdminBrowseEvents.class));
        });

        findViewById(R.id.profile_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, Profile.class));
        });

        findViewById(R.id.exit_button_event_page).setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitlistListener != null) {
            waitlistListener.remove();
        }
    }
}