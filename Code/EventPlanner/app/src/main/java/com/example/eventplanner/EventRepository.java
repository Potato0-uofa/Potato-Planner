package com.example.eventplanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventRepository {

    private static final String COLLECTION_EVENTS = "events";
    private static final String COLLECTION_USERS = "users";
    private final FirebaseFirestore db;

    public interface EventsCallback {
        void onSuccess(List<Events> events);
        void onFailure(Exception e);
    }

    public interface EventCallback {
        void onSuccess(Events event);
        void onFailure(Exception e);
    }

    public interface EntrantsCallback {
        void onSuccess(List<Entrant> entrants);
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

    public void fetchEventById(@NonNull String eventId, @NonNull EventCallback cb) {
        db.collection(COLLECTION_EVENTS).document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Events event = documentSnapshot.toObject(Events.class);
                        if (event != null && (event.getEventId() == null || event.getEventId().isEmpty())) {
                            event.setEventId(documentSnapshot.getId());
                        }
                        cb.onSuccess(event);
                    } else {
                        cb.onFailure(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(cb::onFailure);
    }

    public void fetchOpenEvents(@NonNull EventsCallback cb) {
        db.collection(COLLECTION_EVENTS)
                .whereEqualTo("status", "open")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Events> out = new ArrayList<>();

                    for (var doc : snapshot.getDocuments()) {
                        try {
                            Events e = doc.toObject(Events.class);
                            if (e != null) {
                                if (e.getEventId() == null || e.getEventId().isEmpty()) {
                                    e.setEventId(doc.getId());
                                }
                                out.add(e);
                            }
                        } catch (Exception e) {
                            // Skip documents that fail to deserialize due to schema mismatch
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
                        try {
                            Events e = doc.toObject(Events.class);
                            if (e != null) {
                                if (e.getEventId() == null || e.getEventId().isEmpty()) {
                                    e.setEventId(doc.getId());
                                }
                                out.add(e);
                            }
                        } catch (Exception e) {
                            // Skip documents that fail to deserialize
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

    /**
     * Fetches the detailed information of all entrants on the waiting list for a specific event.
     *
     * @param eventId the ID of the event
     * @param cb      callback to return the list of Entrants
     */
    public void fetchWaitlistEntrants(@NonNull String eventId, @NonNull EntrantsCallback cb) {
        db.collection(COLLECTION_EVENTS).document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            List<String> deviceIds = (List<String>) documentSnapshot.get("waitingList");
            if (deviceIds == null || deviceIds.isEmpty()) {
                cb.onSuccess(new ArrayList<>());
                return;
            }

            // Firebase 'whereIn' is limited to 10 items per query.
            // For larger lists, we need to split into multiple queries or fetch individually.
            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
            for (String deviceId : deviceIds) {
                tasks.add(db.collection(COLLECTION_USERS).document(deviceId).get());
            }

            Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> {
                List<Entrant> entrants = new ArrayList<>();
                for (Task<DocumentSnapshot> task : tasks) {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Entrant entrant = task.getResult().toObject(Entrant.class);
                        if (entrant != null) {
                            entrants.add(entrant);
                        }
                    }
                }
                cb.onSuccess(entrants);
            });
        }).addOnFailureListener(cb::onFailure);
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
                            Object wl = snapshot.get("waitingList");
                            if (wl instanceof List) {
                                List<?> waitingList = (List<?>) wl;
                                cb.onSuccess(waitingList.size());
                            } else {
                                cb.onSuccess(0);
                            }
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
                            Object wl = snapshot.get("waitingList");
                            if (wl instanceof List) {
                                List<?> waitingList = (List<?>) wl;
                                cb.onSuccess(waitingList.size());
                            } else {
                                cb.onSuccess(0);
                            }
                        } else {
                            cb.onSuccess(0);
                        }
                    }
                });
    }
}