package com.example.eventplanner;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class NonAdminBrowseEvents extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events_view);
    }
}