package com.example.eventplanner;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/** Activity showing the waitlist for an event with join and leave functionality. */
public class EventWaitlistActivity extends AppCompatActivity {

    private EventRepository eventRepository;
    private WaitingList waitingList;
    private ListenerRegistration waitlistListener;
    private TextView tvWaitlistCount;
    private RecyclerView recyclerView;
    private WaitlistAdapter adapter;
    private List<Entrant> entrantList = new ArrayList<>();
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_waitlist);

        eventRepository = new EventRepository();
        waitingList = new WaitingList();

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            eventId = "test_event_1";
        }

        tvWaitlistCount = findViewById(R.id.tv_waitlist_count);
        recyclerView = findViewById(R.id.recycler_waitlist);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaitlistAdapter(entrantList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_join_waitlist).setEnabled(false);
        findViewById(R.id.exit_button_waitlist).setOnClickListener(v -> finish());

        findViewById(R.id.btn_view_chosen).setOnClickListener(v -> {
            FragmentFinalEntrantList fragment = FragmentFinalEntrantList.newInstance(eventId);
            fragment.show(getSupportFragmentManager(), "FinalEntrantList");
        });

        findViewById(R.id.btn_view_cancelled).setOnClickListener(v -> {
            FragmentPostDrawWaitlist fragment = FragmentPostDrawWaitlist.newInstance(eventId);
            fragment.show(getSupportFragmentManager(), "PostDrawWaitlist");
        });

        Button notifyAllBtn = findViewById(R.id.btn_notify_all_waiting_list);
        if (notifyAllBtn != null) {
            notifyAllBtn.setOnClickListener(v -> showNotifyAllDialog());
        }

        startListening();
        loadEntrants();
    }

    private void startListening() {
        waitlistListener = eventRepository.listenToWaitlistCount(eventId, new EventRepository.CountCallback() {
            @Override
            public void onSuccess(int count) {
                waitingList.setCloudCount(count);
                tvWaitlistCount.setText("Total Entrants: " + waitingList.getCount());
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventWaitlistActivity.this, "Error listening to count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads the entrants for the event from Firestore and updates the RecyclerView.
     */
    private void loadEntrants() {
        eventRepository.fetchWaitlistEntrants(eventId, new EventRepository.EntrantsCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                entrantList.clear();
                entrantList.addAll(entrants);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventWaitlistActivity.this, "Error loading entrants: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a dialog for notifying all waiting list entrants.
     */
    private void showNotifyAllDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notify All Waiting List");
        final EditText input = new EditText(this);
        input.setHint("Enter notification message");
        builder.setView(input);
        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            sendNoticeToWaitingList(message);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void sendNoticeToWaitingList(String message) {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> waitingListIds = (List<String>) snapshot.get("waitingList");
                    if (waitingListIds == null || waitingListIds.isEmpty()) {
                        waitingListIds = (List<String>) snapshot.get("waitlist");
                    }
                    if (waitingListIds == null || waitingListIds.isEmpty()) {
                        Toast.makeText(this, "No entrants on waitlist", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    RegistrationRepository registrationRepository = new RegistrationRepository();
                    registrationRepository.sendNoticeToUsers(
                            eventId,
                            waitingListIds,
                            "waitlist_notice",
                            message,
                            new RegistrationRepository.SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(EventWaitlistActivity.this,
                                            "Notifications sent",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(EventWaitlistActivity.this,
                                            "Failed: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Failed to load event",
                        Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitlistListener != null) {
            waitlistListener.remove();
        }
    }
}