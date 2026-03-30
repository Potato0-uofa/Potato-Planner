package com.example.eventplanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Activity that displays the details of a specific event and allows the user to join
 * or leave the waitlist, and for organizers to view the waitlist.
 */
public class EventDescriptionView extends AppCompatActivity {

    private EventRepository eventRepository;
    private LocationRepository locationRepository;
    private WaitingList waitingList;
    private ListenerRegistration waitlistListener;
    private TextView tvWaitlistCount;
    private String eventId;
    private String deviceId;
    private Button btnJoinEvent;
    private Button btnLeaveEvent;
    private Button btnViewWaitlist;
    private boolean geolocationRequired = false;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isTrackingLocation = false;

    private boolean isOrganizer = false;

    private Button btnViewQr;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            joinWaitlistAndTrack();
                        } else {
                            Toast.makeText(this,
                                    "Location permission is required to join this event.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        btnViewQr = findViewById(R.id.view_qr_button);
        btnViewQr.setOnClickListener(v -> showQrDialog(eventId));
        btnViewQr.setVisibility(View.GONE);

        eventRepository = new EventRepository();
        locationRepository = new LocationRepository();
        waitingList = new WaitingList();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            eventId = "test_event_1";
        }


        String eventName = getIntent().getStringExtra("eventName");
        String eventDescription = getIntent().getStringExtra("eventDescription");

        tvWaitlistCount = findViewById(R.id.Waitlist_Count);
        btnJoinEvent = findViewById(R.id.join_event_button);
        btnLeaveEvent = findViewById(R.id.leave_event_button);
        btnViewWaitlist = findViewById(R.id.view_waitlist_button);

        buildLocationCallback();

        btnJoinEvent.setOnClickListener(v ->
                eventRepository.fetchEventById(eventId, new EventRepository.EventCallback() {
                    @Override
                    public void onSuccess(Events event) {
                        // Registration window check — UNCHANGED from original
                        String regStart = event.getRegistrationStart();
                        if (regStart != null && !regStart.isEmpty()) {
                            try {
                                java.text.SimpleDateFormat sdf =
                                        new java.text.SimpleDateFormat("yyyy-MM-dd",
                                                java.util.Locale.getDefault());
                                java.util.Date startDate = sdf.parse(regStart);
                                java.util.Date today = new java.util.Date();
                                if (startDate != null && today.before(startDate)) {
                                    Toast.makeText(EventDescriptionView.this,
                                            "Registration opens on " + regStart,
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }
                            } catch (java.text.ParseException ignored) {}
                        }

                        //  NEW — cache the flag and route accordingly
                        geolocationRequired = event.isGeolocationRequired();
                        if (geolocationRequired) {
                            handleGeolocationJoin();
                        } else {
                            joinWaitlistAndTrack(); // no tracking needed
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(EventDescriptionView.this,
                                "Could not verify registration period", Toast.LENGTH_SHORT).show();
                    }
                })
        );

        btnLeaveEvent.setOnClickListener(v -> {
            eventRepository.leaveWaitingList(eventId, deviceId, new EventRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(EventDescriptionView.this, "Left Waitlist!", Toast.LENGTH_SHORT).show();
                    updateButtonVisibility(false);
                    stopLocationTracking();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(EventDescriptionView.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnViewWaitlist.setOnClickListener(v -> {
            Intent intent = new Intent(EventDescriptionView.this, EventWaitlistActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        // Force visible so you can see the button immediately when running the app
        btnViewWaitlist.setVisibility(View.GONE); // hidden by default

        eventRepository.fetchEventById(eventId, new EventRepository.EventCallback() {

            @Override
            public void onSuccess(Events event) {
// Show organizer controls if applicable

                ((TextView) findViewById(R.id.event_name)).setText(event.getName() != null ? event.getName() : "");
                ((TextView) findViewById(R.id.event_details)).setText(event.getDetails() != null ? event.getDetails() : "");
                ((TextView) findViewById(R.id.event_description_main)).setText(event.getDescription() != null ? event.getDescription() : "");

                if (deviceId.equals(event.getOrganizerId()) ||
                        event.getCoOrganizerIds().contains(deviceId)) {
                    btnViewWaitlist.setVisibility(View.VISIBLE);
                }

// Show registration period
                TextView regPeriodText = findViewById(R.id.registration_period_text);
                String start = event.getRegistrationStart();
                String end = event.getRegistrationEnd();
                if (start != null && end != null && !start.isEmpty() && !end.isEmpty()) {
                    regPeriodText.setText("Registration: " + start + " – " + end);
                } else if (end != null && !end.isEmpty()) {
                    regPeriodText.setText("Registration closes: " + end);
                }

// Fill event info
                TextView eventNameText = findViewById(R.id.event_name);
                TextView eventDetailsText = findViewById(R.id.event_details);
                TextView eventDescriptionText = findViewById(R.id.event_description_main);

                eventNameText.setText(event.getName() != null ? event.getName() : "Unnamed Event");
                eventDetailsText.setText(event.getDate() != null ? event.getDate() : "No event details");
                eventDescriptionText.setText(event.getDescription() != null ? event.getDescription() : "No description available");

// Set organizer flag
                isOrganizer = deviceId.equals(event.getOrganizerId()) ||
                        event.getCoOrganizerIds().contains(deviceId);
                if (isOrganizer) {
                    btnJoinEvent.setVisibility(View.GONE);
                    btnLeaveEvent.setVisibility(View.GONE);
                    btnViewWaitlist.setVisibility(View.VISIBLE);
                    btnViewQr.setVisibility(View.VISIBLE);
                }

                checkWaitlistStatus();
            }
            @Override
            public void onFailure(Exception e) {
// keep button hidden
            }
            public void onFailure(Exception e) {}
        });


        // US 01.05.05 - Info box click listener for detailed lottery guidelines
        View lotteryInfoBox = findViewById(R.id.lottery_info_box);
        if (lotteryInfoBox != null) {
            lotteryInfoBox.setOnClickListener(v -> showLotteryGuidelines());
        }

        findViewById(R.id.comment_button_entrant).setOnClickListener(v -> {
            Intent intent = new Intent(EventDescriptionView.this, CommentsActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        checkWaitlistStatus();
        setupNavigation();
        startListeningToWaitlist();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitlistListener != null) waitlistListener.remove();
        stopLocationTracking(); //  NEW — clean up when activity closes
    }

    private void handleGeolocationJoin() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            joinWaitlistAndTrack();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Location Required")
                    .setMessage("This event requires your location to be shared with the organiser " +
                            "while you are on the waitlist. Please grant location permission to continue.")
                    .setPositiveButton("Grant Permission",
                            (dialog, which) -> locationPermissionLauncher.launch(
                                    Manifest.permission.ACCESS_FINE_LOCATION))
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void joinWaitlistAndTrack() {
        eventRepository.joinWaitingList(eventId, deviceId, new EventRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EventDescriptionView.this,
                        "Joined Waitlist!", Toast.LENGTH_SHORT).show(); // same as original
                updateButtonVisibility(true);                           // same as original
                if (geolocationRequired) {
                    startLocationTracking();                            // ✅ new addition
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDescriptionView.this,
                        "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null) return;
                android.location.Location loc = result.getLastLocation();
                if (loc == null) return;

                locationRepository.updateLocation(
                        eventId, deviceId,
                        loc.getLatitude(), loc.getLongitude(),
                        new LocationRepository.SimpleCallback() {
                            @Override public void onSuccess() {}
                            @Override public void onFailure(Exception e) {}
                        });
            }
        };
    }

    @SuppressLint("MissingPermission") // permission is verified before this is called
    private void startLocationTracking() {
        if (isTrackingLocation) return;

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
                .setMinUpdateIntervalMillis(5_000L)
                .build();

        fusedLocationClient.requestLocationUpdates(
                request, locationCallback, Looper.getMainLooper());

        isTrackingLocation = true;
    }

    private void stopLocationTracking() {
        if (!isTrackingLocation) return;
        fusedLocationClient.removeLocationUpdates(locationCallback);
        isTrackingLocation = false;

        locationRepository.removeLocation(eventId, deviceId,
                new LocationRepository.SimpleCallback() {
                    @Override public void onSuccess() {}
                    @Override public void onFailure(Exception e) {}
                });
    }

    private void showLotteryGuidelines() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Lottery Guidelines")
                .setMessage("1. Joining the waitlist does not guarantee a spot.\n" +
                        "2. Once the organizer initiates the draw, winners are selected randomly.\n" +
                        "3. Winners will receive a notification to accept or decline their invitation.\n" +
                        "4. If an invitation is declined, a new winner will be drawn.")
                .setPositiveButton("Got it", null)
                .show();
    }

    private void checkWaitlistStatus() {
        if (isOrganizer) {
            btnJoinEvent.setVisibility(View.GONE);
            btnLeaveEvent.setVisibility(View.GONE);
            btnViewWaitlist.setVisibility(View.VISIBLE);
            btnViewQr.setVisibility(View.VISIBLE);
            return;
        }

        eventRepository.isOnWaitingList(eventId, deviceId, new EventRepository.WaitlistStatusCallback() {
            @Override
            public void onSuccess(boolean isOnWaitlist) {
                eventRepository.fetchEventById(eventId, new EventRepository.EventCallback() {
                    @Override
                    public void onSuccess(Events event) {
                        if (deviceId.equals(event.getOrganizerId()) ||
                                event.getCoOrganizerIds().contains(deviceId)) {
                            btnJoinEvent.setVisibility(View.GONE);
                            btnLeaveEvent.setVisibility(View.GONE);
                        } else {
                            updateButtonVisibility(isOnWaitlist);
                            if (isOnWaitlist) {
                                geolocationRequired = event.isGeolocationRequired();
                                if (geolocationRequired &&
                                        ContextCompat.checkSelfPermission(
                                                EventDescriptionView.this,
                                                Manifest.permission.ACCESS_FINE_LOCATION)
                                                == PackageManager.PERMISSION_GRANTED) {
                                    startLocationTracking();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        updateButtonVisibility(isOnWaitlist);
                    }
                });
            }
            @Override
            public void onFailure(Exception e) { /* silent */ }
        });
    }



    private void updateButtonVisibility(boolean isOnWaitlist) {
        btnJoinEvent.setVisibility(isOnWaitlist ? View.GONE : View.VISIBLE);
        btnLeaveEvent.setVisibility(isOnWaitlist ? View.VISIBLE : View.GONE);
    }

    private void startListeningToWaitlist() {
        waitlistListener = eventRepository.listenToWaitlistCount(eventId, new EventRepository.CountCallback() {
            @Override
            public void onSuccess(int count) {
                waitingList.setCloudCount(count);
                tvWaitlistCount.setText("Waitlist: " + waitingList.getCount() + " people");
            }

            @Override
            public void onFailure(Exception e) {
                // Ignore error for demo
            }
        });
    }

    private void setupNavigation() {
        View homeBtn = findViewById(R.id.home_button_event_page);
        if (homeBtn != null) homeBtn.setOnClickListener(v -> startActivity(new Intent(this, HomePage.class)));

        // View searchBtn = findViewById(R.id.search_button_event_page);
        // if (searchBtn != null) searchBtn.setOnClickListener(v -> startActivity(new Intent(this, SearchScreen.class)));

        View browseBtn = findViewById(R.id.browse_button_event_page);
        if (browseBtn != null) browseBtn.setOnClickListener(v -> startActivity(new Intent(this, BrowseEventsActivity.class)));

        View profileBtn = findViewById(R.id.profile_button_event_page);
        if (profileBtn != null) profileBtn.setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));

        View exitBtn = findViewById(R.id.exit_button_event_page);
        if (exitBtn != null) exitBtn.setOnClickListener(v -> finish());

        findViewById(R.id.new_event_button_event_page).setOnClickListener(v -> {
            EventTypeFragment fragment = new EventTypeFragment();
            fragment.show(getSupportFragmentManager(), "NewEventFragment");
        });

    }

    private android.graphics.Bitmap generateQrBitmap(String text) throws com.google.zxing.WriterException {
        com.google.zxing.qrcode.QRCodeWriter writer = new com.google.zxing.qrcode.QRCodeWriter();
        com.google.zxing.common.BitMatrix bitMatrix = writer.encode(text, com.google.zxing.BarcodeFormat.QR_CODE, 600, 600);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
            }
        }
        return bitmap;
    }

    private void showQrDialog(String eventId) {
        try {
            android.graphics.Bitmap bitmap = generateQrBitmap(eventId);
            android.widget.ImageView imageView = new android.widget.ImageView(this);
            imageView.setImageBitmap(bitmap);
            imageView.setPadding(32, 32, 32, 32);

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Event QR Code")
                    .setMessage("Scan this QR code to open the event.")
                    .setView(imageView)
                    .setPositiveButton("OK", null)
                    .show();

        } catch (com.google.zxing.WriterException e) {
            android.widget.Toast.makeText(this, "Failed to generate QR code", android.widget.Toast.LENGTH_SHORT).show();
        }
        }


}