package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NonAdminBrowseEvents extends AppCompatActivity {

    private final EventRepository eventRepository = new EventRepository();
    private final List<Events> eventList = new ArrayList<>();
    private ArrayAdapter<Events> adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events_view);

        db = FirebaseFirestore.getInstance();

        // Home bar navigation
        findViewById(R.id.new_event_button_browse).setOnClickListener(v -> {
            startActivity(new Intent(NonAdminBrowseEvents.this, CreateEventActivity.class));
        });

        findViewById(R.id.search_button_browse).setOnClickListener(v -> {
            startActivity(new Intent(NonAdminBrowseEvents.this, SearchScreen.class));
        });

        findViewById(R.id.home_button_browse).setOnClickListener(v -> {
            startActivity(new Intent(NonAdminBrowseEvents.this, HomePage.class));
        });

        findViewById(R.id.browse_button_browse).setOnClickListener(v -> {
            //Already on browse, do nothing
        });

        findViewById(R.id.profile_button_browse).setOnClickListener(v -> {
            startActivity(new Intent(NonAdminBrowseEvents.this, Profile.class));
        });

        Button btnFilter = findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> showFilterDialog());

        ListView listView = findViewById(R.id.browse_event_list);
        adapter = new ArrayAdapter<Events>(
                NonAdminBrowseEvents.this,
                R.layout.item_event_browse,
                eventList
        ) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event_browse, parent, false);
                }

                Events event = getItem(position);
                TextView tvName = convertView.findViewById(R.id.tv_event_name);
                TextView tvCategory = convertView.findViewById(R.id.tv_event_category);
                TextView tvStatus = convertView.findViewById(R.id.tv_event_status);

                if (event != null) {
                    tvName.setText(event.getName() != null ? event.getName() : "Unnamed Event");
                    tvCategory.setText(event.getCategory() != null ? event.getCategory() : "No Category");
                    tvStatus.setText(event.getStatus() != null ? event.getStatus() : "Unknown");
                }

                return convertView;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Events selectedEvent = eventList.get(position);
            Intent intent = new Intent(NonAdminBrowseEvents.this, EventDescriptionView.class);
            intent.putExtra("eventId", selectedEvent.getEventId());
            intent.putExtra("eventName", selectedEvent.getName());
            intent.putExtra("eventDescription", selectedEvent.getDescription());
            intent.putExtra("eventDate", selectedEvent.getDate());
            startActivity(intent);
        });

        // Initialize dummy events
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
                            if (event.getEventId() == null) event.setEventId(document.getId());
                            eventList.add(event);
                        }
                        adapter.notifyDataSetChanged();
                        if (eventList.isEmpty()) {
                            Toast.makeText(NonAdminBrowseEvents.this, "No events found", Toast.LENGTH_SHORT).show();
                        }
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
                            if (event.getEventId() == null) event.setEventId(document.getId());
                            eventList.add(event);
                        }
                        adapter.notifyDataSetChanged();
                        if (eventList.isEmpty()) {
                            Toast.makeText(NonAdminBrowseEvents.this, "No " + filterTag + " events found", Toast.LENGTH_SHORT).show();
                        }
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