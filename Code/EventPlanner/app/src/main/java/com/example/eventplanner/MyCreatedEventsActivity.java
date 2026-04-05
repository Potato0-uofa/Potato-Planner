package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/** Activity listing all events created by the current user with status and visibility info. */
public class MyCreatedEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView listView;
    private List<Events> createdEvents = new ArrayList<>();
    private ArrayAdapter<Events> adapter;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_created_events);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        listView = findViewById(R.id.list_created_events);

        adapter = new ArrayAdapter<Events>(this, R.layout.item_my_created_event, createdEvents) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(R.layout.item_my_created_event, parent, false);
                }

                Events event = getItem(position);
                if (event != null) {
                    TextView tvName = convertView.findViewById(R.id.tv_event_name);
                    TextView tvDate = convertView.findViewById(R.id.tv_event_date);
                    TextView tvStatus = convertView.findViewById(R.id.tv_event_status);
                    TextView tvVisibility = convertView.findViewById(R.id.tv_event_visibility);

                    tvName.setText(event.getName() != null ? event.getName() : "Unnamed Event");

                    String date = event.getDate();
                    tvDate.setText(date != null && !date.isEmpty() ? date : "No date set");

                    String status = event.getStatus();
                    if (status != null && !status.isEmpty()) {
                        tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
                    } else {
                        tvStatus.setText("Open");
                    }

                    tvVisibility.setText(event.isPrivate() ? "Private" : "Public");
                }

                return convertView;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Events selectedEvent = createdEvents.get(position);
            Intent intent = new Intent(MyCreatedEventsActivity.this, CreateEventActivity.class);
            intent.putExtra("eventId", selectedEvent.getEventId());
            intent.putExtra("eventName", selectedEvent.getName());
            intent.putExtra("eventDescription", selectedEvent.getDescription());
            startActivity(intent);
        });

        // Exit button
        findViewById(R.id.exit_button_created_events).setOnClickListener(v -> finish());

        // Navigation bar
        findViewById(R.id.new_event_button_created).setOnClickListener(v -> {
            EventTypeFragment fragment = new EventTypeFragment();
            fragment.show(getSupportFragmentManager(), "NewEventFragment");
        });
        findViewById(R.id.qr_button_created).setOnClickListener(v ->
                startActivity(new Intent(this, SearchScreen.class)));
        findViewById(R.id.home_button_created).setOnClickListener(v ->
                startActivity(new Intent(this, HomePage.class)));
        findViewById(R.id.browse_button_created).setOnClickListener(v ->
                startActivity(new Intent(this, BrowseEventsActivity.class)));
        findViewById(R.id.profile_button_created).setOnClickListener(v ->
                startActivity(new Intent(this, Profile.class)));

        loadCreatedEvents();
    }

    private void loadCreatedEvents() {
        db.collection("events")
                .whereEqualTo("organizerId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    createdEvents.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Events event = doc.toObject(Events.class);
                        event.setEventId(doc.getId());
                        createdEvents.add(event);
                    }

                    adapter.notifyDataSetChanged();

                    if (createdEvents.isEmpty()) {
                        Toast.makeText(this, "No created events found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load created events", Toast.LENGTH_SHORT).show());
    }
}
