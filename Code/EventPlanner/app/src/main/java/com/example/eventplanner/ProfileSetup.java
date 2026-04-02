package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity: New user sets up their profile
 * Prompts user for their name, email, and phone number.
 * Validates the inputted information.
 * The user's profile is saved onto Firestore, and they are redirected to the homepage afterwards.
 */
public class ProfileSetup extends AppCompatActivity {

    // Attributes
    private EditText editName, editEmail, editPhone;
    private final UserRepository userRepository = new UserRepository();

    /**
     * Initializes the activity and sets up the layout.
     * Also attaches a click listener to the confirm button
     * @param savedInstanceState the previously saved instance state (if applicable)
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        editName  = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        Button btnConfirm = findViewById(R.id.btn_save_profile);

        btnConfirm.setOnClickListener(v -> validateAndSave());
    }

    /**
     * Validates the user's input and saves the profile to Firestore.
     *
     * Validation rules:
     *
     * Name is required
     * Email is required and must be a valid email format
     * Phone is required, and must be between 10 to 15 digits. A leading + is optional
     *
     * On success, navigates to the app homepage.
     * On failure, should display a toast error message.
     */
    private void validateAndSave() {
        String name  = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        boolean valid = true;

        // If name prompt is empty, state that a name is required
        // If the name does not consist of a First and Last name, prompt user to enter them
        if (TextUtils.isEmpty(name)) {
            editName.setError("Name is required!");
            valid = false;
        } else if (!name.trim().contains(" ")) {
            editName.setError("Please enter your first and last name");
            valid = false;
        }

        // If the email prompt is empty, state that an email is required
        // If the email address format is incorrect, state that a valid email address be entered
        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Email is required!");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Enter a valid email address");
            valid = false;
        }

        // (If user chooses to): If the phone number is invalid, state that it must be valid
        // (10-15 digits, + is optional)
        if (!TextUtils.isEmpty(phone) && !phone.matches("^\\+?[0-9]{10,15}$")) {
            editPhone.setError("Enter a valid phone number (10-15 digits)");
            valid = false;
        }

        if (!valid) return;

        String deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        User user = new User(deviceId, name, email, phone);

        if (email.equalsIgnoreCase("ibaldo@ualberta.ca")) { //WHERE TO SET USERS to admin. JUST USING MINE FOR NOW.
            user.setAdmin(true);
        }


        userRepository.upsertUser(user, new UserRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent(ProfileSetup.this, HomePage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ProfileSetup.this,
                        "Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}