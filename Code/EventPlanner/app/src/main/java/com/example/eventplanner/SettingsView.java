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

/**
 * Activity that displays and allows editing of the user's profile settings.
 * Loads the current user's data from Firestore and populates the name, email,
 * phone, and country fields. Each field has an "Edit" button that opens a
 * dialog for inline editing with validation before saving back to Firestore.
 */
public class SettingsView extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone, tvCountry;
    private final UserRepository userRepository = new UserRepository();
    private User currentUser;

    /**
     * Initializes the activity, loads the current user from Firestore,
     * and sets up Edit button click listeners for each field.
     *
     * @param savedInstanceState the previously saved instance state (if applicable)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        tvName    = findViewById(R.id.tv_name_value);
        tvEmail   = findViewById(R.id.tv_email_value);
        tvPhone   = findViewById(R.id.tv_phone_value);
        tvCountry = findViewById(R.id.tv_country_value);

        // Close button —> Go back to the User Profile View
        findViewById(R.id.btn_close).setOnClickListener(v ->
                startActivity(new Intent(SettingsView.this, Profile.class)));

        loadUser();

        // Edit button listeners
        findViewById(R.id.btn_edit_name).setOnClickListener(v ->
                showEditDialog("Name", tvName.getText().toString(), InputType.TYPE_CLASS_TEXT, value -> {
                    if (TextUtils.isEmpty(value)) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
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
                    currentUser.setPhone(value); // stored in User; adapt if User gets a country field
                    saveUser(() -> tvCountry.setText(TextUtils.isEmpty(value) ? "—" : value));
                }));
    }

    /**
     * Loads the current user's data from Firestore using the device ID,
     * and populates the UI fields with their saved information.
     */
    private void loadUser() {
        String deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        userRepository.getUserByDeviceId(deviceId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    currentUser = user;
                    tvName.setText(TextUtils.isEmpty(user.getName()) ? "—" : user.getName());
                    tvEmail.setText(TextUtils.isEmpty(user.getEmail()) ? "—" : user.getEmail());
                    tvPhone.setText(TextUtils.isEmpty(user.getPhone()) ? "—" : user.getPhone());
                    tvCountry.setText("—"); // Placeholder, will implement when priorities allow
                                            // country was not a necessary field to fill out
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SettingsView.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves the current user object to Firestore and runs a UI update on success.
     *
     * @param onSuccess a {@link Runnable} that updates the UI after a successful save
     */
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

    /**
     * Shows an {@link AlertDialog} with an {@link EditText} pre-filled with the current value,
     * allowing the user to edit a single field. Calls the provided callback with the new value
     * when the user confirms.
     *
     * @param fieldName   the label shown in the dialog title
     * @param currentValue the current value to pre-fill in the input
     * @param inputType   the {@link InputType} for the EditText
     * @param onConfirm   callback invoked with the new trimmed value on confirmation
     */
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

    /**
     * Callback interface used to return a confirmed value from the edit dialog.
     */
    interface OnConfirmListener {
        /**
         * Called when the user confirms their edits for their chosen info.
         *
         * @param value the trimmed string value entered by the user
         */
        void onConfirm(String value);
    }
}