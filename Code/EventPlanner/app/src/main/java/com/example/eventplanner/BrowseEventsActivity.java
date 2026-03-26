package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BrowseEventsActivity extends AppCompatActivity {

    private final List<Events> allEventsList = new ArrayList<>();
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

        findViewById(R.id.qr_button_browse).setOnClickListener(v -> {
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

        EditText searchBar = findViewById(R.id.et_search_bar);
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
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
                        allEventsList.clear(); // add this line
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Events event = document.toObject(Events.class);
                            event.setEventId(document.getId());
                            eventList.add(event);
                            allEventsList.add(event);
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
                        allEventsList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Events event = document.toObject(Events.class);
                            event.setEventId(document.getId());
                            eventList.add(event);
                            allEventsList.add(event);
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error filtering events", Toast.LENGTH_SHORT).show());
        }
    }

    private void filterBySearch(String query) {
        String lowerQuery = query.toLowerCase().trim();
        eventList.clear();
        for (Events event : allEventsList) {
            if (event.getName() != null && event.getName().toLowerCase().contains(lowerQuery)) {
                eventList.add(event);
            }
        }
        adapter.notifyDataSetChanged();
    }
}