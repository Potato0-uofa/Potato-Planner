package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class CreateEventActivity extends AppCompatActivity {
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
    }
}