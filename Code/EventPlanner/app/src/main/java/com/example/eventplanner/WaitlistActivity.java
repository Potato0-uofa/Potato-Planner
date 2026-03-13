package com.example.eventplanner;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WaitlistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WaitlistAdapter adapter;
    private List<Entrant> entrantList = new ArrayList<>();
    private EventRepository eventRepository;
    private String eventId;

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

        recyclerView = findViewById(R.id.recycler_waitlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaitlistAdapter(entrantList);
        recyclerView.setAdapter(adapter);

        loadWaitlist();
    }

    private void loadWaitlist() {
        eventRepository.fetchWaitlistEntrants(eventId, new EventRepository.EntrantsCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                entrantList.clear();
                entrantList.addAll(entrants);
                adapter.notifyDataSetChanged();
                
                if (entrants.isEmpty()) {
                    Toast.makeText(WaitlistActivity.this, "No entrants in waitlist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(WaitlistActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}