package com.example.eventplanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    private RegistrationRepository registrationRepository;

    private boolean isOrganizer = false;
    private boolean isCoOrganizer = false;
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

    private final androidx.activity.result.ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // Show local preview immediately, just like CreateEventActivity
                    ImageView eventImageView = findViewById(R.id.event_image_icon);
                    if (eventImageView != null) {
                        eventImageView.setImageURI(uri);
                    }
                    // Then upload in the background
                    uploadEventImage(uri);
                }
            });

    /**
     * Uploads a new event image to Firebase Storage and updates the imageUrl in Firestore.
     * Similar implementation to CreateEventActivity.
     *
     * @param uri the URI of the image to upload
     */
    private void uploadEventImage(android.net.Uri uri) {
        com.google.firebase.storage.StorageReference storageRef = com.google.firebase.storage.FirebaseStorage
                .getInstance().getReference("event_images")
                .child("event_" + eventId + "_" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            // Update imageUrl in Firestore
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection("events")
                                    .document(eventId)
                                    .update("imageUrl", downloadUri.toString())
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(EventDescriptionView.this, "Photo updated!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to update photo", Toast.LENGTH_SHORT).show());
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        eventRepository = new EventRepository();
        locationRepository = new LocationRepository();
        registrationRepository = new RegistrationRepository();
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
                            } catch (java.text.ParseException e) {
                            }
                        }

                        geolocationRequired = event.isGeolocationRequired();
                        if (geolocationRequired) {
                            handleGeolocationJoin();
                        } else {
                            joinWaitlistAndTrack();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(EventDescriptionView.this,
                                "Could not verify registration period", Toast.LENGTH_SHORT).show();
                    }
                })
        );

        btnLeaveEvent.setOnClickListener(v ->
                eventRepository.leaveWaitingList(eventId, deviceId,
                        new EventRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(EventDescriptionView.this,
                                        "Left Waitlist!", Toast.LENGTH_SHORT).show();
                                stopLocationTracking();
                                updateButtonVisibility(false);

                                registrationRepository.leaveEvent(eventId, deviceId,
                                        new RegistrationRepository.SimpleCallback() {
                                            @Override
                                            public void onSuccess() { }

                                            @Override
                                            public void onFailure(Exception e) { }
                                        });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(EventDescriptionView.this,
                                        "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
        );

        btnViewWaitlist.setOnClickListener(v -> {
            FragmentFinalEntrantList fragment = FragmentFinalEntrantList.newInstance(eventId);
            fragment.show(getSupportFragmentManager(), "FinalEntrantList");
        });

        findViewById(R.id.comment_button_entrant).setOnClickListener(v -> {
            Intent intent = new Intent(EventDescriptionView.this, CommentsActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });


        // Force visible so you can see the button immediately when running the app
        btnViewWaitlist.setVisibility(View.GONE); // hidden by default

        eventRepository.fetchEventById(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Events event) {
                ((TextView) findViewById(R.id.event_name)).setText(event.getName());

                isCoOrganizer = event.getCoOrganizerIds() != null
                        && event.getCoOrganizerIds().contains(deviceId);

                if (deviceId.equals(event.getOrganizerId()) || isCoOrganizer) {
                    btnViewWaitlist.setVisibility(View.VISIBLE);
                }

                // Set role badge for all users
                TextView roleBadge = findViewById(R.id.role_badge);
                if (roleBadge != null) {
                    roleBadge.setVisibility(View.VISIBLE);
                    if (deviceId.equals(event.getOrganizerId())) {
                        roleBadge.setText("Organizer");
                        roleBadge.setBackgroundResource(R.drawable.role_badge_organizer);
                        roleBadge.setTextColor(0xFFC498D0);
                    } else if (isCoOrganizer) {
                        roleBadge.setText("Co-Organizer");
                        roleBadge.setBackgroundResource(R.drawable.role_badge_coorganizer);
                        roleBadge.setTextColor(0xFFFFB74D);
                    } else {
                        roleBadge.setText("Attendee");
                        roleBadge.setBackgroundResource(R.drawable.role_badge_entrant);
                        roleBadge.setTextColor(0xFF64FFDA);
                    }
                }

                // Hide join/leave buttons if current user is the organizer
                if (deviceId.equals(event.getOrganizerId())) {
                    isOrganizer = true;
                    btnJoinEvent.setVisibility(View.GONE);
                    btnLeaveEvent.setVisibility(View.GONE);
                }

                // Co-organizers cannot join the entrant pool
                if (isCoOrganizer && !deviceId.equals(event.getOrganizerId())) {
                    btnJoinEvent.setVisibility(View.GONE);
                    btnLeaveEvent.setVisibility(View.GONE);
                }

                // Show edit photo button, but only for organizer of the event
                Button editPhotoButton = findViewById(R.id.edit_photo_button);
                if (editPhotoButton != null) {
                    if (deviceId.equals(event.getOrganizerId())) {
                        editPhotoButton.setVisibility(View.VISIBLE);
                        editPhotoButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
                    } else {
                        editPhotoButton.setVisibility(View.GONE);
                    }
                }

                // Load the existing event image (if there is one)
                if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                    ImageView eventImageView = findViewById(R.id.event_image_icon);
                    if (eventImageView != null) {
                        eventImageView.setImageURI(android.net.Uri.parse(event.getImageUrl()));
                    }
                }

                // Show QR button only for organizer of non-private events
                Button viewQrButton = findViewById(R.id.view_qr_button);
                if (viewQrButton != null) {
                    boolean showQr = !event.isPrivate();
                    viewQrButton.setVisibility(showQr ? View.VISIBLE : View.GONE);
                    viewQrButton.setOnClickListener(v -> showQrDialog(eventId));
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

                // Set description and details from Firestore
                TextView descriptionView = findViewById(R.id.event_description_main);
                if (descriptionView != null && event.getDescription() != null) {
                    descriptionView.setText(event.getDescription());
                }

                TextView detailsView = findViewById(R.id.event_details);
                if (detailsView != null && event.getDetails() != null) {
                    detailsView.setText(event.getDetails());
                }

                Button inviteEntrantsButton = findViewById(R.id.invite_entrants_button);
                if (inviteEntrantsButton != null) {
                    if (deviceId.equals(event.getOrganizerId())) {
                        inviteEntrantsButton.setVisibility(View.VISIBLE);
                        inviteEntrantsButton.setOnClickListener(v -> {
                            String entrantOption = event.isPrivate()
                                    ? "Invite to private waitlist"
                                    : "Invite entrant";
                            String[] options = new String[]{entrantOption, "Invite co-organizer"};

                            new MaterialAlertDialogBuilder(EventDescriptionView.this)
                                    .setTitle("Send Invitation")
                                    .setItems(options, (dialog, which) -> {
                                        Intent intent = new Intent(EventDescriptionView.this, InviteEntrantActivity.class);
                                        intent.putExtra("eventId", eventId);
                                        if (which == 1) {
                                            intent.putExtra(
                                                    InviteEntrantActivity.EXTRA_INVITE_TYPE,
                                                    InviteEntrantActivity.INVITE_TYPE_COORGANIZER
                                            );
                                        } else {
                                            intent.putExtra(
                                                    InviteEntrantActivity.EXTRA_INVITE_TYPE,
                                                    InviteEntrantActivity.INVITE_TYPE_WAITLIST
                                            );
                                        }
                                        startActivity(intent);
                                    })
                                    .show();
                        });
                    } else {
                        inviteEntrantsButton.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onFailure(Exception e) {
                // keep button hidden
            }
        });

        // US 01.05.05 - Info box click listener for detailed lottery guidelines
        View lotteryInfoBox = findViewById(R.id.lottery_info_box);
        if (lotteryInfoBox != null) {
            lotteryInfoBox.setOnClickListener(v -> showLotteryGuidelines());
        }

        checkWaitlistStatus();
        setupNavigation();
        startListeningToWaitlist();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitlistListener != null) waitlistListener.remove();
        stopLocationTracking();
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
        eventRepository.isOnWaitingList(eventId, deviceId, new EventRepository.WaitlistStatusCallback() {
            @Override
            public void onSuccess(boolean isOnWaitlist) {
                updateButtonVisibility(isOnWaitlist);
            }

            @Override
            public void onFailure(Exception e) {
                // Silently fail
            }
        });
    }

    private void updateButtonVisibility(boolean isOnWaitlist) {
        if (isOrganizer) return;
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

    private void showQrDialog(String eventId) {
        try {
            com.google.zxing.qrcode.QRCodeWriter writer = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(eventId, com.google.zxing.BarcodeFormat.QR_CODE, 600, 600);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                }
            }
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
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigation() {
        View homeBtn = findViewById(R.id.home_button_event_page);
        if (homeBtn != null) homeBtn.setOnClickListener(v -> startActivity(new Intent(this, HomePage.class)));

        View browseBtn = findViewById(R.id.browse_button_event_page);
        if (browseBtn != null) browseBtn.setOnClickListener(v -> startActivity(new Intent(this, NonAdminBrowseEvents.class)));

        View profileBtn = findViewById(R.id.profile_button_event_page);
        if (profileBtn != null) profileBtn.setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));

        View exitBtn = findViewById(R.id.exit_button_event_page);
        if (exitBtn != null) exitBtn.setOnClickListener(v -> finish());
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
        if (isCoOrganizer) {
            Toast.makeText(this,
                    "Co-organizers cannot join the entrant pool",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        eventRepository.joinWaitingList(eventId, deviceId, new EventRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EventDescriptionView.this,
                        "Joined Waitlist!", Toast.LENGTH_SHORT).show();
                updateButtonVisibility(true);
                registrationRepository.joinEvent(eventId, deviceId,
                        new RegistrationRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() { }

                            @Override
                            public void onFailure(Exception e) { }
                        });
                if (geolocationRequired) {
                    startLocationTracking();
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

    @SuppressLint("MissingPermission")
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

}
