package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class HomePage extends AppCompatActivity {

    private final UserRepository userRepository = new UserRepository();
    private ImageButton notificationButton;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable notificationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        notificationButton = findViewById(R.id.notification_button_home);
        notificationButton.setVisibility(View.GONE);

        findViewById(R.id.new_event_button_home).setOnClickListener(v -> {
            EventTypeFragment fragment = new EventTypeFragment();
            fragment.show(getSupportFragmentManager(), "NewEventFragment");
        });

        findViewById(R.id.qr_button_home).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, SearchScreen.class));
        });

        findViewById(R.id.home_button_home).setOnClickListener(v -> {
            // Do nothing since already on home page
        });

        findViewById(R.id.browse_button_home).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, BrowseEventsActivity.class));
        });

        findViewById(R.id.profile_button_home).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, Profile.class));
        });

        notificationButton.setOnClickListener(v -> {
            notificationButton.setVisibility(View.GONE);
            startActivity(new Intent(HomePage.this, NotificationLogs.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset state when coming back to the page
        notificationButton.setVisibility(View.GONE);
        if (notificationRunnable != null) {
            handler.removeCallbacks(notificationRunnable);
        }
        checkAndScheduleNotification();
    }

    private void checkAndScheduleNotification() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        userRepository.getUserByDeviceId(deviceId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    if (user.isNotificationsEnabled()) {
                        // Schedule notification appearance in 5 seconds (faster for demo)
                        notificationRunnable = () -> {
                            if (!isFinishing()) {
                                notificationButton.setVisibility(View.VISIBLE);
                                Snackbar.make(findViewById(R.id.homepage), "Demo: New Notification Received!", Snackbar.LENGTH_LONG).show();
                            }
                        };
                        handler.postDelayed(notificationRunnable, 5000);
                    } else {
                        Toast.makeText(HomePage.this, "Notifications are disabled in settings", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Ignore or log error
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (notificationRunnable != null) {
            handler.removeCallbacks(notificationRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && notificationRunnable != null) {
            handler.removeCallbacks(notificationRunnable);
        }
    }
}
