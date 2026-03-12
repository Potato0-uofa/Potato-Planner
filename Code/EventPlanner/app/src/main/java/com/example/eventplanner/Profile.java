package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Profile extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Settings button
        findViewById(R.id.settings_button_profile).setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, SettingsView.class));
        });

        // Home bar navigation
        findViewById(R.id.new_event_button_profile).setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, CreateEventActivity.class));
        });

        findViewById(R.id.search_button_profile).setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, SearchScreen.class));
        });

        findViewById(R.id.home_button_profile).setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, HomePage.class));
        });

        findViewById(R.id.browse_button_profile).setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, NonAdminBrowseEvents.class));
        });

        findViewById(R.id.profile_button_profile).setOnClickListener(v -> {
            // Already on profile, do nothing
        });
    }


}