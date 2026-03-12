package com.example.eventplanner;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.ListenerRegistration;

public class EventWaitlistActivity extends AppCompatActivity {

    private EventRepository eventRepository;
    private WaitingList waitingList;
    private ListenerRegistration waitlistListener;
    private TextView tvWaitlistCount;
    private final String testEventId = "test_event_1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_waitlist);

        eventRepository = new EventRepository();
        waitingList = new WaitingList();
        tvWaitlistCount = findViewById(R.id.tv_waitlist_count);

        // Hide the join button as it's not part of US 01.05.04 implementation task
        findViewById(R.id.btn_join_waitlist).setEnabled(false);

        startListening();
    }

    private void startListening() {
        waitlistListener = eventRepository.listenToWaitlistCount(testEventId, new EventRepository.CountCallback() {
            @Override
            public void onSuccess(int count) {
                waitingList.setCloudCount(count);
                tvWaitlistCount.setText("Total Entrants: " + waitingList.getCount());
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventWaitlistActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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