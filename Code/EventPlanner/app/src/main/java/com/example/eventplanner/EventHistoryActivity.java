package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity that displays the user's event registration history.
 * Shows all events the user has registered for, including events
 * they were not selected for, with the event date and registration timestamp.
 * If the user has no registrations, a message is displayed instead.
 */
public class EventHistoryActivity extends AppCompatActivity {

    private ListView listView;
    private TextView emptyText;
    private FirebaseFirestore db;
    private String deviceId;

    /**
     * Initializes the activity, sets up the list view, and loads
     * the user's event registration history from Firestore.
     *
     * @param savedInstanceState the previously saved instance state (if applicable)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        listView  = findViewById(R.id.notification_list);
        emptyText = new TextView(this);
        emptyText.setText("User has not registered in any events!");
        emptyText.setTextSize(16f);
        emptyText.setTextColor(android.graphics.Color.WHITE);
        emptyText.setTypeface(null, android.graphics.Typeface.BOLD);
        emptyText.setPadding(32, 64, 32, 0);
        emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        ((ViewGroup) listView.getParent()).addView(emptyText);
        listView.setEmptyView(emptyText);

        // Navigation bar
        findViewById(R.id.exit_button_event_history).setOnClickListener(v -> finish());

        findViewById(R.id.home_button_event_history).setOnClickListener(v ->
                startActivity(new Intent(this, HomePage.class)));

        findViewById(R.id.search_button_event_history).setOnClickListener(v ->
                startActivity(new Intent(this, SearchScreen.class)));

        findViewById(R.id.browse_button_event_history).setOnClickListener(v ->
                startActivity(new Intent(this, NonAdminBrowseEvents.class)));

        findViewById(R.id.profile_button_event_history).setOnClickListener(v ->
                startActivity(new Intent(this, Profile.class)));

        findViewById(R.id.new_event_button_event_history).setOnClickListener(v ->
                startActivity(new Intent(this, CreateEventActivity.class)));

        loadHistory();
    }

    /**
     * Loads all registration records for the current user from our Firestore DB,
     * then fetches the corresponding event details for each event registration.
     * Populates the list with event name, date, registration timestamp, and its status.
     * Shows an empty state message if there are no registrations found.
     */
    private void loadHistory() {
        db.collection("registrations")
                .whereEqualTo("userId", deviceId)
                .get()
                .addOnSuccessListener(registrationSnapshots -> {
                    if (registrationSnapshots.isEmpty()) {
                        // Empty view will show automatically via setEmptyView
                        return;
                    }

                    List<String> eventIds = new ArrayList<>();
                    List<String> statuses = new ArrayList<>();
                    List<String> timestamps = new ArrayList<>();

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault());

                    for (QueryDocumentSnapshot doc : registrationSnapshots) {
                        String eventId = doc.getString("eventId");
                        String status  = doc.getString("status");
                        com.google.firebase.Timestamp joinedAt =
                                doc.getTimestamp("joinedAt");

                        if (eventId != null) {
                            eventIds.add(eventId);
                            statuses.add(status != null ? status : "unknown");
                            timestamps.add(joinedAt != null
                                    ? sdf.format(new Date(joinedAt.toDate().getTime()))
                                    : "Unknown date");
                        }
                    }

                    fetchEventsAndDisplay(eventIds, statuses, timestamps);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show());
    }

    /**
     * Fetches the event details from our Firestore DB for each event ID in the registration list,
     * builds a display string for each entry, and populates the ListView.
     *
     * @param eventIds   list of event IDs from the user's registrations
     * @param statuses   list of registration statuses corresponding to each event
     * @param timestamps list of formatted registration timestamps for each event
     */
    private void fetchEventsAndDisplay(List<String> eventIds, List<String> statuses, List<String> timestamps) {
        List<String> displayItems = new ArrayList<>();
        final int[] remaining = {eventIds.size()};

        for (int i = 0; i < eventIds.size(); i++) {
            final int index = i;
            final String eventId = eventIds.get(i);

            db.collection("events")
                    .document(eventId)
                    .get()
                    .addOnSuccessListener(eventDoc -> {
                        String eventName = eventDoc.getString("name");
                        String eventDate = eventDoc.getString("date");
                        String status    = statuses.get(index);
                        String joinedAt  = timestamps.get(index);

                        String displayText =
                                (eventName != null ? eventName : "Unknown Event") + "\n" +
                                        "Date: " + (eventDate != null ? eventDate : "N/A") + "\n" +
                                        "Registered: " + joinedAt + "\n" +
                                        "Status: " + capitalize(status);

                        displayItems.add(displayText);
                        remaining[0]--;

                        if (remaining[0] == 0) {
                            showList(displayItems);
                        }
                    })
                    .addOnFailureListener(e -> {
                        displayItems.add("Unknown Event\nFailed to load details");
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            showList(displayItems);
                        }
                    });
        }
    }

    /**
     * Populates the ListView with the given list of display strings.
     *
     * @param items the list of formatted strings to display in the history list
     */
    private void showList(List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                items
        );
        listView.setAdapter(adapter);
    }

    /**
     * Capitalizes the first letter of a string. (For cleanliness)
     *
     * @param text the string to capitalize
     * @return the capitalized string, or the original if null or empty
     */
    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}