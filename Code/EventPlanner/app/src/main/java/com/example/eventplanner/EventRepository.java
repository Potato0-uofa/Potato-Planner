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


/**
 * Repository for CRUD and waitlist operations on Event documents in Firestore.
 * Used by admin and entrant control screens.
 *
 * Outstanding issues:
 * - Firestore integration tests are currently limited; most behavior validated by intent/manual flow.
 */
public class EventRepository {

    /** Name of the Firestore collection that stores event documents. */
    private static final String COLLECTION_EVENTS = "events";

    /** Firestore database instance used for all repository operations. */
    private final FirebaseFirestore db;

    /**
     * Callback for operations that return a list of {@link Events}.
     */
    public interface EventsCallback {

        /**
         * Called when the requested events are successfully retrieved.
         *
         * @param events list of retrieved events
         */
        void onSuccess(List<Events> events);

        /**
         * Called when the operation fails.
         *
         * @param e exception describing the failure
         */
        void onFailure(Exception e);
    }

    /**
     * Callback for operations that only need to report success or failure.
     */
    public interface SimpleCallback {

        /**
         * Called when the operation completes successfully.
         */
        void onSuccess();

        /**
         * Called when the operation fails.
         *
         * @param e exception describing the failure
         */
        void onFailure(Exception e);
    }

    /**
     * Callback for operations that return a single integer count.
     */
    public interface CountCallback {

        /**
         * Called when the count is successfully computed or retrieved.
         *
         * @param count resulting count value
         */
        void onSuccess(int count);

        /**
         * Called when the operation fails.
         *
         * @param e exception describing the failure
         */
        void onFailure(Exception e);
    }

    /**
     * Creates a new repository backed by the default Firestore instance.
     */
    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new event document in Firestore.
     * <p>
     * A new Firestore document ID is generated and assigned to the event before saving.
     * If the event does not already have a creation timestamp, the current timestamp is used.
     * If the event does not already have a status, the default status {@code "open"} is assigned.
     *
     * @param event event object to create
     * @param cb callback invoked on success or failure
     */
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

    /**
     * Fetches all events whose status is {@code "open"}.
     *
     * @param cb callback receiving the list of open events on success, or an exception on failure
     */
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

    /**
     * Replaces an existing event document in Firestore with the provided event data.
     *
     * @param event event object containing updated values; must include a non-empty event ID
     * @param cb callback invoked on success or failure
     */
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

    /**
     * Deletes an event document by id.
     *
     * @param eventId Firestore event document id
     * @param cb callback for success/failure
     */
    public void deleteEvent(@NonNull String eventId, @NonNull SimpleCallback cb) {
        db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .delete()
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Fetches all events from Firestore.
     * If an event does not contain an eventId field, the Firestore document id is used.
     *
     * @param cb callback receiving event list on success or exception on failure
     */
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

    /**
     * Adds a device or user identifier to an event's waiting list using Firestore array union.
     *
     * @param eventId ID of the event document
     * @param deviceId identifier to add to the waiting list
     * @param cb callback invoked on success or failure
     */
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
     * Removes a device or user identifier from an event's waiting list using Firestore array remove.
     *
     * @param eventId ID of the event document
     * @param userId identifier to remove from the waiting list
     * @param cb callback invoked on success or failure
     */
    public void leaveWaitingList(@NonNull String eventId, @NonNull String userId, @NonNull SimpleCallback cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("waitingList", FieldValue.arrayRemove(userId));

        db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Callback for checking whether a user or device is currently on a waiting list.
     */
    public interface WaitlistStatusCallback {

        /**
         * Called when the waitlist membership check completes successfully.
         *
         * @param isOnWaitlist {@code true} if the identifier is on the waiting list; {@code false} otherwise
         */
        void onSuccess(boolean isOnWaitlist);

        /**
         * Called when the operation fails.
         *
         * @param e exception describing the failure
         */
        void onFailure(Exception e);
    }

    /**
     * Checks whether a given user or device identifier is on an event's waiting list.
     *
     * @param eventId ID of the event document
     * @param userId identifier to check for membership in the waiting list
     * @param cb callback invoked with the result or an exception
     */
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

    /**
     * Registers a realtime listener that reports the current waiting list count for an event.
     *
     * @param eventId ID of the event document to observe
     * @param cb callback receiving updated waitlist counts or errors
     * @return listener registration that can be used to stop observing
     */
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

    /**
     * Registers a realtime listener that reports the current waiting list count for an event.
     * <p>
     * This method currently mirrors {@link #listenToWaitlistCount(String, CountCallback)}.
     *
     * @param eventId ID of the event document to observe
     * @param cb callback receiving updated waitlist counts or errors
     * @return listener registration that can be used to stop observing
     */
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