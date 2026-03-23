package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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
    private final List<Events> eventList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recycler_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new EventAdapter(eventList);
        recyclerView.setAdapter(adapter);

        Button btnFilter = findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> showFilterDialog());

        // Setup bottom navigation bar
        findViewById(R.id.new_event_button_browse).setOnClickListener(v -> {
            startActivity(new Intent(BrowseEventsActivity.this, CreateEventActivity.class));
        });

        findViewById(R.id.search_button_browse).setOnClickListener(v -> {
            startActivity(new Intent(BrowseEventsActivity.this, SearchScreen.class));
        });

        findViewById(R.id.home_button_browse).setOnClickListener(v -> {
            startActivity(new Intent(BrowseEventsActivity.this, HomePage.class));
        });

        findViewById(R.id.browse_button_browse).setOnClickListener(v -> {
            // Already on browse
        });

        findViewById(R.id.profile_button_browse).setOnClickListener(v -> {
            startActivity(new Intent(BrowseEventsActivity.this, Profile.class));
        });

        loadEvents("All");
    }

    private void showFilterDialog() {
        String[] options = {"All", "Sport", "Music"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Filter Events")
                .setItems(options, (dialog, index) -> {
                    loadEvents(options[index]);
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
}