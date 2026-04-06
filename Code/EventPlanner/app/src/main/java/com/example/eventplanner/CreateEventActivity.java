package com.example.eventplanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import java.util.List;
import java.util.ArrayList;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Activity for creating a new event in the app. Provides an organizer with an interface to input
 * an event's name, description, date, and a waitlist limit. Also supports selecting and uploading
 * an event image. When successfully created the event is persisted using the eventRepository and
 * the user is directed back to the home page. The view also has a navigation bar at the bottom.
 * If launched from a setup fragment (public or private), it loads the correct layout and updates
 * the already-created event rather than creating a new one.
 */
public class CreateEventActivity extends AppCompatActivity {

    /**
     * Repository used to perform create, edit, delete, and read operations on Events objects.
     */
    private final EventRepository eventRepository = new EventRepository();

    /**
     * Firebase Storage reference used to upload event images.
     */
    private final StorageReference storageReference =
            FirebaseStorage.getInstance("gs://potato-planner-2350c.appspot.com").getReference("event_images");

    /**
     * URI of the image selected by the organizer, if any.
     */
    private Uri selectedImageUri = null;

    /**
     * Image preview for the selected event image.
     */
    private ImageView eventImageView;

    /**
     * The Firestore event ID passed in from the setup fragment, if any.
     * If non-null, the activity will update this existing event instead of creating a new one.
     */
    private String existingEventId = null;

    /**
     * Whether this event is private. Determines which layout to inflate and which
     * button IDs to use for navigation and event creation.
     */
    private boolean isPrivate;

