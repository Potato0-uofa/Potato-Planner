package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.ListenerRegistration;

/**
 * Activity that displays the details of a specific event and allows the user to join
 * or leave the waitlist. Receives event data via Intent extras, renders them in the UI,
 * and attaches a real-time Firestore listener to keep the displayed waitlist count in sync.
 *
 * <p>Expected Intent extras:</p>
 * <ul>
 *   <li>"eventId"          – Firestore document ID of the event.</li>
 *   <li>"eventName"        – Display name of the event.</li>
 *   <li>"eventDescription" – Full description of the event.</li>
 * </ul>
 */
public class EventDescriptionView extends AppCompatActivity {

    /** Repository used to read event data and mutate the waiting list in Firestore. */
    private EventRepository eventRepository;

    /** Local representation of the waiting list, used to cache and display the current count. */
    private WaitingList waitingList;

    /**
     * Active Firestore snapshot listener for the waitlist count.
     * Held so it can be detached in #onDestroy() to prevent memory leaks.
     */
    private ListenerRegistration waitlistListener;

    /** TextView that displays the live waitlist headcount. */
    private TextView tvWaitlistCount;

    /** Firestore document ID for the event being viewed. */
    private String eventId;

    /** The device's ANDROID_ID, used as the entrant identifier. */
    private String deviceId;

    /** Button to join the waitlist. Visible when the user is not on the waitlist. */
    private Button btnJoinEvent;

    /** Button to leave the waitlist. Visible when the user is already on the waitlist. */
    private Button btnLeaveEvent;

    /**
     * Initializes the activity: inflates the layout, resolves Intent extras, populates
     * event name and description views, wires up the join and leave waitlist buttons,
     * checks the user's current waitlist status, configures bottom navigation, and
     * starts the real-time waitlist listener.
     *
     * @param savedInstanceState Saved instance state bundle, or null on first creation.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        eventRepository = new EventRepository();
        waitingList = new WaitingList();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            eventId = "test_event_1";
        }

        String eventName = getIntent().getStringExtra("eventName");
        String eventDescription = getIntent().getStringExtra("eventDescription");

        tvWaitlistCount = findViewById(R.id.Waitlist_Count);
        btnJoinEvent = findViewById(R.id.join_event_button);
        btnLeaveEvent = findViewById(R.id.leave_event_button);

        ((TextView) findViewById(R.id.event_name)).setText(eventName != null ? eventName : "Test Event");
        ((TextView) findViewById(R.id.event_details)).setText(eventDescription != null ? eventDescription : "Test Description");

        btnJoinEvent.setOnClickListener(v -> {
            eventRepository.joinWaitingList(eventId, deviceId, new EventRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(EventDescriptionView.this, "Joined Waitlist!", Toast.LENGTH_SHORT).show();
                    updateButtonVisibility(true);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(EventDescriptionView.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnLeaveEvent.setOnClickListener(v -> {
            eventRepository.leaveWaitingList(eventId, deviceId, new EventRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(EventDescriptionView.this, "Left Waitlist!", Toast.LENGTH_SHORT).show();
                    updateButtonVisibility(false);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(EventDescriptionView.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        checkWaitlistStatus();
        setupNavigation();
        startListeningToWaitlist();
    }

    /**
     * Checks whether the current device is already on the waitlist and updates
     * button visibility accordingly.
     */
    private void checkWaitlistStatus() {
        eventRepository.isOnWaitingList(eventId, deviceId, new EventRepository.WaitlistStatusCallback() {
            @Override
            public void onSuccess(boolean isOnWaitlist) {
                updateButtonVisibility(isOnWaitlist);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDescriptionView.this, "Error checking waitlist status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Toggles the visibility of the join and leave buttons based on the device's
     * current waitlist status.
     *
     * @param isOnWaitlist true to show the leave button, false to show the join button
     */
    private void updateButtonVisibility(boolean isOnWaitlist) {
        btnJoinEvent.setVisibility(isOnWaitlist ? View.GONE : View.VISIBLE);
        btnLeaveEvent.setVisibility(isOnWaitlist ? View.VISIBLE : View.GONE);
    }

    /**
     * Attaches a real-time Firestore listener that updates tvWaitlistCount
     * whenever the number of entrants on the waitlist changes.
     *
     * <p>The returned ListenerRegistration is stored in waitlistListener
     * and removed in onDestroy().</p>
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
     *
     * <ul>
     *   <li><b>home_button_event_page</b>    – Navigates to HomePage.</li>
     *   <li><b>search_button_event_page</b>  – Navigates to SearchScreen.</li>
     *   <li><b>browse_button_event_page</b>  – Navigates to NonAdminBrowseEvents.</li>
     *   <li><b>profile_button_event_page</b> – Navigates to Profile.</li>
     *   <li><b>exit_button_event_page</b>    – Calls finish() to close the activity.</li>
     * </ul>
     */
    private void setupNavigation() {
        findViewById(R.id.home_button_event_page).setOnClickListener(v ->
                startActivity(new Intent(EventDescriptionView.this, HomePage.class)));

        findViewById(R.id.search_button_event_page).setOnClickListener(v ->
                startActivity(new Intent(EventDescriptionView.this, SearchScreen.class)));

        findViewById(R.id.browse_button_event_page).setOnClickListener(v ->
                startActivity(new Intent(EventDescriptionView.this, NonAdminBrowseEvents.class)));

        findViewById(R.id.profile_button_event_page).setOnClickListener(v ->
                startActivity(new Intent(EventDescriptionView.this, Profile.class)));

        findViewById(R.id.exit_button_event_page).setOnClickListener(v -> finish());
    }

    /**
     * Lifecycle callback invoked when the activity is being destroyed.
     * Detaches the Firestore waitlist listener to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitlistListener != null) {
            waitlistListener.remove();
        }
    }
}