package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class EventDescriptionView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        String eventId = getIntent().getStringExtra("eventId");
        String eventName = getIntent().getStringExtra("eventName");
        String eventDescription = getIntent().getStringExtra("eventDescription");
        String eventDate = getIntent().getStringExtra("eventDate");

        // Set the data to your views
        ((TextView) findViewById(R.id.event_name)).setText(eventName);
        ((TextView) findViewById(R.id.event_details)).setText(eventDescription);

        findViewById(R.id.home_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, CreateEventActivity.class));
        });

        findViewById(R.id.search_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, SearchScreen.class));
        });

        findViewById(R.id.home_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, HomePage.class));
        });

        findViewById(R.id.browse_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, NonAdminBrowseEvents.class));
        });

        findViewById(R.id.profile_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, Profile.class));
        });
    }
}