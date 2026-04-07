package com.example.eventplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Main home screen with navigation bar and upcoming event notifications. */
public class HomePage extends AppCompatActivity {

    private final UserRepository userRepository = new UserRepository();
    private final EventRepository eventRepository = new EventRepository();
    private ImageButton notificationButton;
    private ListenerRegistration registrationListener;
    private String deviceId;
    private boolean userNotificationsEnabled = true;

    private ViewPager2 carouselViewPager;
    private LinearLayout carouselDots;
    private TextView carouselEmptyText;
    private EventCarouselAdapter carouselAdapter;
    private final List<Events> carouselEvents = new ArrayList<>();

    private int savedCarouselPosition = 0;

    private static final long AUTO_SCROLL_DELAY_MS = 7000;
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoScrollRunnable = this::autoScrollNext;

    private static final String PREFS_SYSTEM_NOTIFICATIONS = "system_notification_seen";
    private static final Set<String> SYSTEM_NOTIFY_STATUSES = new HashSet<>(Arrays.asList(
            "invited",
            "private_waitlist_invited",
            "coorganizer_invited",
            "accepted",
            "declined",
            "cancelled",
            "waitlisted"
    ));

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this,
                            "System notifications are disabled. You can enable them in settings.",
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        notificationButton = findViewById(R.id.notification_button_home);
        notificationButton.setVisibility(View.VISIBLE);
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        NotificationHelper.ensureChannel(this);
        NotificationHelper.startGlobalListener(this);
        ensureNotificationPermission();

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
            startActivity(new Intent(HomePage.this, InvitationsActivity.class));
        });

        setupCarousel();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Any touch resets the auto-scroll timer
        resetAutoScrollTimer();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset state when coming back to the page
        notificationButton.setVisibility(View.VISIBLE);
        checkAndScheduleNotification();
        loadCarouselEvents();
        resetAutoScrollTimer();
    }

    private void setupCarousel() {
        carouselViewPager = findViewById(R.id.carousel_viewpager);
        carouselDots = findViewById(R.id.carousel_dots);
        carouselEmptyText = findViewById(R.id.carousel_empty_text);

        carouselAdapter = new EventCarouselAdapter(carouselEvents);
        carouselViewPager.setAdapter(carouselAdapter);

        // Show peek of neighboring pages
        carouselViewPager.setOffscreenPageLimit(3);
        carouselViewPager.setClipToPadding(false);
        carouselViewPager.setClipChildren(false);

        // Page transformer for depth/scale transition effect
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(32));
        transformer.addTransformer((page, position) -> {
            float absPos = Math.abs(position);
            // Scale neighboring pages down slightly
            page.setScaleY(1f - (0.1f * absPos));
            // Fade neighboring pages slightly
            page.setAlpha(1f - (0.3f * absPos));
        });
        carouselViewPager.setPageTransformer(transformer);

        // Dot indicator callback
        carouselViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDotIndicators(position);
            }
        });
    }

    private void loadCarouselEvents() {
        eventRepository.fetchOpenEvents(new EventRepository.EventsCallback() {
            @Override
            public void onSuccess(List<Events> events) {
                carouselEvents.clear();
                for (Events event : events) {
                    if (!event.isPrivate()
                            && !deviceId.equals(event.getOrganizerId())
                            && (event.getCoOrganizerIds() == null
                                || !event.getCoOrganizerIds().contains(deviceId))) {
                        carouselEvents.add(event);
                    }
                }
                carouselAdapter.notifyDataSetChanged();

                if (carouselEvents.isEmpty()) {
                    carouselViewPager.setVisibility(View.GONE);
                    carouselDots.setVisibility(View.GONE);
                    carouselEmptyText.setVisibility(View.VISIBLE);
                } else {
                    carouselViewPager.setVisibility(View.VISIBLE);
                    carouselDots.setVisibility(View.VISIBLE);
                    carouselEmptyText.setVisibility(View.GONE);
                    buildDotIndicators(carouselEvents.size());
                    int restorePos = Math.min(savedCarouselPosition, carouselEvents.size() - 1);
                    // Post to next frame so ViewPager2 has processed the adapter update
                    carouselViewPager.post(() -> {
                        carouselViewPager.setCurrentItem(restorePos, false);
                        updateDotIndicators(restorePos);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                carouselViewPager.setVisibility(View.GONE);
                carouselDots.setVisibility(View.GONE);
                carouselEmptyText.setVisibility(View.VISIBLE);
                carouselEmptyText.setText("No Events Available");
            }
        });
    }

    private void buildDotIndicators(int count) {
        carouselDots.removeAllViews();
        // Cap visible dots at 7 to keep it clean
        int dotCount = Math.min(count, 7);
        for (int i = 0; i < dotCount; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(R.drawable.dot_indicator_inactive);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            carouselDots.addView(dot);
        }
    }

    private void updateDotIndicators(int selectedPosition) {
        int dotCount = carouselDots.getChildCount();
        if (dotCount == 0) return;
        int activeDot = selectedPosition % dotCount;
        for (int i = 0; i < dotCount; i++) {
            ImageView dot = (ImageView) carouselDots.getChildAt(i);
            dot.setImageResource(i == activeDot
                    ? R.drawable.dot_indicator_active
                    : R.drawable.dot_indicator_inactive);
        }
    }

    private void resetAutoScrollTimer() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
        if (!carouselEvents.isEmpty()) {
            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY_MS);
        }
    }

    private void autoScrollNext() {
        if (carouselEvents.isEmpty()) return;
        int next = carouselViewPager.getCurrentItem() + 1;
        if (next >= carouselEvents.size()) {
            next = 0;
        }
        carouselViewPager.setCurrentItem(next, true);
        // Schedule the next auto-scroll
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY_MS);
    }

    private void checkAndScheduleNotification() {
        userRepository.getUserByDeviceId(deviceId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    userNotificationsEnabled = user.isNotificationsEnabled();
                } else {
                    // Default to enabled when user profile is not initialized.
                    userNotificationsEnabled = true;
                }

                if (userNotificationsEnabled) {
                    NotificationHelper.ensureChannel(HomePage.this);
                } else {
                    NotificationManagerCompat.from(HomePage.this).cancelAll();
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Keep default behavior on transient profile lookup failures.
                userNotificationsEnabled = true;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        savedCarouselPosition = carouselViewPager.getCurrentItem();
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    // Heads-up notifications are now managed globally by NotificationHelper.
}
