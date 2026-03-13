package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity that provides profile management settings for the current user.
 */
public class ProfileSettingsActivity extends AppCompatActivity {

    /** Repository used to perform operations on user documents in Firestore. */
    private UserRepository userRepository;

    /** The device's ID, used as the unique identifier for the current user. */
    private String deviceId;

    /**
     * Initializes the activity, retrieves the device ID, and wires up the delete profile button.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        userRepository = new UserRepository();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Button btnDeleteProfile = findViewById(R.id.btn_delete_profile);
        btnDeleteProfile.setOnClickListener(v -> {
            userRepository.deleteUser(deviceId, new UserRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ProfileSettingsActivity.this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                    
                    // Restart MainActivity with FLAG_ACTIVITY_CLEAR_TASK to trigger bootstrap re-check
                    Intent intent = new Intent(ProfileSettingsActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ProfileSettingsActivity.this, "Failed to delete profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}