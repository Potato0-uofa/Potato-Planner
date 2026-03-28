package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BrowseEventsActivity extends AppCompatActivity {

    private final List<Events> allEventsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private final List<Events> eventList = new ArrayList<>();
    private FirebaseFirestore db;

    // Currently active filters
    private String activeAvailabilityDate = null; // yyyy-MM-dd, null means no filter
    private int activeMinCapacity = -1;           // -1 means no filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recycler_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EventAdapter(eventList);
        recyclerView.setAdapter(adapter);

        Button btnFilter = findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> showFilterDialog());

        // Setup bottom navigation bar
        findViewById(R.id.new_event_button_browse).setOnClickListener(v -> {
            EventTypeFragment fragment = new EventTypeFragment();
            fragment.show(getSupportFragmentManager(), "NewEventFragment");
        });

        findViewById(R.id.qr_button_browse).setOnClickListener(v ->
                startActivity(new Intent(BrowseEventsActivity.this, SearchScreen.class)));

        findViewById(R.id.home_button_browse).setOnClickListener(v ->
                startActivity(new Intent(BrowseEventsActivity.this, HomePage.class)));

        findViewById(R.id.browse_button_browse).setOnClickListener(v -> {
            // Already on browse
        });

        findViewById(R.id.profile_button_browse).setOnClickListener(v ->
                startActivity(new Intent(BrowseEventsActivity.this, Profile.class)));

        EditText searchBar = findViewById(R.id.et_search_bar);
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        loadAllEvents();
    }

    // Allows the user to filter based on date availability, as well as event capacity
    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_filter_events, null);

        EditText etDate = dialogView.findViewById(R.id.filter_date_input);
        CheckBox cbDate = dialogView.findViewById(R.id.filter_date_checkbox);
        EditText etCapacity = dialogView.findViewById(R.id.filter_capacity_input);
        CheckBox cbCapacity = dialogView.findViewById(R.id.filter_capacity_checkbox);
        Button btnClear = dialogView.findViewById(R.id.filter_clear_button);

        // Pre-fill with active filters if any
        if (activeAvailabilityDate != null) {
            cbDate.setChecked(true);
            etDate.setText(activeAvailabilityDate);
        }
        if (activeMinCapacity != -1) {
            cbCapacity.setChecked(true);
            etCapacity.setText(String.valueOf(activeMinCapacity));
        }

        // Toggle the input fields when user selects the checkbox
        etDate.setEnabled(cbDate.isChecked());
        etCapacity.setEnabled(cbCapacity.isChecked());
        cbDate.setOnCheckedChangeListener((btn, checked) -> etDate.setEnabled(checked));
        cbCapacity.setOnCheckedChangeListener((btn, checked) -> etCapacity.setEnabled(checked));

        // Clear all filters selected
        btnClear.setOnClickListener(v -> {
            activeAvailabilityDate = null;
            activeMinCapacity = -1;
            applyFilters();
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Filter Events")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    // Read availability date filter
                    if (cbDate.isChecked()) {
                        String dateStr = etDate.getText().toString().trim();
                        if (!dateStr.isEmpty()) {
                            activeAvailabilityDate = dateStr;
                        } else {
                            activeAvailabilityDate = null;
                        }
                    } else {
                        activeAvailabilityDate = null;
                    }

                    // Reads the capacity filter
                    if (cbCapacity.isChecked()) {
                        String capStr = etCapacity.getText().toString().trim();
                        if (!capStr.isEmpty()) {
                            try {
                                activeMinCapacity = Integer.parseInt(capStr);
                            } catch (NumberFormatException e) {
                                activeMinCapacity = -1;
                                Toast.makeText(this, "Invalid capacity value", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            activeMinCapacity = -1;
                        }
                    } else {
                        activeMinCapacity = -1;
                    }

                    applyFilters();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Loads all events from Firestore into allEventsList, then applies the selected filters
    private void loadAllEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEventsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Events event = document.toObject(Events.class);
                        event.setEventId(document.getId());
                        allEventsList.add(event);
                    }
                    applyFilters();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show());
    }

    // Applies the active availability and capacity filters to allEventsList and updates
    // the RecyclerView in real time
    private void applyFilters() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date filterDate = null;

        if (activeAvailabilityDate != null) {
            try {
                filterDate = sdf.parse(activeAvailabilityDate);
            } catch (ParseException e) {
                Toast.makeText(this, "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
                activeAvailabilityDate = null;
            }
        }

        eventList.clear();
        for (Events event : allEventsList) {

            // Availability filter: checks registration is open on the requested date
            if (filterDate != null) {
                try {
                    String regStart = event.getRegistrationStart();
                    String regEnd = event.getRegistrationEnd();

                    // Skip if event has no registration dates
                    if (regStart == null || regEnd == null ||
                            regStart.isEmpty() || regEnd.isEmpty()) {
                        continue;
                    }

                    Date startDate = sdf.parse(regStart);
                    Date endDate = sdf.parse(regEnd);

                    // Event must be open (filterDate is within registration window)
                    if (startDate == null || endDate == null) continue;
                    if (filterDate.before(startDate) || filterDate.after(endDate)) continue;

                } catch (ParseException e) {
                    // Skip events with unparseable dates
                    continue;
                }
            }

            // Capacity filter: waitlist limit must be >= the user's requested minimum
            if (activeMinCapacity != -1) {
                int limit = event.getWaitlistLimit();
                // -1 means unlimited — treat as always passing capacity filter
                if (limit != -1 && limit < activeMinCapacity) continue;
            }

            eventList.add(event);
        }

        adapter.notifyDataSetChanged();

        if (eventList.isEmpty()) {
            Toast.makeText(this, "No events match your filters", Toast.LENGTH_SHORT).show();
        }
    }

    // Filters the current eventList by search query on top of active filters
    private void filterBySearch(String query) {
        String lowerQuery = query.toLowerCase().trim();
        eventList.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date filterDate = null;
        if (activeAvailabilityDate != null) {
            try {
                filterDate = sdf.parse(activeAvailabilityDate);
            } catch (ParseException e) {
                filterDate = null;
            }
        }

        for (Events event : allEventsList) {
            // Apply the search query
            if (event.getName() == null ||
                    !event.getName().toLowerCase().contains(lowerQuery)) continue;

            // Apply the availability filter
            if (filterDate != null) {
                try {
                    String regStart = event.getRegistrationStart();
                    String regEnd = event.getRegistrationEnd();
                    if (regStart == null || regEnd == null ||
                            regStart.isEmpty() || regEnd.isEmpty()) continue;
                    Date startDate = sdf.parse(regStart);
                    Date endDate = sdf.parse(regEnd);
                    if (startDate == null || endDate == null) continue;
                    if (filterDate.before(startDate) || filterDate.after(endDate)) continue;
                } catch (ParseException e) {
                    continue;
                }
            }

            // Apply the capacity filter
            if (activeMinCapacity != -1) {
                int limit = event.getWaitlistLimit();
                if (limit != -1 && limit < activeMinCapacity) continue;
            }

            eventList.add(event);
        }

        adapter.notifyDataSetChanged();
    }
}