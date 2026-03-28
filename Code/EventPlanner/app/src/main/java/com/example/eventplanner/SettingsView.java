package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

/**
 * Activity that displays and allows editing of the user's profile settings.
 * Loads the current user's data from Firestore and populates the fields.
 * Includes a master notification toggle.
 */
public class SettingsView extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone, tvCountry, tvAddress, tvUsername;
    private SwitchCompat switchNotifications;
    private final UserRepository userRepository = new UserRepository();
    private User currentUser;
    private boolean isUpdatingUI = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        tvName    = findViewById(R.id.tv_name_value);
        tvEmail   = findViewById(R.id.tv_email_value);
        tvPhone   = findViewById(R.id.tv_phone_value);
        tvCountry = findViewById(R.id.tv_country_value);
        tvUsername = findViewById(R.id.tv_username_value);
        tvAddress = findViewById(R.id.tv_address_value);
        switchNotifications = findViewById(R.id.switch_notifications);

        // Close button -> Go back to the User Profile View
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        loadUser();

        // Notification toggle listener
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentUser != null && !isUpdatingUI) {
                currentUser.setNotificationsEnabled(isChecked);
                saveUser(() -> {}); // Save preference to Firestore
            }
        });

        // Edit button listeners
        findViewById(R.id.btn_edit_name).setOnClickListener(v ->
                showEditDialog("Name", tvName.getText().toString(), InputType.TYPE_CLASS_TEXT, value -> {
                    if (TextUtils.isEmpty(value)) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!value.trim().contains(" ")) {
                        Toast.makeText(this, "Please enter your first and last name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentUser.setName(value);
                    saveUser(() -> tvName.setText(value));
                }));

        findViewById(R.id.btn_edit_email).setOnClickListener(v ->
                showEditDialog("Email", tvEmail.getText().toString(), InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, value -> {
                    if (TextUtils.isEmpty(value)) {
                        Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                        Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentUser.setEmail(value);
                    saveUser(() -> tvEmail.setText(value));
                }));

        findViewById(R.id.btn_edit_phone).setOnClickListener(v ->
                showEditDialog("Phone Number", tvPhone.getText().toString(), InputType.TYPE_CLASS_PHONE, value -> {
                    if (!TextUtils.isEmpty(value) && !value.matches("^\\+?[0-9]{10,15}$")) {
                        Toast.makeText(this, "Enter a valid phone number (10-15 digits)", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentUser.setPhone(value);
                    saveUser(() -> tvPhone.setText(TextUtils.isEmpty(value) ? "—" : value));
                }));

        findViewById(R.id.btn_edit_country).setOnClickListener(v ->
                showEditDialog("Country", tvCountry.getText().toString(), InputType.TYPE_CLASS_TEXT, value -> {
                    currentUser.setCountry(value);
                    saveUser(() -> tvCountry.setText(TextUtils.isEmpty(value) ? "—" : value));
                }));

        findViewById(R.id.btn_edit_address).setOnClickListener(v ->
                showEditDialog("Business Address", tvAddress.getText().toString(), InputType.TYPE_CLASS_TEXT, value -> {
                    currentUser.setAddress(value);
                    saveUser(() -> tvAddress.setText(TextUtils.isEmpty(value) ? "—" : value));
                }));

        findViewById(R.id.btn_edit_username).setOnClickListener(v ->
                showEditDialog("Username", tvUsername.getText().toString(), InputType.TYPE_CLASS_TEXT, value -> {
                    currentUser.setUsername(value);
                    saveUser(() -> tvUsername.setText(TextUtils.isEmpty(value) ? "—" : value));
                }));


        // Delete Profile Button
        findViewById(R.id.btn_delete_profile).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (currentUser != null) {
                            userRepository.deleteUser(currentUser.getDeviceId(), new UserRepository.SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(SettingsView.this, "Profile deleted", Toast.LENGTH_SHORT).show();
                                    // Navigate to MainActivity to "reset" the app state (will trigger bootstrap)
                                    Intent intent = new Intent(SettingsView.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(SettingsView.this, "Failed to delete profile", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        //SIGN OUT BUTTON
        findViewById(R.id.btn_sign_out).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Sign Out", (dialog, which) -> {
                        Intent intent = new Intent(SettingsView.this, MainActivity.class);
                        intent.putExtra("signed_out", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadUser() {
        String deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        userRepository.getUserByDeviceId(deviceId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    currentUser = user;
                    isUpdatingUI = true;
                    tvName.setText(TextUtils.isEmpty(user.getName()) ? "—" : user.getName());
                    tvEmail.setText(TextUtils.isEmpty(user.getEmail()) ? "—" : user.getEmail());
                    tvPhone.setText(TextUtils.isEmpty(user.getPhone()) ? "—" : user.getPhone());
                    tvCountry.setText(TextUtils.isEmpty(user.getCountry()) ? "—" : user.getCountry());
                    tvAddress.setText(TextUtils.isEmpty(user.getAddress()) ? "—" : user.getAddress());
                    tvUsername.setText(TextUtils.isEmpty(user.getUsername()) ? "—" : user.getUsername());

                    // Set switch state WITHOUT triggering the listener
                    switchNotifications.setChecked(user.isNotificationsEnabled());
                    isUpdatingUI = false;
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SettingsView.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUser(Runnable onSuccess) {
        userRepository.upsertUser(currentUser, new UserRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                onSuccess.run();
                Toast.makeText(SettingsView.this, "Saved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SettingsView.this, "Failed to save changes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(String fieldName, String currentValue, int inputType, OnConfirmListener onConfirm) {
        EditText input = new EditText(this);
        input.setInputType(inputType);
        input.setText(currentValue.equals("—") ? "" : currentValue);
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Edit " + fieldName)
                .setView(input)
                .setPositiveButton("Save", (dialog, which) ->
                        onConfirm.onConfirm(input.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    interface OnConfirmListener {
        void onConfirm(String value);
    }
}