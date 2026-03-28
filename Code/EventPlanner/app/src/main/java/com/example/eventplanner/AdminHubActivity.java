package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AdminHubActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_hub);
        findViewById(R.id.btn_browse_events).setOnClickListener(v ->
                startActivity(new Intent(AdminHubActivity.this, AdminBrowseEventsActivity.class)));

        findViewById(R.id.btn_browse_images).setOnClickListener(v ->
                startActivity(new Intent(AdminHubActivity.this, AdminBrowseImagesActivity.class)));

        findViewById(R.id.btn_remove_organizers).setOnClickListener(v ->
                startActivity(new Intent(AdminHubActivity.this, AdminRemoveOrganizersActivity.class)));
    }
}
