package com.example.eventplanner;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class WaitlistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WaitlistAdapter adapter;
    private List<Entrant> entrantList = new ArrayList<>();
    private EventRepository eventRepository;
    private String eventId;
    private TextView tvWaitlistCount;
    private ListenerRegistration waitlistListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_waitlist);

        eventRepository = new EventRepository();
        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null) {
            Toast.makeText(this, "Event ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvWaitlistCount = findViewById(R.id.tv_waitlist_count);
        recyclerView = findViewById(R.id.recycler_waitlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaitlistAdapter(entrantList);
        recyclerView.setAdapter(adapter);

        // Disable join button in this view as it's for organizers
        findViewById(R.id.btn_join_waitlist).setVisibility(android.view.View.GONE);

        loadWaitlist();
        startListeningToCount();
    }

    private void loadWaitlist() {
        eventRepository.fetchWaitlistEntrants(eventId, new EventRepository.EntrantsCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                entrantList.clear();
                entrantList.addAll(entrants);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(WaitlistActivity.this, "Error loading entrants: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startListeningToCount() {
        waitlistListener = eventRepository.listenToWaitlistCount(eventId, new EventRepository.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (tvWaitlistCount != null) {
                    tvWaitlistCount.setText("Total Entrants: " + count);
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Silently fail or log
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