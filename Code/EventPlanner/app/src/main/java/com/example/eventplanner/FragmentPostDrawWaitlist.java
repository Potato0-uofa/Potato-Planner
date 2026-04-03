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

        // Redraw — placeholder for lottery system
        view.findViewById(R.id.redraw_entrants_button).setOnClickListener(v ->
                Toast.makeText(getContext(), "Redraw coming soon", Toast.LENGTH_SHORT).show());

        // Notification button — placeholder
        view.findViewById(R.id.notification_button_canceled_waitlist).setOnClickListener(v ->
                Toast.makeText(getContext(), "Notifications coming soon", Toast.LENGTH_SHORT).show());

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