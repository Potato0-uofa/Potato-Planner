package com.example.eventplanner;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for reading and writing attendee location data.
 *
 * Firestore path: events/{eventId}/attendee_locations/{deviceId}
 *
 * Call {@link #updateLocation} whenever the device's position changes.
 * Call {@link #removeLocation} when the attendee leaves the event or stops tracking.
 * Call {@link #listenToLocations} from the organizer dashboard to observe all attendee positions.
 */
public class LocationRepository {

    private static final String COLLECTION_EVENTS = "events";
    private static final String COLLECTION_LOCATIONS = "attendee_locations";

    private final FirebaseFirestore db;

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface LocationsCallback {
        void onUpdate(List<EntrantLocation> locations);
        void onFailure(Exception e);
    }

    public LocationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Writes or updates the caller's location for the given event.
     * Safe to call on every FusedLocationProvider update — uses set() with merge
     * so it creates the document if missing and overwrites if present.
     *
     * @param eventId  the event to associate the location with
     * @param deviceId the attendee's device ID (used as the document ID)
     * @param lat      current latitude
     * @param lng      current longitude
     * @param cb       success/failure callback
     */
    public void updateLocation(@NonNull String eventId,
                               @NonNull String deviceId,
                               double lat,
                               double lng,
                               @NonNull SimpleCallback cb) {
        EntrantLocation location = new EntrantLocation(deviceId, lat, lng, Timestamp.now());
        db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .collection(COLLECTION_LOCATIONS)
                .document(deviceId)
                .set(location)
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Removes the attendee's location document from the subcollection.
     * Should be called when the user leaves the waitlist, the event ends,
     * or the user explicitly revokes location sharing.
     *
     * @param eventId  the event the attendee belongs to
     * @param deviceId the attendee's device ID
     * @param cb       success/failure callback
     */
    public void removeLocation(@NonNull String eventId,
                               @NonNull String deviceId,
                               @NonNull SimpleCallback cb) {
        db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .collection(COLLECTION_LOCATIONS)
                .document(deviceId)
                .delete()
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Attaches a real-time listener to the attendee_locations subcollection.
     * Fires immediately with the current snapshot and again on every change.
     * Intended for use by the organizer dashboard.
     *
     * Store the returned {@link ListenerRegistration} and call
     * {@code registration.remove()} in {@code onDestroy()} to avoid leaks.
     *
     * @param eventId the event to observe
     * @param cb      callback that receives the full updated list of locations
     * @return a {@link ListenerRegistration} that must be removed when done
     */
    public ListenerRegistration listenToLocations(@NonNull String eventId,
                                                  @NonNull LocationsCallback cb) {
        return db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .collection(COLLECTION_LOCATIONS)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        cb.onFailure(error);
                        return;
                    }
                    List<EntrantLocation> locations = new ArrayList<>();
                    if (snapshot != null) {
                        for (var doc : snapshot.getDocuments()) {
                            EntrantLocation loc = doc.toObject(EntrantLocation.class);
                            if (loc != null) locations.add(loc);
                        }
                    }
                    cb.onUpdate(locations);
                });
    }
}