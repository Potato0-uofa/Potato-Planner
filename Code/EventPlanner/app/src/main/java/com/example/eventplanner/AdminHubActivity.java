package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Central admin dashboard activity providing navigation to all admin management screens
 * including event browsing, image browsing, organizer removal, and comment browsing.
 */
public class AdminHubActivity extends AppCompatActivity {

    /**
     * Initializes the activity and sets up navigation buttons to each admin screen.
     *
     * @param savedInstanceState previously saved activity state, if any
     */
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
        findViewById(R.id.btn_browse_comments).setOnClickListener(v ->
                startActivity(new Intent(AdminHubActivity.this, AdminBrowseCommentsActivity.class)));
    }
}
