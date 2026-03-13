package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Activity that displays the details of a specified event and allows the user to join the waitlist.
 */
public class EventDescriptionView extends AppCompatActivity {

    private EventRepository eventRepository;
    private WaitingList waitingList;
    private ListenerRegistration waitlistListener;
    private TextView tvWaitlistCount;
    private String eventId;
    private String deviceId;
    private Button btnViewWaitlist;

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

        tvWaitlistCount = findViewById(R.id.Waitlist_Count);
        ((TextView) findViewById(R.id.event_name)).setText(eventName != null ? eventName : "Demo Event");
        ((TextView) findViewById(R.id.event_details)).setText(eventDescription != null ? eventDescription : "This is a demo event description.");

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

        btnViewWaitlist = findViewById(R.id.view_waitlist_button);
        // Force visible so you can see the button immediately when running the app
        btnViewWaitlist.setVisibility(View.VISIBLE); 
        btnViewWaitlist.setOnClickListener(v -> {
            Intent intent = new Intent(EventDescriptionView.this, EventWaitlistActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        // US 01.05.05 - Info box click listener for detailed lottery guidelines
        findViewById(R.id.lottery_info_box).setOnClickListener(v -> showLotteryGuidelines());

        View exitBtn = findViewById(R.id.exit_button_event_page);
        if (exitBtn != null) {
            exitBtn.setOnClickListener(v -> finish());
        }

        startListeningToWaitlist();
    }

    private void showLotteryGuidelines() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Lottery Guidelines")
                .setMessage("1. Joining the waitlist does not guarantee a spot.\n" +
                        "2. Once the organizer initiates the draw, winners are selected randomly.\n" +
                        "3. Winners will receive a notification to accept or decline their invitation.\n" +
                        "4. If an invitation is declined, a new winner will be drawn.")
                .setPositiveButton("Got it", null)
                .show();
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
                // Ignore error for demo
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitlistListener != null) {
            waitlistListener.remove();
        }
    }
}