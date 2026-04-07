package com.example.eventplanner;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows all entrants currently on the waitlist for an event, before the draw
 * has taken place.
 *
 * Reads the waitingList array directly from the event document and loads
 * each user's profile from the "users" collection.
 */
public class FragmentPreDrawWaitlist extends DialogFragment {

    private final List<Entrant> entrantList = new ArrayList<>();
    private WaitlistAdapter adapter;
    private String eventId;

    public static FragmentPreDrawWaitlist newInstance(String eventId) {
        FragmentPreDrawWaitlist fragment = new FragmentPreDrawWaitlist();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_waitlist, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WaitlistAdapter(entrantList);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.exit_button_waitlist).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.notification_button_waitlist).setOnClickListener(v ->
                showNotifyWaitlistDialog());

        loadWaitlistedEntrants();

        return view;
    }

    private void showNotifyWaitlistDialog() {
        if (getContext() == null) return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Notify Waiting List");
        final EditText input = new EditText(getContext());
        input.setHint("Enter message for all waiting-list entrants");
        builder.setView(input);
        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            sendNoticeToWaitlist(message);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void sendNoticeToWaitlist(String message) {
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
                        Toast.makeText(getContext(), "No entrants on waitlist", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(getContext(),
                                            "Notification sent to waiting list",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(getContext(),
                                            "Failed to notify waiting list: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                })
                .addOnFailureListener(e -> Toast.makeText(
                        getContext(),
                        "Failed to load event",
                        Toast.LENGTH_SHORT
                ).show());
    }

    /**
     * Reads the waitingList array from the event document and fetches
     * each entrant's user profile from the "users" collection.
     */
    private void loadWaitlistedEntrants() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        Toast.makeText(getContext(), "Event not found",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> deviceIds = (List<String>) eventDoc.get("waitingList");
                    if (deviceIds == null || deviceIds.isEmpty()) {
                        deviceIds = (List<String>) eventDoc.get("waitlist");
                    }
                    if (deviceIds == null || deviceIds.isEmpty()) {
                        Toast.makeText(getContext(), "No entrants on the waitlist",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    entrantList.clear();

                    for (String deviceId : deviceIds) {
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(deviceId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        Entrant entrant = userDoc.toObject(Entrant.class);
                                        if (entrant != null) {
                                            entrant.setDeviceId(userDoc.getId());
                                            entrantList.add(entrant);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Error loading entrant",
                                                Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading waitlist",
                                Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}