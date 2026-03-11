package com.example.eventplanner;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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




}