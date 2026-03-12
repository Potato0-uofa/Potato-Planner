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
        findViewById(R.id.new_event_button_profile).setOnClickListener(v ->
                startActivity(new Intent(Profile.this, CreateEventActivity.class)));

        findViewById(R.id.search_button_profile).setOnClickListener(v ->
                startActivity(new Intent(Profile.this, SearchScreen.class)));

        findViewById(R.id.home_button_profile).setOnClickListener(v ->
                startActivity(new Intent(Profile.this, HomePage.class)));

        findViewById(R.id.browse_button_profile).setOnClickListener(v ->
                startActivity(new Intent(Profile.this, NonAdminBrowseEvents.class)));

        findViewById(R.id.profile_button_profile).setOnClickListener(v -> {
            // Already on profile, don't do anything
        });
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
                    TextView tvName  = findViewById(R.id.profile_name);
                    TextView tvEmail = findViewById(R.id.profile_email);
                    TextView tvPhone = findViewById(R.id.profile_phone);

                    tvName.setText(TextUtils.isEmpty(user.getName()) ? "No name set" : user.getName());
                    tvEmail.setText("Email: " + (TextUtils.isEmpty(user.getEmail()) ? "—" : user.getEmail()));
                    tvPhone.setText("Phone Number: " + (TextUtils.isEmpty(user.getPhone()) ? "—" : user.getPhone()));
                }
            }

            @Override
            public void onFailure(Exception e) {
                // If it fails, just show placeholder text
            }
        });
    }
}