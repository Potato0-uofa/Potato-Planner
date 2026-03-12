package com.example.eventplanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateEventActivity extends AppCompatActivity {

    private final EventRepository eventRepository = new EventRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event_view);

        // Home bar navigation
        findViewById(R.id.new_event_button_create_page).setOnClickListener(v -> {
            //Do nothing since already on new event page
        });

        findViewById(R.id.search_button_create_page).setOnClickListener(v -> {
            startActivity(new Intent(CreateEventActivity.this, SearchScreen.class));
        });

        findViewById(R.id.home_button_create_page).setOnClickListener(v -> {
            startActivity(new Intent(CreateEventActivity.this, HomePage.class));
        });

        findViewById(R.id.browse_button_create_page).setOnClickListener(v -> {
            startActivity(new Intent(CreateEventActivity.this, NonAdminBrowseEvents.class));
        });

        findViewById(R.id.profile_button_create_page).setOnClickListener(v -> {
            startActivity(new Intent(CreateEventActivity.this, HomePage.class));
        });

        // Create event button
        findViewById(R.id.create_button_create_page).setOnClickListener(v -> {
            EditText nameInput = findViewById(R.id.event_name);
            EditText descriptionInput = findViewById(R.id.event_description_main);
            EditText closureDateInput = findViewById(R.id.event_closure_date);
            EditText waitlistLimitInput = findViewById(R.id.waitlist_limit_input);

            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String closureDate = closureDateInput.getText().toString().trim();
            String waitlistLimitStr = waitlistLimitInput.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty() || closureDate.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the organizer's device ID
            @SuppressLint("HardwareIds") String organizerId = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            Events event = new Events(name, closureDate, description, "");
            event.setOrganizerId(organizerId);

            // Set waitlist limit if provided
            if (!waitlistLimitStr.isEmpty()) {
                int limit = Integer.parseInt(waitlistLimitStr);
                event.setWaitlistLimit(limit);
            }
            // If blank, waitlistLimit stays -1 (no limit)

            eventRepository.createEvent(event, new EventRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(CreateEventActivity.this, "Event created!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateEventActivity.this, HomePage.class));
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(CreateEventActivity.this, "Failed to create event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}