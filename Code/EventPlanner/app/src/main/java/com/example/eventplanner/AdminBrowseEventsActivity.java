package com.example.eventplanner;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseEventsActivity extends AppCompatActivity
        implements AdminEventAdapter.OnEventActionListener {

    private RecyclerView recyclerView;
    private AdminEventAdapter adapter;
    private final List<Events> eventList = new ArrayList<>();
    private final EventRepository eventRepository = new EventRepository();

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