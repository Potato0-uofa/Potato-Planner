package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Objects;

/**
 * Activity that displays the details of a specified event and allows the user to join the waitlist
 * Recieves the event data via Intent extrs, renders them in the UI and attaches  real time firestore
 * listener to keep the displayed waitlist count in sync.
 */
public class EventDescriptionView extends AppCompatActivity {

    /**
     * Repository used to read event data and mutate the waiting list in Firestore.
     * */
    private EventRepository eventRepository;
    /**
     * Local representation of the waiting list, used to cache and display the current count.
     * */
    private WaitingList waitingList;
    /**
     * Active Firestore snapshot listener for the waitlist count.
     * Held so it can be detached in {@link #onDestroy()} to prevent memory leaks.
     */
    private ListenerRegistration waitlistListener;
    /**
     * TextView that displays the live waitlist headcount.
     * */
    private TextView tvWaitlistCount;
    /**
     * The device's {@code ANDROID_ID}, used as the entrant identifier.
     * */
    private String eventId;

    /**
     * Initializes the event activity
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        eventRepository = new EventRepository();
        waitingList = new WaitingList();
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            eventId = "test_event_1"; // Default for demo
        }

        String eventName = getIntent().getStringExtra("eventName");
        String eventDescription = getIntent().getStringExtra("eventDescription");

        // Set the data to your views
        tvWaitlistCount = findViewById(R.id.Waitlist_Count);
        ((TextView) findViewById(R.id.event_name)).setText(eventName != null ? eventName : "Test Event");
        ((TextView) findViewById(R.id.event_details)).setText(eventDescription != null ? eventDescription : "Test Description");

        Button btnJoinEvent = findViewById(R.id.join_event_button);
        btnJoinEvent.setOnClickListener(v -> {
            eventRepository.joinWaitingList(eventId, userId, new EventRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(EventDescriptionView.this, "Joined Waitlist!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(EventDescriptionView.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        setupNavigation();
        startListeningToWaitlist();
    }


    /**
     *  Attaches a real-time Firestore listener that updates {@link #tvWaitlistCount}
     */
    private void startListeningToWaitlist() {
        waitlistListener = eventRepository.listenToWaitlistCount(eventId, new EventRepository.CountCallback() {
            @Override
            public void onSuccess(int count) {
                waitingList.setCloudCount(count);
                tvWaitlistCount.setText("Waitlist: " + waitingList.getCount() + " people");
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDescriptionView.this, "Error loading waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Configures click listeners for the bottom navigation bar and the exit button.
     */
    private void setupNavigation() {
        findViewById(R.id.home_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, HomePage.class));
        });

        findViewById(R.id.search_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, SearchScreen.class));
        });

        findViewById(R.id.browse_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, NonAdminBrowseEvents.class));
        });

        findViewById(R.id.profile_button_event_page).setOnClickListener(v -> {
            startActivity(new Intent(EventDescriptionView.this, Profile.class));
        });

        findViewById(R.id.exit_button_event_page).setOnClickListener(v -> finish());
    }

    /**
     * Life style callback invoked when the activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitlistListener != null) {
            waitlistListener.remove();
        }
    }
}