package com.example.eventplanner;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Repository for event registration records in Firestore
 * Supports join, leave, and invitation response flows.
 */
public class RegistrationRepository {

    /** Name of the Firestore collection that stores registration documents. */
    private static final String COLLECTION_REGISTRATIONS = "registrations";

    /** Firestore database instance used for registration operations. */
    private final FirebaseFirestore db;

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
     * Creates a new repository backed by the default Firestore instance.
     */
    public RegistrationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates or replaces a registration record for a user joining an event.
     * <p>
     * The registration document ID is composed as {@code eventId + "_" + userId}.
     * A newly joined registration is stored with status {@code "pending"} and the
     * current timestamp as {@code joinedAt}.
     *
     * @param eventId identifier of the event being joined
     * @param userId identifier of the user or device joining the event
     * @param cb callback invoked on success or failure
     */
    public void joinEvent(@NonNull String eventId, @NonNull String userId, @NonNull SimpleCallback cb) {

        String docId = eventId + "_" + userId;

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId);
        payload.put("userId", userId);
        payload.put("status", "waitlisted"); // waitlisted, accepted, declined, rejected
        payload.put("joinedAt", Timestamp.now());

        db.collection(COLLECTION_REGISTRATIONS)
                .document(docId)
                .set(payload)
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Deletes a registration record for a user leaving an event.
     *
     * @param eventId identifier of the event being left
     * @param userId identifier of the user or device leaving the event
     * @param cb callback invoked on success or failure
     */
    public void leaveEvent(@NonNull String eventId, @NonNull String userId, @NonNull SimpleCallback cb) {

        String docId = eventId + "_" + userId;

        db.collection(COLLECTION_REGISTRATIONS)
                .document(docId)
                .delete()
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Marks a registration or invitation as declined for a given event and user.
     * <p>
     * This uses Firestore merge semantics so the document is updated if it exists,
     * or created if it does not.
     *
     * @param eventId event identifier
     * @param userId user or device identifier
     * @param cb callback for success or failure
     */
    public void declineInvitation(@NonNull String eventId, @NonNull String userId, @NonNull SimpleCallback cb) {
        String docId = eventId + "_" + userId;

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId);
        payload.put("userId", userId);
        payload.put("status", "declined");
        payload.put("updatedAt", Timestamp.now());

        db.collection(COLLECTION_REGISTRATIONS)
                .document(docId)
                .set(payload, SetOptions.merge()) // works even if doc doesn't exist yet
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }


    public void acceptInvitation(@NonNull String eventId, @NonNull String userId, @NonNull SimpleCallback cb) {
        String docId = eventId + "_" + userId;

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId);
        payload.put("userId", userId);
        payload.put("status", "accepted");
        payload.put("updatedAt", Timestamp.now());

        db.collection(COLLECTION_REGISTRATIONS)
                .document(docId)
                .set(payload, SetOptions.merge())
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }


    /**
     * Updates the status of an event registration/invitation for a specific user.
     */
    public void updateInvitationStatus(@NonNull String eventId,
                                       @NonNull String userId,
                                       @NonNull String status,
                                       @NonNull SimpleCallback cb) {
        String docId = eventId + "_" + userId;
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId);
        payload.put("userId", userId);
        payload.put("status", status);
        payload.put("updatedAt", Timestamp.now());

        db.collection(COLLECTION_REGISTRATIONS)
                .document(docId)
                .set(payload, SetOptions.merge())
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }


    public void inviteUserToEvent(@NonNull String eventId, @NonNull String userId, @NonNull SimpleCallback cb) {
        String docId = eventId + "_" + userId;
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId);
        payload.put("userId", userId);
        payload.put("status", "invited");
        payload.put("lastNoticeType", "lottery_win");
        payload.put("lastNoticeMessage", "You won the lottery and were invited to sign up.");
        payload.put("updatedAt", Timestamp.now());

        db.collection(COLLECTION_REGISTRATIONS)
                .document(docId)
                .set(payload, SetOptions.merge())
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /** Invites a user to join a private event waiting list. */
    public void inviteUserToPrivateWaitlist(@NonNull String eventId,
                                            @NonNull String userId,
                                            @NonNull SimpleCallback cb) {
        String docId = eventId + "_" + userId;
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId);
        payload.put("userId", userId);
        payload.put("status", "private_waitlist_invited");
        payload.put("lastNoticeType", "private_waitlist_invite");
        payload.put("lastNoticeMessage", "You were invited to join the waiting list for this private event.");
        payload.put("updatedAt", Timestamp.now());

        db.collection(COLLECTION_REGISTRATIONS)
                .document(docId)
                .set(payload, SetOptions.merge())
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /** Invites a user to be a co-organizer for an event. */
    public void inviteUserToCoOrganizer(@NonNull String eventId,
                                        @NonNull String userId,
                                        @NonNull SimpleCallback cb) {
        String docId = eventId + "_" + userId;
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId);
        payload.put("userId", userId);
        payload.put("status", "coorganizer_invited");
        payload.put("lastNoticeType", "coorganizer_invite");
        payload.put("lastNoticeMessage", "You were invited to be a co-organizer for this event.");
        payload.put("updatedAt", Timestamp.now());

        db.collection(COLLECTION_REGISTRATIONS)
                .document(docId)
                .set(payload, SetOptions.merge())
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Sends the same in-app notice to a list of users while preserving their current status.
     */
    public void sendNoticeToUsers(@NonNull String eventId,
                                  @NonNull List<String> userIds,
                                  @NonNull String noticeType,
                                  @NonNull String message,
                                  @NonNull SimpleCallback cb) {
        if (userIds.isEmpty()) {
            cb.onSuccess();
            return;
        }

        WriteBatch batch = db.batch();
        Timestamp now = Timestamp.now();
        for (String userId : userIds) {
            if (userId == null || userId.trim().isEmpty()) continue;

            String docId = eventId + "_" + userId;
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", eventId);
            payload.put("userId", userId);
            payload.put("lastNoticeType", noticeType);
            payload.put("lastNoticeMessage", message);
            payload.put("updatedAt", now);

            batch.set(
                    db.collection(COLLECTION_REGISTRATIONS).document(docId),
                    payload,
                    SetOptions.merge()
            );
        }

        batch.commit()
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Marks a registration as cancelled by the organizer (entrant did not sign up).
     */
    public void cancelRegistration(@NonNull String eventId, @NonNull String userId, @NonNull SimpleCallback cb) {
        String docId = eventId + "_" + userId;

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId);
        payload.put("userId", userId);
        payload.put("status", "cancelled");
        payload.put("updatedAt", Timestamp.now());

        db.collection(COLLECTION_REGISTRATIONS)
                .document(docId)
                .set(payload, SetOptions.merge())
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }
}