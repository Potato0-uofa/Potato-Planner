package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class UserSettingsActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        userRepository = new UserRepository();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Button btnDeleteProfile = findViewById(R.id.btn_delete_profile);
        btnDeleteProfile.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Profile")
                    .setMessage("Delete Profile Permanently?")
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        userRepository.deleteUser(deviceId, new UserRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(UserSettingsActivity.this, "Profile Deleted", Toast.LENGTH_SHORT).show();
                                
                                // Restart app to trigger bootstrap logic
                                Intent intent = new Intent(UserSettingsActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(UserSettingsActivity.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Close button listener (if applicable to the layout)
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
    }
}