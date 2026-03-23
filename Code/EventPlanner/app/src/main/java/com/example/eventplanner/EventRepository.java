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

/**
 * Repository for CRUD and waitlist operations on Event documents in Firestore.
 */
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

    public interface WaitlistStatusCallback {
        void onSuccess(boolean isOnWaitlist);
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
        db.collection(COLLECTION_EVENTS).document(id).set(event)
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
        db.collection(COLLECTION_EVENTS).whereEqualTo("status", "open").get()
                .addOnSuccessListener(snapshot -> {
                    List<Events> out = new ArrayList<>();
                    for (var doc : snapshot.getDocuments()) {
                        Events e = doc.toObject(Events.class);
                        if (e != null) {
                            if (e.getEventId() == null || e.getEventId().isEmpty()) {
                                e.setEventId(doc.getId());
                            }
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
        db.collection(COLLECTION_EVENTS).document(event.getEventId()).set(event)
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    public void deleteEvent(@NonNull String eventId, @NonNull SimpleCallback cb) {
        db.collection(COLLECTION_EVENTS).document(eventId).delete()
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    public void fetchAllEvents(@NonNull EventsCallback cb) {
        db.collection(COLLECTION_EVENTS).get()
                .addOnSuccessListener(snapshot -> {
                    List<Events> out = new ArrayList<>();
                    for (var doc : snapshot.getDocuments()) {
                        Events e = doc.toObject(Events.class);
                        if (e != null) {
                            if (e.getEventId() == null || e.getEventId().isEmpty()) {
                                e.setEventId(doc.getId());
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
        db.collection(COLLECTION_EVENTS).document(eventId).set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    public void leaveWaitingList(@NonNull String eventId, @NonNull String deviceId, @NonNull SimpleCallback cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("waitingList", FieldValue.arrayRemove(deviceId));
        db.collection(COLLECTION_EVENTS).document(eventId).set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    public void isOnWaitingList(@NonNull String eventId, @NonNull String deviceId, @NonNull WaitlistStatusCallback cb) {
        db.collection(COLLECTION_EVENTS).document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Object wlObj = snapshot.get("waitingList");
                        boolean isOnList = false;
                        if (wlObj instanceof List) {
                            isOnList = ((List<?>) wlObj).contains(deviceId);
                        }
                        cb.onSuccess(isOnList);
                    } else {
                        cb.onSuccess(false);
                    }
                })
                .addOnFailureListener(cb::onFailure);
    }

    public void fetchWaitlistEntrants(@NonNull String eventId, @NonNull EntrantsCallback cb) {
        db.collection(COLLECTION_EVENTS).document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            List<String> deviceIds = (List<String>) documentSnapshot.get("waitingList");
            if (deviceIds == null || deviceIds.isEmpty()) {
                cb.onSuccess(new ArrayList<>());
                return;
            }
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
        return db.collection(COLLECTION_EVENTS).document(eventId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        cb.onFailure(e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Object wlObj = snapshot.get("waitingList");
                        int count = 0;
                        if (wlObj instanceof List) {
                            count = ((List<?>) wlObj).size();
                        }
                        cb.onSuccess(count);
                    } else {
                        cb.onSuccess(0);
                    }
                });
    }

    public void addCoOrganizer(@NonNull String eventId, @NonNull String deviceId, @NonNull SimpleCallback cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("coOrganizerIds", FieldValue.arrayUnion(deviceId));
        db.collection(COLLECTION_EVENTS).document(eventId).set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }
}