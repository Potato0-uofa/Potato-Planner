package com.example.eventplanner;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
/**
 * Activity that allows administrators to browse uploaded images
 * associated with events.
 * Implements user story:
 * - US 03.06.01: Browse uploaded images
 */

public class AdminBrowseImagesActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_images);

        recyclerView = findViewById(R.id.recycler_admin_images);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}