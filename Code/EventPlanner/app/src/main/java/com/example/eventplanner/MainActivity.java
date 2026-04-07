package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Entry point of the app. Checks if the user has a profile and routes to setup or home. */
public class MainActivity extends AppCompatActivity {

    /** Log tag for this activity. */
    private static final String TAG = "MainActivity";

    /** Repository for checking and creating user profiles. */
    private final UserRepository userRepository = new UserRepository();

    /**
     * Initializes the login screen and checks if the user already has a profile,
     * redirecting to the home page if so.
     *
     * @param savedInstanceState previously saved activity state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        NotificationHelper.startGlobalListener(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView txtSignUp = findViewById(R.id.txt_sign_up);
        txtSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileSetup.class);
            startActivity(intent);
        });

        boolean signedOut = getIntent().getBooleanExtra("signed_out", false);
        if (!signedOut) {
            bootstrapUser();
        }

    }

    /**
     * Checks if a user profile exists for the current device ID. If a complete
     * profile is found, redirects to the home page. Otherwise, creates a blank
     * user record or stays on the login screen for incomplete profiles.
     */
    private void bootstrapUser() {
        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        userRepository.getUserByDeviceId(deviceId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    // Only create a new user if one doesn't exist at all
                    User newUser = new User(deviceId, "", "", "");
                    userRepository.upsertUser(newUser, new UserRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "User bootstrap complete");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "User bootstrap failed", e);
                        }
                    });
                } else {
                    // User exists. Check if they have completed their profile.
                    if (!TextUtils.isEmpty(user.getName()) && !TextUtils.isEmpty(user.getEmail())) {
                        Log.d(TAG, "Existing user found with complete profile. Redirecting to HomePage.");
                        Intent intent = new Intent(MainActivity.this, HomePage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d(TAG, "Existing user found but profile is incomplete.");
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load user", e);
            }
        });
    }
}
