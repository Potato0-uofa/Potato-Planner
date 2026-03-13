package com.example.eventplanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventRepository {

    private static final String COLLECTION_EVENTS = "events";
    private final FirebaseFirestore db;

    public interface EventsCallback {
        void onSuccess(List<Events> events);
        void onFailure(Exception e);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface CountCallback {
        void onSuccess(int count);
        void onFailure(Exception e);
    }

    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void createEvent(@NonNull Events event, @NonNull SimpleCallback cb) {
        String id = db.collection(COLLECTION_EVENTS).document().getId();
        event.setEventId(id);

        if (event.getCreatedAt() == null) {
            event.setCreatedAt(Timestamp.now());
        }

        if (event.getStatus() == null || event.getStatus().isEmpty()) {
            event.setStatus("open");
        }

        db.collection(COLLECTION_EVENTS)
                .document(id)
                .set(event)
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    public void fetchOpenEvents(@NonNull EventsCallback cb) {
        db.collection(COLLECTION_EVENTS)
                .whereEqualTo("status", "open")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Events> out = new ArrayList<>();

                    for (var doc : snapshot.getDocuments()) {
                        Events e = doc.toObject(Events.class);
                        if (e != null) {
                            out.add(e);
                        }
                    }

                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onFailure);
    }

    public void updateEvent(@NonNull Events event, @NonNull SimpleCallback cb) {

        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            cb.onFailure(new IllegalArgumentException("eventId is required"));
            return;
        }

        db.collection(COLLECTION_EVENTS)
                .document(event.getEventId())
                .set(event)
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    public void deleteEvent(@NonNull String eventId, @NonNull SimpleCallback cb) {
        db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .delete()
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    public void fetchAllEvents(@NonNull EventsCallback cb) {
        db.collection(COLLECTION_EVENTS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Events> out = new ArrayList<>();
                    for (var doc : snapshot.getDocuments()) {
                        Events e = doc.toObject(Events.class);
                        if (e != null) {
                            if (e.getEventId() == null || e.getEventId().isEmpty()) {
                                e.setEventId(doc.getId()); // critical fix
                            }
                            out.add(e);
                        }
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onFailure);
    }

    public void joinWaitingList(@NonNull String eventId, @NonNull String deviceId, @NonNull SimpleCallback cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("waitingList", FieldValue.arrayUnion(deviceId));

        db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    public void leaveWaitingList(@NonNull String eventId, @NonNull String userId, @NonNull SimpleCallback cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("waitingList", FieldValue.arrayRemove(userId));

        db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    public interface WaitlistStatusCallback {
        void onSuccess(boolean isOnWaitlist);
        void onFailure(Exception e);
    }

    public void isOnWaitingList(@NonNull String eventId, @NonNull String userId, @NonNull WaitlistStatusCallback cb) {
        db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        List<String> waitingList = (List<String>) snapshot.get("waitingList");
                        boolean isOnList = waitingList != null && waitingList.contains(userId);
                        cb.onSuccess(isOnList);
                    } else {
                        cb.onSuccess(false);
                    }
                })
                .addOnFailureListener(cb::onFailure);
    }

    public ListenerRegistration listenToWaitlistCount(@NonNull String eventId, @NonNull CountCallback cb) {
        return db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            cb.onFailure(e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            List<String> waitingList = (List<String>) snapshot.get("waitingList");
                            int count = (waitingList != null) ? waitingList.size() : 0;
                            cb.onSuccess(count);
                        } else {
                            cb.onSuccess(0);
                        }
                    }
                });
    }

    public ListenerRegistration observeWaitingListCount(@NonNull String eventId, @NonNull CountCallback cb) {
        return db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            cb.onFailure(e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            List<String> waitingList = (List<String>) snapshot.get("waitingList");
                            int count = (waitingList != null) ? waitingList.size() : 0;
                            cb.onSuccess(count);
                        } else {
                            cb.onSuccess(0);
                        }
                    }
                });
    }
}