package com.example.eventplanner;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChosenEntrantsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WaitlistAdapter adapter;
    private final List<Entrant> entrantList = new ArrayList<>();
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chosen_entrants);

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            eventId = "test_event_1";
        }

        TextView tvCount = findViewById(R.id.tv_chosen_count);
        recyclerView = findViewById(R.id.recycler_chosen_entrants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaitlistAdapter(entrantList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_back_chosen).setOnClickListener(v -> finish());

        // Load chosen entrants from Firestore
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> chosenIds = (List<String>) documentSnapshot.get("chosenEntrants");
                    if (chosenIds == null || chosenIds.isEmpty()) {
                        tvCount.setText("Chosen Entrants: 0");
                        Toast.makeText(this, "No entrants have been chosen yet", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    tvCount.setText("Chosen Entrants: " + chosenIds.size());

                    // Fetch each chosen entrant's details from the users collection
                    for (String userId : chosenIds) {
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
                                        Toast.makeText(this, "Error loading entrant: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading event: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}