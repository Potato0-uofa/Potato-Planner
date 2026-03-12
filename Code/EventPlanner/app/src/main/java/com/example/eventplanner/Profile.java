package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity that displays the current user's profile information.
 * Loads the user's name, email, and phone from Firestore and populates
 * the profile page UI. Also provides navigation to the Settings screen
 * and the bottom taskbar.
 *
 * The information on the user's profile also has the ability to be updated
 * if the user so chooses.
 */
public class Profile extends AppCompatActivity {

    private final UserRepository userRepository = new UserRepository();

    /**
     * Initializes the activity, loads the user's profile data from Firestore,
     * and sets up navigation button click listeners.
     *
     * @param savedInstanceState the previously saved instance state (if applicable)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        loadUser();

        // Settings button
        findViewById(R.id.settings_button_profile).setOnClickListener(v ->
                startActivity(new Intent(Profile.this, SettingsView.class)));

        // Home bar navigation

        // Create Event Button
        findViewById(R.id.new_event_button_profile).setOnClickListener(v ->
                startActivity(new Intent(Profile.this, CreateEventActivity.class)));

        // Search (Spyglass) Button
        findViewById(R.id.search_button_profile).setOnClickListener(v ->
                startActivity(new Intent(Profile.this, SearchScreen.class)));

        // Home Button
        findViewById(R.id.home_button_profile).setOnClickListener(v ->
                startActivity(new Intent(Profile.this, HomePage.class)));

        // Browse Events Button
        findViewById(R.id.browse_button_profile).setOnClickListener(v ->
                startActivity(new Intent(Profile.this, NonAdminBrowseEvents.class)));

        // Profile View Button
        findViewById(R.id.profile_button_profile).setOnClickListener(v -> {
            // Already on profile, don't do anything
        });

        // View Event History Button
        findViewById(R.id.join_event_button).setOnClickListener(v ->
                startActivity(new Intent(Profile.this, EventHistoryActivity.class)));
    }

    /**
     * Loads the current user's data from Firestore using the device ID,
     * and populates the profile name, email, and phone TextViews.
     */
    private void loadUser() {
        String deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        userRepository.getUserByDeviceId(deviceId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    TextView tvName        = findViewById(R.id.profile_name);
                    TextView tvEmail       = findViewById(R.id.profile_email);
                    TextView tvPhone       = findViewById(R.id.profile_phone);
                    TextView tvDescription = findViewById(R.id.profile_user_description);
                    TextView tvAddress     = findViewById(R.id.profile_address);
                    TextView tvUsername = findViewById(R.id.profile_username);

                    tvName.setText(TextUtils.isEmpty(user.getName()) ? "No name set" : user.getName());
                    tvEmail.setText("Email: " + (TextUtils.isEmpty(user.getEmail()) ? "—" : user.getEmail()));
                    tvPhone.setText("Phone Number: " + (TextUtils.isEmpty(user.getPhone()) ? "—" : user.getPhone()));

                    // Description: show 'tap-to-edit' hint if empty
                    if (TextUtils.isEmpty(user.getDescription())) {
                        tvDescription.setText("Tap to add a description...");
                    } else {
                        tvDescription.setText(user.getDescription());
                    }

                    // Tap description to edit inline
                    tvDescription.setOnClickListener(v -> showEditDescriptionDialog(user));

                    // Address: show under contact info if set, hide if empty
                    if (TextUtils.isEmpty(user.getAddress())) {
                        tvAddress.setVisibility(android.view.View.GONE);
                    } else {
                        tvAddress.setVisibility(android.view.View.VISIBLE);
                        tvAddress.setText("Business Address: " + user.getAddress());
                    }

                    // Country: show under contact info if set, hide if empty
                    TextView tvCountry = findViewById(R.id.profile_country);
                    if (TextUtils.isEmpty(user.getCountry())) {
                        tvCountry.setVisibility(android.view.View.GONE);
                    } else {
                        tvCountry.setVisibility(android.view.View.VISIBLE);
                        tvCountry.setText("Country: " + user.getCountry());
                    }

                    // Username: Show under Name if set, hide if empty
                    if (TextUtils.isEmpty(user.getUsername())) {
                        tvUsername.setVisibility(android.view.View.GONE);
                    } else {
                        tvUsername.setVisibility(android.view.View.VISIBLE);
                        tvUsername.setText(user.getUsername());
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Show placeholder text on failure
            }
        });
    }

    private void showEditDescriptionDialog(User user) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setText(user.getDescription());
        input.setSelection(input.getText().length());

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Description")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newDesc = input.getText().toString().trim();
                    user.setDescription(newDesc);
                    userRepository.upsertUser(user, new UserRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            TextView tvDescription = findViewById(R.id.profile_user_description);
                            tvDescription.setText(TextUtils.isEmpty(newDesc) ? "Tap to add a description..." : newDesc);
                            android.widget.Toast.makeText(Profile.this, "Description saved", android.widget.Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            android.widget.Toast.makeText(Profile.this, "Failed to save description", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}