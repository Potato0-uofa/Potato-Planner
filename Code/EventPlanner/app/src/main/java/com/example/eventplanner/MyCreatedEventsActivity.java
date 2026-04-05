package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyCreatedEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView listView;
    private List<Events> createdEvents = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private List<String> eventNames = new ArrayList<>();
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_created_events);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        listView = findViewById(R.id.list_created_events);

        adapter = new ArrayAdapter<>(
                this,
                R.layout.item_created_event,
                eventNames
        );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Events selectedEvent = createdEvents.get(position);
            Intent intent = new Intent(MyCreatedEventsActivity.this, CreateEventActivity.class);
            intent.putExtra("eventId", selectedEvent.getEventId());
            intent.putExtra("eventName", selectedEvent.getName());
            intent.putExtra("eventDescription", selectedEvent.getDescription());
            startActivity(intent);
        });

        // Exit button
        findViewById(R.id.exit_button_created_events).setOnClickListener(v -> finish());

        // Navigation bar
        findViewById(R.id.new_event_button_created).setOnClickListener(v -> {
            EventTypeFragment fragment = new EventTypeFragment();
            fragment.show(getSupportFragmentManager(), "NewEventFragment");
        });
        findViewById(R.id.qr_button_created).setOnClickListener(v ->
                startActivity(new Intent(this, SearchScreen.class)));
        findViewById(R.id.home_button_created).setOnClickListener(v ->
                startActivity(new Intent(this, HomePage.class)));
        findViewById(R.id.browse_button_created).setOnClickListener(v ->
                startActivity(new Intent(this, BrowseEventsActivity.class)));
        findViewById(R.id.profile_button_created).setOnClickListener(v ->
                startActivity(new Intent(this, Profile.class)));

        loadCreatedEvents();
    }

    private void loadCreatedEvents() {
        db.collection("events")
                .whereEqualTo("organizerId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    createdEvents.clear();
                    eventNames.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Events event = doc.toObject(Events.class);
                        event.setEventId(doc.getId());
                        createdEvents.add(event);
                        eventNames.add(event.getName());
                    }

                    adapter.notifyDataSetChanged();

                    if (createdEvents.isEmpty()) {
                        Toast.makeText(this, "No created events found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load created events", Toast.LENGTH_SHORT).show());
    }
}

