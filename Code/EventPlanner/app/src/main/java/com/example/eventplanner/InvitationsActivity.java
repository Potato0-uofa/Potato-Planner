package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/** Activity displaying pending invitations for the current user with event name and organizer. */
public class InvitationsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<InvitationItem> adapter;
    private final List<InvitationItem> invitations = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;

    /** Simple holder for one invitation row. */
    private static class InvitationItem {
        String eventId;
        String eventName;
        String organizerName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        listView = findViewById(R.id.list_created_events);
        db = FirebaseFirestore.getInstance();
        userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        adapter = new ArrayAdapter<InvitationItem>(this, R.layout.item_invitation, invitations) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(R.layout.item_invitation, parent, false);
                }
                InvitationItem item = getItem(position);
                if (item != null) {
                    TextView tvName = convertView.findViewById(R.id.tv_created_event_name);
                    TextView tvOrg = convertView.findViewById(R.id.tv_invitation_organizer);
                    tvName.setText(item.eventName);
                    tvOrg.setText("From: " + item.organizerName);
                }
                return convertView;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(InvitationsActivity.this, NotificationLogs.class);
            intent.putExtra("eventId", invitations.get(position).eventId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInvitations();
    }

    private void loadInvitations() {
        db.collection("registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "invited")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    invitations.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No invitations found", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    int total = queryDocumentSnapshots.size();
                    final int[] loaded = {0};

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String eventId = doc.getString("eventId");
                        if (eventId == null) continue;

                        db.collection("events").document(eventId).get()
                                .addOnSuccessListener(eventDoc -> {
                                    String eventName = eventDoc.getString("name");
                                    if (eventName == null || eventName.isEmpty()) {
                                        eventName = "Unknown Event";
                                    }
                                    String organizerId = eventDoc.getString("organizerId");
                                    String finalEventName = eventName;

                                    if (organizerId != null && !organizerId.isEmpty()) {
                                        db.collection("users").document(organizerId).get()
                                                .addOnSuccessListener(userDoc -> {
                                                    String orgName = userDoc.getString("name");
                                                    if (orgName == null || orgName.isEmpty()) {
                                                        orgName = "Unknown Organizer";
                                                    }
                                                    addItem(eventId, finalEventName, orgName, loaded, total);
                                                })
                                                .addOnFailureListener(e2 ->
                                                        addItem(eventId, finalEventName, "Unknown Organizer", loaded, total));
                                    } else {
                                        addItem(eventId, finalEventName, "Unknown Organizer", loaded, total);
                                    }
                                })
                                .addOnFailureListener(e ->
                                        addItem(eventId, "Unknown Event", "Unknown Organizer", loaded, total));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load invitations", Toast.LENGTH_SHORT).show());
    }

    private void addItem(String eventId, String eventName, String organizerName,
                         int[] loaded, int total) {
        InvitationItem item = new InvitationItem();
        item.eventId = eventId;
        item.eventName = eventName;
        item.organizerName = organizerName;
        invitations.add(item);
        loaded[0]++;
        if (loaded[0] >= total) {
            adapter.notifyDataSetChanged();
        }
    }
}
