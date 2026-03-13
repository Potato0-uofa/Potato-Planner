package com.example.eventplanner;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that allows administrators to browse all events
 * and remove events that violate application policies.
 * Implements user stories:
 * - US 03.02.01: Browse events
 * - US 03.03.01: Remove events
 */
public class AdminBrowseEventsActivity extends AppCompatActivity
        implements AdminEventAdapter.OnEventActionListener {

    /** RecyclerView used to display the list of events. */
    private RecyclerView recyclerView;

    /** Adapter that binds event data to the admin event list UI. */
    private AdminEventAdapter adapter;

    /** In-memory list of events displayed in the RecyclerView. */
    private final List<Events> eventList = new ArrayList<>();

    /** Repository used to load and delete events from persistence. */
    private final EventRepository eventRepository = new EventRepository();

    /**
     * Initializes the activity, sets up the RecyclerView, and loads all events.
     *
     * @param savedInstanceState previously saved activity state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_events);

        recyclerView = findViewById(R.id.recycler_admin_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminEventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);

        loadEvents();
    }

    /**
     * Loads all events from the repository and updates the RecyclerView.
     * This allows administrators to view the list of existing events.
     */
    private void loadEvents() {
        eventRepository.fetchAllEvents(new EventRepository.EventsCallback() {
            @Override
            public void onSuccess(List events) {
                eventList.clear();

                for (Object obj : events) {
                    if (obj instanceof Events) {
                        eventList.add((Events) obj);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminBrowseEventsActivity.this,
                        "Failed to load events: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Handles the removal of an event when an admin clicks the remove button.
     * A confirmation dialog is shown before the event is deleted.
     *
     * @param event the event selected for deletion
     * @param position the position of the event in the displayed list
     */
    @Override
    public void onDeleteClicked(Events event, int position) {
        if (event == null || event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            Toast.makeText(this, "This event cannot be deleted.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Remove Event")
                .setMessage("Are you sure you want to remove this event?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    eventRepository.deleteEvent(event.getEventId(), new EventRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            eventList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(AdminBrowseEventsActivity.this,
                                    "Event removed successfully.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AdminBrowseEventsActivity.this,
                                    "Failed to remove event: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}