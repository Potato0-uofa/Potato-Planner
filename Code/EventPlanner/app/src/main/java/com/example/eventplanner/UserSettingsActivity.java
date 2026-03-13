package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class UserSettingsActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private String deviceId;
    private TextView tvName, tvUsername, tvEmail, tvPhone, tvCountry, tvAddress;
    private SwitchCompat switchNotifications;
    private User currentUser;
    private boolean isUpdatingUI = false; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        userRepository = new UserRepository();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Bind Views
        tvName = findViewById(R.id.tv_name_value);
        tvUsername = findViewById(R.id.tv_username_value);
        tvEmail = findViewById(R.id.tv_email_value);
        tvPhone = findViewById(R.id.tv_phone_value);
        tvCountry = findViewById(R.id.tv_country_value);
        tvAddress = findViewById(R.id.tv_address_value);

        switchNotifications = findViewById(R.id.switch_notifications);

        // Load data and set switch state from Firestore
        loadUserData();

        // Listener for single master switch
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> syncPreferences());

        // DELETE PROFILE Logic (US 01.02.04)
        Button btnDeleteProfile = findViewById(R.id.btn_delete_profile);
        btnDeleteProfile.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Profile")
                    .setMessage("Permanently delete your profile?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        userRepository.deleteUser(deviceId, new UserRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(UserSettingsActivity.this, "Profile Deleted", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(UserSettingsActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(UserSettingsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        userRepository.getUserByDeviceId(deviceId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    currentUser = user;
                    
                    tvName.setText(TextUtils.isEmpty(user.getName()) ? "—" : user.getName());
                    tvUsername.setText(TextUtils.isEmpty(user.getUsername()) ? "—" : user.getUsername());
                    tvEmail.setText(TextUtils.isEmpty(user.getEmail()) ? "—" : user.getEmail());
                    tvPhone.setText(TextUtils.isEmpty(user.getPhone()) ? "—" : user.getPhone());
                    tvCountry.setText(TextUtils.isEmpty(user.getCountry()) ? "—" : user.getCountry());
                    tvAddress.setText(TextUtils.isEmpty(user.getAddress()) ? "—" : user.getAddress());

                    // Set master switch state from User object
                    isUpdatingUI = true;
                    switchNotifications.setChecked(user.isNotificationsEnabled());
                    isUpdatingUI = false;
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserSettingsActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncPreferences() {
        if (currentUser == null || isUpdatingUI) return;

        // Update single master field
        currentUser.setNotificationsEnabled(switchNotifications.isChecked());

        userRepository.upsertUser(currentUser, new UserRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                // Success
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserSettingsActivity.this, "Failed to sync preferences", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
