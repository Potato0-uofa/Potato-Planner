package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
<<<<<<< HEAD
import android.widget.TextView;
=======
import android.provider.Settings;
import android.util.Log;
>>>>>>> 96cddd1fedc38798d82cde7969010cadd0a08ee7

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
<<<<<<< HEAD
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_login), (v, insets) -> {
=======
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
>>>>>>> 96cddd1fedc38798d82cde7969010cadd0a08ee7
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

<<<<<<< HEAD

        TextView txtSignUp = findViewById(R.id.txt_sign_up);
        txtSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileSetup.class);
            startActivity(intent);
=======
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
>>>>>>> 96cddd1fedc38798d82cde7969010cadd0a08ee7
        });
    }
}