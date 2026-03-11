package com.example.eventplanner;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.awt.Insets;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bootstrapUser();
    }

    private void bootstrapUser() {
        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        userRepository.getUserByDeviceId(deviceId, new UserRepository.UserCallback() {

            @Override
            public void onSuccess(User user) {
                if (user == null) {
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
                    Log.d(TAG, "Existing user loaded: " + user.getDeviceId());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load user", e);
            }
        });
    }
}