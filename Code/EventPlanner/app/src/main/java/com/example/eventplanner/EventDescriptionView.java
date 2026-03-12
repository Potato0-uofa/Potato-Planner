package com.example.eventplanner;

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
    }
}