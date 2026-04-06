package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
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

/** Activity for browsing and filtering events with search, sort, and tag filtering. */
public class BrowseEventsActivity extends AppCompatActivity {

    /** Master list of all events loaded from Firestore, used as the source for filtering. */
    private final List<Events> allEventsList = new ArrayList<>();

    /** RecyclerView displaying the filtered event list. */
    private RecyclerView recyclerView;

    /** Adapter binding event data to the RecyclerView. */
    private EventAdapter adapter;

    /** Filtered list of events currently displayed. */
    private final List<Events> eventList = new ArrayList<>();

    /** Firestore database instance. */
    private FirebaseFirestore db;

    /** Currently active date filter, or null if none. */
    private String activeAvailabilityDate = null;

    /** Minimum capacity filter, or -1 if not set. */
    private int activeMinCapacity = -1;

    /** Currently active tag filters that events must match. */
    private final List<String> activeTagFilters = new ArrayList<>();

    /** Current sort order: "none", "alpha", "date_asc", or "date_desc". */
    private String activeSortOrder = "none";

    /** All available event tags for filtering. */
    private static final String[] ALL_TAGS = {
            "Entertainment", "Sports", "Cooking", "Outdoors", "Gaming", "Music", "Active", "Art"
    };

    /**
     * Initializes the activity, sets up the event list, search bar, filter button,
     * and bottom navigation.
     *
     * @param savedInstanceState previously saved activity state, if any
     */
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
        findViewById(R.id.exit_button_browse).setOnClickListener(v -> finish());

        // Bottom nav
        findViewById(R.id.new_event_button_browse).setOnClickListener(v -> {
            EventTypeFragment fragment = new EventTypeFragment();
            fragment.show(getSupportFragmentManager(), "NewEventFragment");
        });
        findViewById(R.id.qr_button_browse).setOnClickListener(v ->
                startActivity(new Intent(BrowseEventsActivity.this, SearchScreen.class)));
        findViewById(R.id.home_button_browse).setOnClickListener(v ->
                startActivity(new Intent(BrowseEventsActivity.this, HomePage.class)));
        findViewById(R.id.browse_button_browse).setOnClickListener(v -> {});
        findViewById(R.id.profile_button_browse).setOnClickListener(v ->
                startActivity(new Intent(BrowseEventsActivity.this, Profile.class)));

