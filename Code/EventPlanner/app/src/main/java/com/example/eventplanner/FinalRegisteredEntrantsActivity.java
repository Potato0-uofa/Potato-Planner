package com.example.eventplanner;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
public class FinalRegisteredEntrantsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WaitlistAdapter adapter;
    private final List<Entrant> acceptedEntrants = new ArrayList<>();
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chosen_entrants);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Missing eventId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recycler_chosen_entrants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaitlistAdapter(acceptedEntrants);
        recyclerView.setAdapter(adapter);

        loadAcceptedEntrants();

        findViewById(R.id.btn_back_chosen).setOnClickListener(v -> finish());
    }

    private void loadAcceptedEntrants() {
        db.collection("registrations")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(querySnapshot -> fetchUsers(querySnapshot))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load accepted entrants", Toast.LENGTH_SHORT).show());
    }

    private void fetchUsers(QuerySnapshot querySnapshot) {
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            String userId = doc.getString("userId");
            if (userId != null && !userId.isEmpty()) {
                tasks.add(db.collection("users").document(userId).get());
            }
        }

        if (tasks.isEmpty()) {
            Toast.makeText(this, "No accepted entrants found", Toast.LENGTH_SHORT).show();
            return;
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            acceptedEntrants.clear();
            for (Object obj : results) {
                DocumentSnapshot userDoc = (DocumentSnapshot) obj;
                if (userDoc.exists()) {
                    Entrant entrant = userDoc.toObject(Entrant.class);
                    if (entrant != null) {
                        entrant.setDeviceId(userDoc.getId());
                        acceptedEntrants.add(entrant);
                    }
                }
            }

            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load user details", Toast.LENGTH_SHORT).show());
    }
}
