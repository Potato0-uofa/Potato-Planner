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
import java.util.Arrays;
import java.util.List;

/** Activity displaying pending invitations for the current user with event name and organizer. */
public class InvitationsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<InvitationItem> adapter;
    private final List<InvitationItem> invitations = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;
    private static final List<String> ACTIONABLE_STATUSES = Arrays.asList(
            "invited",
            "private_waitlist_invited",
            "coorganizer_invited"
    );

    /** Simple holder for one invitation row. */
    private static class InvitationItem {
        String eventId;
        String eventName;
        String organizerName;
        String status;
        String noticeType;
        String noticeMessage;
        boolean actionable;
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
                    String statusLabel = item.status != null ? item.status.replace('_', ' ') : "notification";
                    String subtext = item.noticeMessage != null && !item.noticeMessage.isEmpty()
                            ? item.noticeMessage
                            : ("From: " + item.organizerName + " (" + statusLabel + ")");
                    tvOrg.setText(subtext);
                }
                return convertView;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            InvitationItem selected = invitations.get(position);
            Intent intent = new Intent(InvitationsActivity.this, NotificationLogs.class);
            intent.putExtra("eventId", selected.eventId);
            intent.putExtra("status", selected.status);
            intent.putExtra("noticeType", selected.noticeType);
            intent.putExtra("noticeMessage", selected.noticeMessage);
            intent.putExtra("actionable", selected.actionable);
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
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    invitations.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No invitations found", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    List<QueryDocumentSnapshot> relevantDocs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");
                        String noticeMessage = doc.getString("lastNoticeMessage");
                        boolean actionable = status != null && ACTIONABLE_STATUSES.contains(status);
                        boolean hasNotice = noticeMessage != null && !noticeMessage.trim().isEmpty();
                        if (actionable || hasNotice) {
                            relevantDocs.add(doc);
                        }
                    }

                    if (relevantDocs.isEmpty()) {
                        Toast.makeText(this, "No invitations found", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    int total = relevantDocs.size();
                    final int[] loaded = {0};

                    for (QueryDocumentSnapshot doc : relevantDocs) {
                        String eventId = doc.getString("eventId");
                        if (eventId == null || eventId.trim().isEmpty()) {
                            loaded[0]++;
                            if (loaded[0] >= total) adapter.notifyDataSetChanged();
                            continue;
                        }

                        String status = doc.getString("status");
                        String noticeType = doc.getString("lastNoticeType");
                        String noticeMessage = doc.getString("lastNoticeMessage");
                        boolean actionable = status != null && ACTIONABLE_STATUSES.contains(status);

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
                                                    addItem(eventId, finalEventName, orgName, status,
                                                            noticeType, noticeMessage, actionable,
                                                            loaded, total);
                                                })
                                                .addOnFailureListener(e2 ->
                                                        addItem(eventId, finalEventName, "Unknown Organizer",
                                                                status, noticeType, noticeMessage, actionable,
                                                                loaded, total));
                                    } else {
                                        addItem(eventId, finalEventName, "Unknown Organizer",
                                                status, noticeType, noticeMessage, actionable,
                                                loaded, total);
                                    }
                                })
                                .addOnFailureListener(e ->
                                        addItem(eventId, "Unknown Event", "Unknown Organizer",
                                                status, noticeType, noticeMessage, actionable,
                                                loaded, total));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load invitations", Toast.LENGTH_SHORT).show());
    }

    private void addItem(String eventId, String eventName, String organizerName,
                         String status, String noticeType, String noticeMessage, boolean actionable,
                         int[] loaded, int total) {
        InvitationItem item = new InvitationItem();
        item.eventId = eventId;
        item.eventName = eventName;
        item.organizerName = organizerName;
        item.status = status;
        item.noticeType = noticeType;
        item.noticeMessage = noticeMessage;
        item.actionable = actionable;
        invitations.add(item);
        loaded[0]++;
        if (loaded[0] >= total) {
            adapter.notifyDataSetChanged();
        }
    }
}
