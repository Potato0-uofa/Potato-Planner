package com.example.eventplanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
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
            FirebaseStorage.getInstance().getReference("event_images");

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
    private boolean isPrivate = false;

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

        // Load the correct layout based on public/private
        if (isPrivate) {
            setContentView(R.layout.activity_create_event_private_view);
        } else {
            setContentView(R.layout.activity_create_event_view);
        }

        eventImageView = findViewById(R.id.event_image_icon);

        findViewById(R.id.map_view_button).setOnClickListener(v -> {
            Intent intent = new Intent(CreateEventActivity.this, MapViewActivity.class);
            intent.putExtra("eventId", existingEventId);
            startActivity(intent);
        });

        // Pre-fill fields for the setup fragment
        if (existingEventId != null) {
            eventRepository.fetchEventById(existingEventId, new EventRepository.EventCallback() {
                @Override
                public void onSuccess(Events event) {
                    EditText closureDateInput = findViewById(R.id.event_closure_date);
                    EditText waitlistLimitInput = findViewById(R.id.waitlist_limit_input);
                    TextView waitlistCount = findViewById(R.id.waitlist_Count);

                    if (event.getRegistrationEnd() != null) {
                        closureDateInput.setText(event.getRegistrationEnd());
                    }

                    if (event.getWaitlistLimit() != -1) {
                        waitlistLimitInput.setText(String.valueOf(event.getWaitlistLimit()));
                    } else {
                        // No limit was set — hide the input and show a label instead
                        waitlistLimitInput.setVisibility(View.GONE);
                        waitlistCount.setText("No Waitlist Limit");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // Silently fail, fields just stay blank
                }
            });
        }

        findViewById(R.id.edit_photo_create_page).setOnClickListener(v ->
                        imagePickerLauncher.launch("image/*"));

        setupNavigation();

        // Get the correct create button ID based on layout
        int createBtnId = isPrivate
                ? R.id.create_button_create_page_private
                : R.id.create_button_create_page;

        // Create event button
        findViewById(createBtnId).setOnClickListener(v -> {
            EditText nameInput = findViewById(R.id.event_name);
            EditText descriptionInput = findViewById(R.id.event_description_main);
            EditText closureDateInput = findViewById(R.id.event_closure_date);
            EditText waitlistLimitInput = findViewById(R.id.waitlist_limit_input);

            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
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

            @SuppressLint("HardwareIds")
            String organizerId = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            if (existingEventId != null) {
                // Update the event already created by the setup fragment
                eventRepository.fetchEventById(existingEventId, new EventRepository.EventCallback() {
                    @Override
                    public void onSuccess(Events event) {
                        event.setName(name);
                        event.setDescription(description);
                        event.setDate(closureDate);
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
                // Fallback: create a brand new event (old flow)
                Events event = new Events(name, closureDate, description, "");
                event.setOrganizerId(organizerId);
                event.setPrivate(isPrivate);

                if (!waitlistLimitStr.isEmpty()) {
                    int limit = Integer.parseInt(waitlistLimitStr);
                    event.setWaitlistLimit(limit);
                }

                if (selectedImageUri != null) {
                    uploadImageAndCreateEvent(event);
                } else {
                    createEventInFirestore(event);
                }
            }
        });
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
                // Do nothing since already on new event page
            });
            findViewById(R.id.qr_button_create_page).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, SearchScreen.class)));
            findViewById(R.id.home_button_create_page_private).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, HomePage.class)));
            findViewById(R.id.browse_button_create_page_private).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, BrowseEventsActivity.class)));
            findViewById(R.id.profile_button_create_page_private).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, Profile.class)));
        } else {
            findViewById(R.id.new_event_button_create_page).setOnClickListener(v -> {
                // Do nothing since already on new event page
            });
            findViewById(R.id.qr_button_create_page).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, SearchScreen.class)));
            findViewById(R.id.home_button_create_page).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, HomePage.class)));
            findViewById(R.id.browse_button_create_page).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, NonAdminBrowseEvents.class)));
            findViewById(R.id.profile_button_create_page).setOnClickListener(v ->
                    startActivity(new Intent(CreateEventActivity.this, Profile.class)));
        }
    }

    /**
     * Uploads the selected image to Firebase Storage, stores the download URL in the event,
     * and then creates the event in Firestore.
     *
     * @param event the event being created
     */
    private void uploadImageAndCreateEvent(Events event) {
        String fileName = "event_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageReference.child(fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            event.setImageUrl(uri.toString());
                            createEventInFirestore(event);
                        }).addOnFailureListener(e ->
                                Toast.makeText(CreateEventActivity.this,
                                        "Failed to get image URL: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(CreateEventActivity.this,
                                "Image upload failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    /**
     * Uploads the selected image to Firebase Storage, stores the download URL in the event,
     * and then updates the existing event in Firestore.
     *
     * @param event the event being updated
     */
    private void uploadImageAndUpdateEvent(Events event) {
        String fileName = "event_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageReference.child(fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            event.setImageUrl(uri.toString());
                            updateEventInFirestore(event);
                        }).addOnFailureListener(e ->
                                Toast.makeText(CreateEventActivity.this,
                                        "Failed to get image URL: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(CreateEventActivity.this,
                                "Image upload failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
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
                startActivity(new Intent(CreateEventActivity.this, HomePage.class));
                finish();
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
                startActivity(new Intent(CreateEventActivity.this, HomePage.class));
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CreateEventActivity.this,
                        "Failed to save event: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}