        // Real-time search
        EditText searchBar = findViewById(R.id.et_search_bar);
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        loadAllEvents();
    }

    /**
     * Displays the filter dialog with date, capacity, tag, and sort options.
     */
    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_filter_events, null);

        EditText etDate        = dialogView.findViewById(R.id.filter_date_input);
        CheckBox cbDate        = dialogView.findViewById(R.id.filter_date_checkbox);
        EditText etCapacity    = dialogView.findViewById(R.id.filter_capacity_input);
        CheckBox cbCapacity    = dialogView.findViewById(R.id.filter_capacity_checkbox);
        Button btnClear        = dialogView.findViewById(R.id.filter_clear_button);
        final RadioGroup sortGroup = dialogView.findViewById(R.id.sort_radio_group);

        CheckBox cbEntertainment = dialogView.findViewById(R.id.tag_entertainment);
        CheckBox cbSports        = dialogView.findViewById(R.id.tag_sports);
        CheckBox cbCooking       = dialogView.findViewById(R.id.tag_cooking);
        CheckBox cbOutdoors      = dialogView.findViewById(R.id.tag_outdoors);
        CheckBox cbGaming        = dialogView.findViewById(R.id.tag_gaming);
        CheckBox cbMusic         = dialogView.findViewById(R.id.tag_music);
        CheckBox cbActive        = dialogView.findViewById(R.id.tag_active);
        CheckBox cbArt           = dialogView.findViewById(R.id.tag_art);

        final CheckBox[] tagBoxes = {
                cbEntertainment, cbSports, cbCooking, cbOutdoors,
                cbGaming, cbMusic, cbActive, cbArt
        };

        // Pre-fill active filters
        if (activeAvailabilityDate != null) {
            cbDate.setChecked(true);
            etDate.setText(activeAvailabilityDate);
        }
        if (activeMinCapacity != -1) {
            cbCapacity.setChecked(true);
            etCapacity.setText(String.valueOf(activeMinCapacity));
        }
        for (int i = 0; i < ALL_TAGS.length; i++) {
            tagBoxes[i].setChecked(activeTagFilters.contains(ALL_TAGS[i]));
        }

        // Pre-select active sort
        switch (activeSortOrder) {
            case "alpha":     sortGroup.check(R.id.sort_alphabetical); break;
            case "date_asc":  sortGroup.check(R.id.sort_date_old_new); break;
            case "date_desc": sortGroup.check(R.id.sort_date_new_old); break;
            default:          sortGroup.check(R.id.sort_none); break;
        }

        etDate.setEnabled(cbDate.isChecked());
        etCapacity.setEnabled(cbCapacity.isChecked());
        cbDate.setOnCheckedChangeListener((btn, checked) -> etDate.setEnabled(checked));
        cbCapacity.setOnCheckedChangeListener((btn, checked) -> etCapacity.setEnabled(checked));

        btnClear.setOnClickListener(v -> {
            activeAvailabilityDate = null;
            activeMinCapacity = -1;
            activeTagFilters.clear();
            activeSortOrder = "none";

            // Reset the UI inside the dialog too
            cbDate.setChecked(false);
            etDate.setText("");
            etDate.setEnabled(false);
            cbCapacity.setChecked(false);
            etCapacity.setText("");
            etCapacity.setEnabled(false);
            for (CheckBox cb : tagBoxes) cb.setChecked(false);
            sortGroup.check(R.id.sort_none);

            applyFilters("");
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Filter Events")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    // Date
                    if (cbDate.isChecked()) {
                        String dateStr = etDate.getText().toString().trim();
                        activeAvailabilityDate = dateStr.isEmpty() ? null : dateStr;
                    } else {
                        activeAvailabilityDate = null;
                    }

                    // Capacity
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

                    // Tags
                    activeTagFilters.clear();
                    for (int i = 0; i < ALL_TAGS.length; i++) {
                        if (tagBoxes[i].isChecked()) {
                            activeTagFilters.add(ALL_TAGS[i]);
                        }
                    }

                    // Sort
                    int sortId = sortGroup.getCheckedRadioButtonId();
                    if (sortId == R.id.sort_alphabetical)      activeSortOrder = "alpha";
                    else if (sortId == R.id.sort_date_old_new) activeSortOrder = "date_asc";
                    else if (sortId == R.id.sort_date_new_old) activeSortOrder = "date_desc";
                    else                                       activeSortOrder = "none";

                    applyFilters("");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Loads all events from Firestore and triggers initial filter application.
     */
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
                    applyFilters("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show());
    }

    /**
     * Applies the active search query, date, capacity, tag, and sort filters
     * to the master event list and updates the displayed list.
     *
     * @param query the search query string to match against event names
     */
    private void applyFilters(String query) {
        String lowerQuery = query.toLowerCase().trim();

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

            // Hide private events
            if (event.isPrivate()) continue;

            // Search query
            if (!lowerQuery.isEmpty()) {
                String name = event.getName() != null ? event.getName().toLowerCase() : "";
                if (!name.contains(lowerQuery)) continue;
            }

            // Date filter
            if (filterDate != null) {
                try {
                    String regStart = event.getRegistrationStart();
                    String regEnd   = event.getRegistrationEnd();
                    if (regStart == null || regEnd == null ||
                            regStart.isEmpty() || regEnd.isEmpty()) continue;
                    Date startDate = sdf.parse(regStart);
                    Date endDate   = sdf.parse(regEnd);
                    if (startDate == null || endDate == null) continue;
                    if (filterDate.before(startDate) || filterDate.after(endDate)) continue;
                } catch (ParseException e) {
                    continue;
                }
            }

            // Capacity filter
            if (activeMinCapacity != -1) {
                int limit = event.getWaitlistLimit();
                if (limit != -1 && limit < activeMinCapacity) continue;
            }

            // Tag filter — event must have ALL selected tags
            if (!activeTagFilters.isEmpty()) {
                List<String> eventTags = event.getTags();
                if (eventTags == null || eventTags.isEmpty()) continue;
                List<String> lowerEventTags = new ArrayList<>();
                for (String t : eventTags) lowerEventTags.add(t.toLowerCase());
                boolean hasAllTags = true;
                for (String required : activeTagFilters) {
                    if (!lowerEventTags.contains(required.toLowerCase())) {
                        hasAllTags = false;
                        break;
                    }
                }
                if (!hasAllTags) continue;
            }

            eventList.add(event);
        }

        // Sort
        switch (activeSortOrder) {
            case "alpha":
                eventList.sort((a, b) -> {
                    String nameA = a.getName() != null ? a.getName() : "";
                    String nameB = b.getName() != null ? b.getName() : "";
                    return nameA.compareToIgnoreCase(nameB);
                });
                break;
            case "date_asc":
                eventList.sort((a, b) -> {
                    String dateA = a.getRegistrationStart() != null ? a.getRegistrationStart() : "";
                    String dateB = b.getRegistrationStart() != null ? b.getRegistrationStart() : "";
                    return dateA.compareTo(dateB);
                });
                break;
            case "date_desc":
                eventList.sort((a, b) -> {
                    String dateA = a.getRegistrationStart() != null ? a.getRegistrationStart() : "";
                    String dateB = b.getRegistrationStart() != null ? b.getRegistrationStart() : "";
                    return dateB.compareTo(dateA);
                });
                break;
        }

        adapter.notifyDataSetChanged();

        if (eventList.isEmpty()) {
            Toast.makeText(this, "No events match your filters", Toast.LENGTH_SHORT).show();
        }
    }
}