    /**
     * Launcher used to pick an image from device storage.
     */
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (eventImageView != null) {
                        eventImageView.setImageURI(uri);
                    }
                }
            });

    /**
     * Sets up navigation, image selection, and event creation logic.
     * If an eventId is passed via Intent extras, the activity loads the correct
     * layout (public or private) and updates the existing event on confirmation.
     * Otherwise, falls back to creating a brand new event.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if we were launched from the setup fragment
        existingEventId = getIntent().getStringExtra("eventId");
        isPrivate = getIntent().getBooleanExtra("isPrivate", false);

        // Pick up image URI passed from setup fragment if any
        Uri dataUri = getIntent().getData();
        if (dataUri != null) {
            selectedImageUri = dataUri;
            if (eventImageView != null) {
                eventImageView.setImageURI(selectedImageUri);
            }
        }

        // Load the correct layout based on public/private
        if (isPrivate) {
            setContentView(R.layout.activity_create_event_private_view);
        } else {
            setContentView(R.layout.activity_create_event_view);
        }

        eventImageView = findViewById(R.id.event_image_icon);

        findViewById(R.id.map_view_button).setOnClickListener(v -> {
            if (existingEventId == null) {
                Toast.makeText(this, "Please save the event first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(CreateEventActivity.this, MapViewActivity.class);
            intent.putExtra("eventId", existingEventId);
            startActivity(intent);
        });

        // Pre-fill fields for the setup fragment
        if (existingEventId != null) {
            eventRepository.fetchEventById(existingEventId, new EventRepository.EventCallback() {
                @Override
                public void onSuccess(Events event) {
                    EditText nameInput         = findViewById(R.id.event_name);
                    EditText descriptionInput  = findViewById(R.id.event_description_main);
                    EditText detailsInput      = findViewById(R.id.event_details);
                    EditText closureDateInput  = findViewById(R.id.event_closure_date);
                    EditText waitlistLimitInput = findViewById(R.id.waitlist_limit_input);
                    TextView waitlistCount     = findViewById(R.id.waitlist_Count);

                    // Pre-fill name
                    if (event.getName() != null && !event.getName().equals("New Event")) {
                        nameInput.setText(event.getName());
                    }

                    // Pre-fill description
                    if (event.getDescription() != null && !event.getDescription().equals("Add a description...")) {
                        descriptionInput.setText(event.getDescription());
                    }

                    // Pre-fill details
                    if (event.getDetails() != null) {
                        detailsInput.setText(event.getDetails());
                    }

                    // Pre-fill closure date
                    if (event.getRegistrationEnd() != null) {
                        closureDateInput.setText(event.getRegistrationEnd());
                    }

                    // Pre-fill waitlist limit
                    if (event.getWaitlistLimit() != -1) {
                        waitlistLimitInput.setText(String.valueOf(event.getWaitlistLimit()));
                    } else {
                        waitlistLimitInput.setVisibility(View.GONE);
                        waitlistCount.setText("No Waitlist Limit");
                    }

                    // Pre-fill image if one was saved
                    if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                        com.bumptech.glide.Glide.with(CreateEventActivity.this)
                                .load(event.getImageUrl())
                                .into(eventImageView);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // Silently fail, fields stay as default
                }
            });
        }

        int editPhotoBtnId = isPrivate
                ? R.id.edit_photo_create_page_private
                : R.id.edit_photo_create_page;
        findViewById(editPhotoBtnId).setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        setupNavigation();
        setupInviteActions();
        setupDrawAction();

        int viewWaitlistBtnId = isPrivate
                ? R.id.view_waitlist_button_private
                : R.id.view_waitlist_button_public;

        View viewWaitlistBtn = findViewById(viewWaitlistBtnId);
        if (viewWaitlistBtn != null) {
            viewWaitlistBtn.setOnClickListener(v -> {
                if (existingEventId == null) {
                    Toast.makeText(this, "Please save the event first", Toast.LENGTH_SHORT).show();
                    return;
                }
                FragmentPreDrawWaitlist fragment = FragmentPreDrawWaitlist.newInstance(existingEventId);
                fragment.show(getSupportFragmentManager(), "FinalEntrantList");
            });
        }

        // Check if the button exists in the current layout before setting the listener
        View commentButton = findViewById(R.id.comment_button_entrant);
        if (commentButton != null) {
            commentButton.setOnClickListener(v -> {
                if (existingEventId == null) {
                    Toast.makeText(this, "Please save the event first", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(CreateEventActivity.this, CommentsActivity.class);
                intent.putExtra("eventId", existingEventId);
                startActivity(intent);
            });
        }

        // Get the correct create button ID based on layout
        int createBtnId = isPrivate
                ? R.id.confirm_changes_organizer_event_private
                : R.id.confirm_changes_organizer_event_public;

        findViewById(createBtnId).setVisibility(View.VISIBLE);

        // Create event button
        findViewById(createBtnId).setOnClickListener(v -> {
            EditText nameInput = findViewById(R.id.event_name);
            EditText descriptionInput = findViewById(R.id.event_description_main);
            EditText detailsInput = findViewById(R.id.event_details);
            EditText closureDateInput = findViewById(R.id.event_closure_date);
            EditText waitlistLimitInput = findViewById(R.id.waitlist_limit_input);

            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String details = detailsInput.getText().toString().trim();
            String closureDate = closureDateInput.getText().toString().trim();
            String waitlistLimitStr = waitlistLimitInput.getText().toString().trim();


            if (name.isEmpty() || description.isEmpty() || closureDate.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate that closure date is not in the past
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Date closureDateParsed = sdf.parse(closureDate);
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                java.util.Date today = cal.getTime();

                if (closureDateParsed != null && closureDateParsed.before(today)) {
                    Toast.makeText(this, "Event date cannot be in the past", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (java.text.ParseException e) {
                Toast.makeText(this, "Invalid date format. Please use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save the event (update existing or create new)
            proceedWithSave(name, details, description, closureDate, waitlistLimitStr, new ArrayList<>());
        });
    }

    private void setupInviteActions() {
        View privateInviteBtn = findViewById(R.id.invite_entrant_private);
        if (privateInviteBtn != null) {
            privateInviteBtn.setOnClickListener(v -> openInviteScreen(InviteEntrantActivity.INVITE_TYPE_WAITLIST));
        }

        View coOrganizerPublicBtn = findViewById(R.id.co_organizer_button_public);
        if (coOrganizerPublicBtn != null) {
            coOrganizerPublicBtn.setOnClickListener(v -> openInviteScreen(InviteEntrantActivity.INVITE_TYPE_COORGANIZER));
        }

        View coOrganizerPrivateBtn = findViewById(R.id.co_organizer_button_private);
        if (coOrganizerPrivateBtn != null) {
            coOrganizerPrivateBtn.setOnClickListener(v -> openInviteScreen(InviteEntrantActivity.INVITE_TYPE_COORGANIZER));
        }
    }

    private void openInviteScreen(String inviteType) {
        if (existingEventId == null || existingEventId.trim().isEmpty()) {
            Toast.makeText(this, "Save event details first", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, InviteEntrantActivity.class);
        intent.putExtra("eventId", existingEventId);
        intent.putExtra(InviteEntrantActivity.EXTRA_INVITE_TYPE, inviteType);
        startActivity(intent);
    }

    private void setupDrawAction() {
        int drawBtnId = isPrivate ? R.id.draw_waitlist_button_private : R.id.draw_waitlist_button_public;
        View drawButton = findViewById(drawBtnId);
        if (drawButton == null) return;

        drawButton.setOnClickListener(v -> {
            if (existingEventId == null || existingEventId.trim().isEmpty()) {
                Toast.makeText(this, "Please save the event first", Toast.LENGTH_SHORT).show();
                return;
            }

            showDrawDialog();
        });
    }

    private void showDrawDialog() {
        final EditText input = new EditText(this);
        input.setHint("How many entrants to draw?");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle("Draw Random Entrants")
                .setView(input)
                .setPositiveButton("Draw", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (TextUtils.isEmpty(value)) {
                        Toast.makeText(this, "Enter a number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int count;
                    try {
                        count = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (count <= 0) {
                        Toast.makeText(this, "Number must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    eventRepository.drawFromWaitlist(existingEventId, count, new EventRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(CreateEventActivity.this,
                                    "Random draw completed",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(CreateEventActivity.this,
                                    "Draw failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Sets up the navigation bar and exit button click listeners.
     * Uses different view IDs depending on whether the private or public layout is loaded.
     */
    private void setupNavigation() {
        int exitBtnId = isPrivate
                ? R.id.exit_button_create_page_private
                : R.id.exit_button_create_page;
        findViewById(exitBtnId).setOnClickListener(v -> finish());

        if (isPrivate) {
            findViewById(R.id.new_event_button_create_page_private).setOnClickListener(v -> {
                    EventTypeFragment fragment = new EventTypeFragment();
                    fragment.show(getSupportFragmentManager(), "NewEventFragment");
            });
            findViewById(R.id.home_button_create_page_private).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, HomePage.class)));
            findViewById(R.id.browse_button_create_page_private).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, BrowseEventsActivity.class)));
            findViewById(R.id.profile_button_create_page_private).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, Profile.class)));
        } else {
            findViewById(R.id.new_event_button_create_page).setOnClickListener(v -> {
                    EventTypeFragment fragment = new EventTypeFragment();
                    fragment.show(getSupportFragmentManager(), "NewEventFragment");
            });
            findViewById(R.id.qr_button_create_page).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, SearchScreen.class)));
            findViewById(R.id.home_button_create_page).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, HomePage.class)));
            findViewById(R.id.browse_button_create_page).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, BrowseEventsActivity.class)));
            findViewById(R.id.profile_button_create_page).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, Profile.class)));
        }
    }


    /**
     * Handles the final save — either updating an existing event or creating a new one,
     * with the selected tags applied.
     */
    private void proceedWithSave(String name, String details, String description, String closureDate,
                                 String waitlistLimitStr, List<String> selectedTags) {
        @SuppressLint("HardwareIds")
        String organizerId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        if (existingEventId != null) {
            eventRepository.fetchEventById(existingEventId, new EventRepository.EventCallback() {
                @Override
                public void onSuccess(Events event) {
                    event.setName(name);
                    event.setDetails(details);
                    event.setDescription(description);
                    event.setDate(closureDate);
                    event.setTags(selectedTags);
                    if (!waitlistLimitStr.isEmpty()) {
                        event.setWaitlistLimit(Integer.parseInt(waitlistLimitStr));
                    }
                    if (selectedImageUri != null) {
                        uploadImageAndUpdateEvent(event);
                    } else {
                        updateEventInFirestore(event);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(CreateEventActivity.this,
                            "Failed to load event: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Fallback: brand new event
            Events event = new Events(name, closureDate, description, "");
            event.setDetails(details);
            event.setOrganizerId(organizerId);
            event.setPrivate(isPrivate);
            event.setTags(selectedTags);
            if (!waitlistLimitStr.isEmpty()) {
                event.setWaitlistLimit(Integer.parseInt(waitlistLimitStr));
            }
            if (selectedImageUri != null) {
                uploadImageAndCreateEvent(event);
            } else {
                createEventInFirestore(event);
            }
        }
    }

    /**
     * Uploads the selected image to Firebase Storage, stores the download URL in the event,
     * and then creates the event in Firestore.
     *
     * @param event the event being created
     */
    private void uploadImageAndCreateEvent(Events event) {
        byte[] imageData = readBytesFromUri(selectedImageUri);
        if (imageData == null) {
            Toast.makeText(this, "Image upload failed: could not read image", Toast.LENGTH_LONG).show();
            return;
        }
        ensureAuthAndUpload(imageData, uri -> {
            event.setImageUrl(uri.toString());
            createEventInFirestore(event);
        });
    }

    private void uploadImageAndUpdateEvent(Events event) {
        byte[] imageData = readBytesFromUri(selectedImageUri);
        if (imageData == null) {
            Toast.makeText(this, "Image upload failed: could not read image", Toast.LENGTH_LONG).show();
            return;
        }
        ensureAuthAndUpload(imageData, uri -> {
            event.setImageUrl(uri.toString());
            updateEventInFirestore(event);
        });
    }

    private void ensureAuthAndUpload(byte[] imageData, com.google.android.gms.tasks.OnSuccessListener<Uri> onUrl) {
        Runnable doUpload = () -> {
            String fileName = "event_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageReference.child(fileName);
            imageRef.putBytes(imageData)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(onUrl)
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to get image URL: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show()))
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Image upload failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
        };

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            doUpload.run();
        } else {
            FirebaseAuth.getInstance().signInAnonymously()
                    .addOnSuccessListener(result -> doUpload.run())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Image upload failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
        }
    }

    private byte[] readBytesFromUri(Uri uri) {
        try (java.io.InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return null;
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
            return buffer.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Persists the event in Firestore and navigates back to the home page on success.
     *
     * @param event the event to create
     */
    private void createEventInFirestore(Events event) {
        eventRepository.createEvent(event, new EventRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(CreateEventActivity.this, "Event created!", Toast.LENGTH_SHORT).show();
                if (isPrivate) {
                    Toast.makeText(CreateEventActivity.this, "Private event created!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateEventActivity.this, HomePage.class));
                    finish();
                } else {
                    showQrDialog(event.getEventId());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CreateEventActivity.this,
                        "Failed to create event: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Updates the existing event in Firestore with the organizer's newly entered details,
     * and navigates back to the home page on success.
     *
     * @param event the event to update
     */
    private void updateEventInFirestore(Events event) {
        eventRepository.updateEvent(event, new EventRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(CreateEventActivity.this, "Event saved!", Toast.LENGTH_SHORT).show();
                if (isPrivate) {
                    Toast.makeText(CreateEventActivity.this, "Private event created!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateEventActivity.this, HomePage.class));
                    finish();
                } else if (!isFinishing()){
                    showQrDialog(event.getEventId());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CreateEventActivity.this,
                        "Failed to save event: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    //FOR QR CODE
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
                    .setPositiveButton("OK", (dialog, which) -> {
                        startActivity(new Intent(CreateEventActivity.this, HomePage.class));
                        finish();
                    })
                    .show();

        } catch (com.google.zxing.WriterException e) {
            android.widget.Toast.makeText(this, "Failed to generate QR code", android.widget.Toast.LENGTH_SHORT).show();
            startActivity(new Intent(CreateEventActivity.this, HomePage.class));
            finish();
        }
    }
}






