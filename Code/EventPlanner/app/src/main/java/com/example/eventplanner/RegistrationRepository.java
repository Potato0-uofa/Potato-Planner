package com.example.eventplanner;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationRepository {

    private static final String COLLECTION_REGISTRATIONS = "registrations";
    private final FirebaseFirestore db;

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public RegistrationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void joinEvent(@NonNull String eventId, @NonNull String userId, @NonNull SimpleCallback cb) {

        String docId = eventId + "_" + userId;

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId);
        payload.put("userId", userId);
        payload.put("status", "pending"); // pending, accepted, waitlisted, rejected
        payload.put("joinedAt", Timestamp.now());

        db.collection(COLLECTION_REGISTRATIONS)
                .document(docId)
                .set(payload)
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    public void leaveEvent(@NonNull String eventId, @NonNull String userId, @NonNull SimpleCallback cb) {

        String docId = eventId + "_" + userId;

        db.collection(COLLECTION_REGISTRATIONS)
                .document(docId)
                .delete()
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }
}