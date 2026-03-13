package com.example.eventplanner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BrowseEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Events> eventList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recycler_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Using a basic adapter - assuming EventAdapter will be created or exists
        adapter = new EventAdapter(eventList);
        recyclerView.setAdapter(adapter);

        Button btnFilter = findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> showFilterDialog());

        // Initialize dummy events if needed (e.g., first run)
        // For the sake of the task, we can call it here or keep it available.
        initializeDummyEvents();
        
        loadEvents("All");
    }

    private void showFilterDialog() {
        String[] options = {"All", "Sport", "Music"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Filter Events")
                .setItems(options, (dialog, which) -> {
                    loadEvents(options[which]);
                })
                .show();
    }

    private void loadEvents(String filterTag) {
        if (filterTag.equals("All")) {
            db.collection("events")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        eventList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Events event = document.toObject(Events.class);
                            event.setEventId(document.getId());
                            eventList.add(event);
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("events")
                    .whereEqualTo("category", filterTag)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        eventList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Events event = document.toObject(Events.class);
                            event.setEventId(document.getId());
                            eventList.add(event);
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error filtering events", Toast.LENGTH_SHORT).show());
        }
    }

    public void initializeDummyEvents() {
        String[] sports = {"Soccer Match", "Basketball Game", "Tennis Tournament"};
        String[] music = {"Rock Concert", "Jazz Night", "Pop Festival"};
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            Events event = new Events(sports[i], "2024-12-01", "Exciting sport event", "Stadium");
            event.setCategory("Sport");
            event.setStatus("Open");
            event.setEventTimestamp(System.currentTimeMillis() + random.nextInt(100000000));
            db.collection("events").add(event);
        }

        for (int i = 0; i < 3; i++) {
            Events event = new Events(music[i], "2024-12-05", "Great music event", "Concert Hall");
            event.setCategory("Music");
            event.setStatus("Open");
            event.setEventTimestamp(System.currentTimeMillis() + random.nextInt(100000000));
            db.collection("events").add(event);
        }
        
        Toast.makeText(this, "6 Dummy events uploaded", Toast.LENGTH_SHORT).show();
    }
}