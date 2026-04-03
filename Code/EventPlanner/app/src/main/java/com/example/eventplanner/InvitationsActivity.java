package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InvitationsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private final List<String> invitationTitles = new ArrayList<>();
    private final List<String> invitationEventIds = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        listView = findViewById(R.id.list_created_events);
        db = FirebaseFirestore.getInstance();
        userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, invitationTitles);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(InvitationsActivity.this, NotificationLogs.class);
            intent.putExtra("eventId", invitationEventIds.get(position));
            startActivity(intent);
        });

        loadInvitations();
    }

    private void loadInvitations() {
        db.collection("registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "invited")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    invitationTitles.clear();
                    invitationEventIds.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No invitations found", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String eventId = doc.getString("eventId");
                        if (eventId != null) {
                            invitationEventIds.add(eventId);
                            invitationTitles.add("Invitation for event: " + eventId);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load invitations", Toast.LENGTH_SHORT).show());
    }
}