package com.example.eventplanner;

import android.app.AlertDialog;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows cancelled/declined entrants and allows the organizer to redraw
 * replacements from the waiting list.
 *
 * US 02.05.03: Organizer draws replacement applicant from the pooling system.
 */
public class FragmentPostDrawWaitlist extends DialogFragment {

    private final List<Entrant> entrantList = new ArrayList<>();
    private WaitlistAdapter adapter;
    private String eventId;

    public static FragmentPostDrawWaitlist newInstance(String eventId) {
        FragmentPostDrawWaitlist fragment = new FragmentPostDrawWaitlist();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_draw_waitlist, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WaitlistAdapter(entrantList);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.exit_button_canceled_waitlist).setOnClickListener(v -> dismiss());

        // US 02.05.03 - Redraw: draw a replacement from the waitlist
        view.findViewById(R.id.redraw_entrants_button).setOnClickListener(v -> {
            EventRepository eventRepository = new EventRepository();
            eventRepository.drawFromWaitlist(eventId, 1, new EventRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(),
                            "Replacement entrant drawn from waitlist!",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(),
                            "Redraw failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        // US 02.07.03 - Notify all cancelled entrants
        view.findViewById(R.id.notification_button_canceled_waitlist).setOnClickListener(v ->
                showNotifyCancelledDialog());

        loadCancelledEntrants();

        return view;
    }

    private void loadCancelledEntrants() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> cancelledIds = (List<String>) documentSnapshot.get("cancelledEntrants");
                    if (cancelledIds == null || cancelledIds.isEmpty()) {
                        Toast.makeText(getContext(), "No cancelled entrants",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    entrantList.clear();
                    for (String userId : cancelledIds) {
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
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
                                        Toast.makeText(getContext(), "Error loading entrant",
                                                Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading event", Toast.LENGTH_SHORT).show());
    }

    private void showNotifyCancelledDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Notify Cancelled Entrants");
        final EditText input = new EditText(getContext());
        input.setHint("Enter message for all cancelled entrants");
        builder.setView(input);
        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            sendNoticeToCancelledEntrants(message);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void sendNoticeToCancelledEntrants(String message) {
        FirebaseFirestore.getInstance()
                .collection("registrations")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "cancelled")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> cancelledUserIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String userId = doc.getString("userId");
                        if (userId != null && !userId.trim().isEmpty()) {
                            cancelledUserIds.add(userId);
                        }
                    }

                    if (cancelledUserIds.isEmpty()) {
                        Toast.makeText(getContext(), "No cancelled entrants found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    RegistrationRepository registrationRepository = new RegistrationRepository();
                    registrationRepository.sendNoticeToUsers(
                            eventId,
                            cancelledUserIds,
                            "cancelled_notice",
                            message,
                            new RegistrationRepository.SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getContext(),
                                            "Notification sent to cancelled entrants",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(getContext(),
                                            "Failed to notify cancelled entrants: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                })
                .addOnFailureListener(e -> Toast.makeText(
                        getContext(),
                        "Failed to load cancelled entrants",
                        Toast.LENGTH_SHORT
                ).show());
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
