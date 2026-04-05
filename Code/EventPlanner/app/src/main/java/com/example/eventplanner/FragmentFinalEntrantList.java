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

import java.util.ArrayList;
import java.util.List;

/** Dialog showing the event waitlist with navigation to pending, selected, and cancelled entrant views. */
public class FragmentFinalEntrantList extends DialogFragment {

    private final List<Entrant> entrantList = new ArrayList<>();
    private WaitlistAdapter adapter;
    private String eventId;

    public static FragmentFinalEntrantList newInstance(String eventId) {
        FragmentFinalEntrantList fragment = new FragmentFinalEntrantList();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_final_entrant_list, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WaitlistAdapter(entrantList);
        recyclerView.setAdapter(adapter);

        // Exit button
        view.findViewById(R.id.exit_button_entrant_list).setOnClickListener(v -> dismiss());

        // Notify all entrants currently on the waiting list.
        view.findViewById(R.id.notification_button_entrants).setOnClickListener(v -> showNotifyWaitlistDialog());

        // View Pending button
        view.findViewById(R.id.view_pending_button).setOnClickListener(v -> {
            FragmentPendingInvites fragment = FragmentPendingInvites.newInstance(eventId);
            fragment.show(getParentFragmentManager(), "PendingInvites");
        });

        // Selected Entrants button — load chosen entrants into the recycler
        view.findViewById(R.id.selected_entrants_button).setOnClickListener(v -> {
            FragmentSelectedEntrants fragment = FragmentSelectedEntrants.newInstance(eventId);
            fragment.show(getParentFragmentManager(), "SelectedEntrants");
        });

        // Cancelled Entrants button
        view.findViewById(R.id.canceled_entrants_button).setOnClickListener(v -> {
            FragmentPostDrawWaitlist fragment = FragmentPostDrawWaitlist.newInstance(eventId);
            fragment.show(getParentFragmentManager(), "CancelledEntrants");
        });

        // Export CSV button
        view.findViewById(R.id.export_csv_button).setOnClickListener(v -> exportAsCsv());

        loadWaitlistEntrants();

        return view;
    }

    private void loadWaitlistEntrants() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> waitlistIds = (List<String>) documentSnapshot.get("waitingList");
                    if (waitlistIds == null || waitlistIds.isEmpty()) {
                        waitlistIds = (List<String>) documentSnapshot.get("waitlist");
                    }
                    if (waitlistIds == null || waitlistIds.isEmpty()) {
                        Toast.makeText(getContext(), "No entrants on waitlist",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    entrantList.clear();
                    for (String userId : waitlistIds) {
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
                        Toast.makeText(getContext(), "Error loading event",
                                Toast.LENGTH_SHORT).show());
    }

    private void showNotifyWaitlistDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                                    Toast.makeText(
                                            getContext(),
                                            "Notification sent to waiting list",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(
                                            getContext(),
                                            "Failed to notify waiting list: " + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
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

    private void exportAsCsv() {
        if (entrantList.isEmpty()) {
            Toast.makeText(getContext(), "No entrants to export", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csv = new StringBuilder();
        csv.append("Name,Username,Email,Phone,Country,Address\n");
        for (Entrant entrant : entrantList) {
            String name     = entrant.getName()     != null ? entrant.getName()     : "";
            String username = entrant.getUsername() != null ? entrant.getUsername() : "";
            String email    = entrant.getEmail()    != null ? entrant.getEmail()    : "";
            String phone    = entrant.getPhone()    != null ? entrant.getPhone()    : "";
            String country  = entrant.getCountry()  != null ? entrant.getCountry()  : "";
            String address  = entrant.getAddress()  != null ? entrant.getAddress()  : "";

            csv.append("\"").append(name).append("\",")
                    .append("\"").append(username).append("\",")
                    .append("\"").append(email).append("\",")
                    .append("\"").append(phone).append("\",")
                    .append("\"").append(country).append("\",")
                    .append("\"").append(address).append("\"\n");
        }

        try {
            String fileName = "entrants_" + eventId + ".csv";
            java.io.File file = new java.io.File(
                    requireContext().getExternalFilesDir(null), fileName);

            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(csv.toString());
            writer.flush();
            writer.close();

            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    file);

            android.content.Intent shareIntent = new android.content.Intent(
                    android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(android.content.Intent.createChooser(shareIntent, "Export CSV via..."));

        } catch (java.io.IOException e) {
            Toast.makeText(getContext(), "Failed to export: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
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