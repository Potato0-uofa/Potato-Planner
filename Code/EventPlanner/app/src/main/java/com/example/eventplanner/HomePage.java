package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        findViewById(R.id.new_event_button_home).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, CreateEventActivity.class));
        });

        findViewById(R.id.search_button_home).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, SearchScreen.class));
        });

        findViewById(R.id.home_button_home).setOnClickListener(v -> {
            // Do nothing since already on home page
        });

        findViewById(R.id.browse_button_home).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, NonAdminBrowseEvents.class));
        });

        findViewById(R.id.profile_button_home).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, Profile.class));
        });

        findViewById(R.id.notification_button_home).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, NotificationLogs.class));
        });
    }
}