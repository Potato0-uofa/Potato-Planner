package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.core.graphics.Insets;
import android.text.TextUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // DEMONSTRATION SETUP: Redirect to EventWaitlistActivity immediately
        Intent intent = new Intent(this, EventWaitlistActivity.class);
        startActivity(intent);

        /*
        //startActivity(new Intent(this, AdminBrowseEventsActivity.class));
        //finish();
        //IF YOU WANT TO GO TO ADMIN PAGE UNCOMMENT

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

        bootstrapUser();
        */
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

                } else if (!TextUtils.isEmpty(user.getName()) && !TextUtils.isEmpty(user.getEmail())) {
                    // If they already have a profile, then just skip to the homepage
                    Intent intent = new Intent(MainActivity.this, HomePage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load user", e);
            }
        });
    }
}