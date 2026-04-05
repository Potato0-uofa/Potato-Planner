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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Dialog displaying all selected entrants (invited, accepted, or declined) for an event. */
public class FragmentSelectedEntrants extends DialogFragment {

    private static final List<String> SELECTED_STATUSES = java.util.Arrays.asList(
            "invited",
            "accepted",
            "declined",
            "cancelled"
    );

    private final List<Entrant> entrantList = new ArrayList<>();
    private WaitlistAdapter adapter;
    private String eventId;

    public static FragmentSelectedEntrants newInstance(String eventId) {
        FragmentSelectedEntrants fragment = new FragmentSelectedEntrants();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selected_entrants, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WaitlistAdapter(entrantList);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.exit_button_selected).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.notification_button_selected).setOnClickListener(v ->
                showNotifySelectedDialog());

        loadSelectedEntrants();

        return view;
    }

    private void loadSelectedEntrants() {
        FirebaseFirestore.getInstance()
                .collection("registrations")
                .whereEqualTo("eventId", eventId)
                .whereIn("status", SELECTED_STATUSES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> selectedIds = new LinkedHashSet<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String userId = doc.getString("userId");
                        if (userId != null && !userId.trim().isEmpty()) {
                            selectedIds.add(userId);
                        }
                    }

                    if (selectedIds.isEmpty()) {
                        Toast.makeText(getContext(), "No selected entrants found",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    entrantList.clear();
                    for (String userId : selectedIds) {
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
                        Toast.makeText(getContext(), "Error loading selected entrants",
                                Toast.LENGTH_SHORT).show());
    }

    private void showNotifySelectedDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Notify Selected Entrants");
        final EditText input = new EditText(getContext());
        input.setHint("Enter message for all selected entrants");
        builder.setView(input);
        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            sendNoticeToSelected(message);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void sendNoticeToSelected(String message) {
        FirebaseFirestore.getInstance()
                .collection("registrations")
                .whereEqualTo("eventId", eventId)
                .whereIn("status", SELECTED_STATUSES)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Set<String> selectedIds = new LinkedHashSet<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String userId = doc.getString("userId");
                        if (userId != null && !userId.trim().isEmpty()) {
                            selectedIds.add(userId);
                        }
                    }

                    if (selectedIds.isEmpty()) {
                        Toast.makeText(getContext(), "No selected entrants found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    RegistrationRepository registrationRepository = new RegistrationRepository();
                    registrationRepository.sendNoticeToUsers(
                            eventId,
                            new ArrayList<>(selectedIds),
                            "selected_notice",
                            message,
                            new RegistrationRepository.SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getContext(),
                                            "Notification sent to selected entrants",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(getContext(),
                                            "Failed to notify selected entrants: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                })
                .addOnFailureListener(e -> Toast.makeText(
                        getContext(),
                        "Failed to load selected entrants",
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