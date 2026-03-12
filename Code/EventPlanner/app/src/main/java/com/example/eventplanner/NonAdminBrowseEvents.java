package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class NonAdminBrowseEvents extends AppCompatActivity {

    private final EventRepository eventRepository = new EventRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events_view);

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

        loadEvents();
    }

    private void loadEvents() {
        eventRepository.fetchOpenEvents(new EventRepository.EventsCallback() {
            @Override
            public void onSuccess(List<Events> events) {
                ListView listView = findViewById(R.id.browse_event_list);
                List<String> eventNames = new ArrayList<>();
                for (Events e : events) {
                    eventNames.add(e.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        NonAdminBrowseEvents.this,
                        android.R.layout.simple_list_item_1,
                        eventNames
                );
                listView.setAdapter(adapter);

                listView.setOnItemClickListener((parent, view, position, id) -> {
                    Events selectedEvent = events.get(position);
                    Intent intent = new Intent(NonAdminBrowseEvents.this, EventDescriptionView.class);
                    intent.putExtra("eventId", selectedEvent.getEventId());
                    intent.putExtra("eventName", selectedEvent.getName());
                    intent.putExtra("eventDescription", selectedEvent.getDescription());
                    intent.putExtra("eventDate", selectedEvent.getDate());
                    startActivity(intent);
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(NonAdminBrowseEvents.this,
                        "Failed to load events: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}