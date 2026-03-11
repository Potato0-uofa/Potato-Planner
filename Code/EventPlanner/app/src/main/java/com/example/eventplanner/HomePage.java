package com.example.eventplanner;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        findViewById(R.id.new_event_button_home).setOnClickListener(v -> {
            // TODO: Navigate to the Create Event screen
        });

        findViewById(R.id.search_button_home).setOnClickListener(v -> {
            // TODO: Navigate to the Search screen
        });

        findViewById(R.id.home_button_home).setOnClickListener(v -> {
            // TODO: Refresh the page (could also just do nothing)
        });

        findViewById(R.id.browse_button_home).setOnClickListener(v -> {
            // TODO: Navigate to the Browse Events screen
        });

        findViewById(R.id.profile_button_home).setOnClickListener(v -> {
            // TODO: Navigate to the User Profile screen
        });

        findViewById(R.id.notification_button_home).setOnClickListener(v -> {
            // TODO: Navigate to the Notifications screen
        });
    }
}