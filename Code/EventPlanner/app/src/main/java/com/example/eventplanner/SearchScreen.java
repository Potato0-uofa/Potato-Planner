package com.example.eventplanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
/** Activity for scanning QR codes to quickly navigate to an event page. */
public class SearchScreen extends AppCompatActivity {

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    Intent intent = new Intent(SearchScreen.this, EventDescriptionView.class);
                    intent.putExtra("eventId", result.getContents());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchScanner();
                } else {
                    Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);
        findViewById(R.id.new_event_button_search).setOnClickListener(v -> {
            EventTypeFragment fragment = new EventTypeFragment();
            fragment.show(getSupportFragmentManager(), "NewEventFragment");
        });

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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                launchScanner();
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan an event QR code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }
}
