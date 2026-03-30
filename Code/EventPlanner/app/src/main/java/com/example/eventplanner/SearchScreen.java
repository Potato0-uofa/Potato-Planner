package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
public class SearchScreen extends AppCompatActivity {

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    Intent intent = new Intent(SearchScreen.this, EventDescriptionView.class);
                    intent.putExtra("eventId", result.getContents());
                    startActivity(intent);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);
        findViewById(R.id.new_event_button_search).setOnClickListener(v ->
                startActivity(new Intent(SearchScreen.this, CreateEventActivity.class)));

        findViewById(R.id.qr_button_search).setOnClickListener(v -> {
// Already on QR page
        });

        findViewById(R.id.home_button_search).setOnClickListener(v ->
                startActivity(new Intent(SearchScreen.this, HomePage.class)));

        findViewById(R.id.browse_button_search).setOnClickListener(v ->
                startActivity(new Intent(SearchScreen.this, BrowseEventsActivity.class)));

        findViewById(R.id.profile_button_search).setOnClickListener(v ->
                startActivity(new Intent(SearchScreen.this, Profile.class)));

        findViewById(R.id.btn_scan_qr).setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan an event QR code");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            barcodeLauncher.launch(options);
        });
    }
}