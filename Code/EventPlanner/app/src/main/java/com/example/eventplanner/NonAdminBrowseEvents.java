package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class NonAdminBrowseEvents extends AppCompatActivity {
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
            startActivity(new Intent(NonAdminBrowseEvents.this, HomePage.class));
        });
    }
}