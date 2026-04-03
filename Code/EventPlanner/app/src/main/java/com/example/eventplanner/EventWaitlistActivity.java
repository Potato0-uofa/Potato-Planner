package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class EventWaitlistActivity extends AppCompatActivity {

    private EventRepository eventRepository;
    private WaitingList waitingList;
    private ListenerRegistration waitlistListener;
    private TextView tvWaitlistCount;
    private RecyclerView recyclerView;
    private WaitlistAdapter adapter;
    private List<Entrant> entrantList = new ArrayList<>();
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_waitlist);

        eventRepository = new EventRepository();
        waitingList = new WaitingList();

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            eventId = "test_event_1";
        }

        tvWaitlistCount = findViewById(R.id.tv_waitlist_count);
        recyclerView = findViewById(R.id.recycler_waitlist);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaitlistAdapter(entrantList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_join_waitlist).setEnabled(false);
        findViewById(R.id.exit_button_waitlist).setOnClickListener(v -> finish());

        findViewById(R.id.btn_view_chosen).setOnClickListener(v -> {
            FragmentFinalEntrantList fragment = FragmentFinalEntrantList.newInstance(eventId);
            fragment.show(getSupportFragmentManager(), "FinalEntrantList");
        });

        findViewById(R.id.btn_view_cancelled).setOnClickListener(v -> {
            FragmentPostDrawWaitlist fragment = FragmentPostDrawWaitlist.newInstance(eventId);
            fragment.show(getSupportFragmentManager(), "PostDrawWaitlist");
        });

        startListening();
        loadEntrants();
    }

    private void startListening() {
        waitlistListener = eventRepository.listenToWaitlistCount(eventId, new EventRepository.CountCallback() {
            @Override
            public void onSuccess(int count) {
                waitingList.setCloudCount(count);
                tvWaitlistCount.setText("Total Entrants: " + waitingList.getCount());
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventWaitlistActivity.this, "Error listening to count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEntrants() {
        eventRepository.fetchWaitlistEntrants(eventId, new EventRepository.EntrantsCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                entrantList.clear();
                entrantList.addAll(entrants);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventWaitlistActivity.this, "Error loading entrants: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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