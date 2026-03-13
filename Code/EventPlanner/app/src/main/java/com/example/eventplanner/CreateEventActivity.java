package com.example.eventplanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Activity for creating a new event in the app. Provides an organizer with an interface to input
 * an event's name, description, date, and a waitlist limit. Also supports selecting and uploading
 * an event image. When successfully created the event is persisted using the eventRepository and
 * the user is directed back to the home page. The view also has a navigation bar at the bottom.
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
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event_view);

        eventImageView = findViewById(R.id.event_image_icon);

        findViewById(R.id.edit_photo_create_page).setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        // Home bar navigation
        findViewById(R.id.new_event_button_create_page).setOnClickListener(v -> {
            // Do nothing since already on new event page
        });

        findViewById(R.id.search_button_create_page).setOnClickListener(v -> {
            startActivity(new Intent(CreateEventActivity.this, SearchScreen.class));
        });

        findViewById(R.id.home_button_create_page).setOnClickListener(v -> {
            startActivity(new Intent(CreateEventActivity.this, HomePage.class));
        });

        findViewById(R.id.browse_button_create_page).setOnClickListener(v -> {
            startActivity(new Intent(CreateEventActivity.this, NonAdminBrowseEvents.class));
        });

        findViewById(R.id.profile_button_create_page).setOnClickListener(v -> {
            startActivity(new Intent(CreateEventActivity.this, Profile.class));
        });

        // Create event button
        findViewById(R.id.create_button_create_page).setOnClickListener(v -> {
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

            @SuppressLint("HardwareIds")
            String organizerId = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            Events event = new Events(name, closureDate, description, "");
            event.setOrganizerId(organizerId);

            if (!waitlistLimitStr.isEmpty()) {
                int limit = Integer.parseInt(waitlistLimitStr);
                event.setWaitlistLimit(limit);
            }

            if (selectedImageUri != null) {
                uploadImageAndCreateEvent(event);
            } else {
                createEventInFirestore(event);
            }
        });
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
}