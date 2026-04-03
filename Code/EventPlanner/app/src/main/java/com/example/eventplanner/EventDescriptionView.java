package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Activity that displays the details of a specific event and allows the user to join
 * or leave the waitlist, and for organizers to view the waitlist.
 */
public class EventDescriptionView extends AppCompatActivity {

    private EventRepository eventRepository;

    private boolean isOrganizer = false;
    private WaitingList waitingList;
    private ListenerRegistration waitlistListener;
    private TextView tvWaitlistCount;
    private String eventId;
    private String deviceId;
    private Button btnJoinEvent;
    private Button btnLeaveEvent;
    private Button btnViewWaitlist;

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
        waitingList = new WaitingList();

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


        btnJoinEvent.setOnClickListener(v -> {
            eventRepository.fetchEventById(eventId, new EventRepository.EventCallback() {
                @Override
                public void onSuccess(Events event) {
                    String regStart = event.getRegistrationStart();
                    if (regStart != null && !regStart.isEmpty()) {
                        try {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                            java.util.Date startDate = sdf.parse(regStart);
                            java.util.Date today = new java.util.Date();
                            if (startDate != null && today.before(startDate)) {
                                Toast.makeText(EventDescriptionView.this,
                                        "Registration opens on " + regStart,
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                        } catch (java.text.ParseException e) {
                            // If date can't be parsed, allow joining
                        }
                    }

                    // Date check passed, join the waitlist
                    eventRepository.joinWaitingList(eventId, deviceId, new EventRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(EventDescriptionView.this, "Joined Waitlist!", Toast.LENGTH_SHORT).show();
                            updateButtonVisibility(true);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(EventDescriptionView.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(EventDescriptionView.this,
                            "Could not verify registration period", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnLeaveEvent.setOnClickListener(v -> {
            eventRepository.leaveWaitingList(eventId, deviceId, new EventRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(EventDescriptionView.this, "Left Waitlist!", Toast.LENGTH_SHORT).show();
                    updateButtonVisibility(false);
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

                if (deviceId.equals(event.getOrganizerId()) ||
                        event.getCoOrganizerIds().contains(deviceId)) {
                    btnViewWaitlist.setVisibility(View.VISIBLE);
                }

                // Hide join/leave buttons if current user is the organizer
                if (deviceId.equals(event.getOrganizerId())) {
                    isOrganizer = true;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitlistListener != null) {
            waitlistListener.remove();
        }
    }
}