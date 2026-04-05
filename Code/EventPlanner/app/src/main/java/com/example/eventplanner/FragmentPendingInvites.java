package com.example.eventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Shows entrants with pending invitations (status "invited", not yet accepted).
 * Allows the organizer to cancel entrants who did not sign up.
 *
 * US 02.06.04: Organizer cancels entrants that did not sign up for the event.
 */
public class FragmentPendingInvites extends DialogFragment {

    private final List<Entrant> entrantList = new ArrayList<>();
    private PendingEntrantAdapter adapter;
    private String eventId;

    public static FragmentPendingInvites newInstance(String eventId) {
        FragmentPendingInvites fragment = new FragmentPendingInvites();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_invites, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // US 02.06.04 - Cancel entrants who did not sign up
        adapter = new PendingEntrantAdapter(entrantList, (entrant, position) -> {
            String userId = entrant.getDeviceId();
            if (userId == null || userId.isEmpty()) {
                Toast.makeText(getContext(), "Invalid entrant", Toast.LENGTH_SHORT).show();
                return;
            }

            EventRepository eventRepository = new EventRepository();
            RegistrationRepository registrationRepository = new RegistrationRepository();

            // Move from pendingEntrants to cancelledEntrants on the event
            eventRepository.cancelEntrant(eventId, userId, new EventRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    // Update registration status to cancelled
                    registrationRepository.cancelRegistration(eventId, userId,
                            new RegistrationRepository.SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    entrantList.remove(entrant);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(getContext(),
                                            "Entrant cancelled",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(getContext(),
                                            "Cancelled from event but registration update failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(),
                            "Failed to cancel entrant: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.exit_button_pending_list).setOnClickListener(v -> dismiss());

        loadPendingEntrants();

        return view;
    }

    private void loadPendingEntrants() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> pendingIds = (List<String>) documentSnapshot.get("pendingEntrants");
                    if (pendingIds == null || pendingIds.isEmpty()) {
                        Toast.makeText(getContext(), "No pending invites",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    entrantList.clear();
                    for (String userId : pendingIds) {
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
