package com.example.eventplanner;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for user profile persistence in Firestore.
 * Supports bootstrap, profile upsert, browse-all, and delete operations.
 */
public class UserRepository {

    /** Name of the Firestore collection that stores user documents. */
    private static final String COLLECTION_USERS = "users";

    /** Firestore database instance used for repository operations. */
    private final FirebaseFirestore db;

    /**
     * Callback for operations that return a single {@link User}.
     */
    public interface UserCallback {

        /**
         * Called when a user lookup completes successfully.
         *
         * @param user the retrieved user, or {@code null} if no matching document exists
         */
        void onSuccess(User user);

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
     * Callback for operations that return multiple {@link User} objects.
     */
    public interface UsersCallback {

        /**
         * Called when the user list is retrieved successfully.
         *
         * @param users list of retrieved users
         */
        void onSuccess(List<User> users);

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
    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieves a user profile by device ID.
     *
     * @param deviceId unique device identifier used as the Firestore document ID
     * @param cb callback invoked with the user if found, {@code null} if not found, or an error on failure
     */
    public void getUserByDeviceId(@NonNull String deviceId, @NonNull UserCallback cb) {
        db.collection(COLLECTION_USERS)
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        cb.onSuccess(doc.toObject(User.class));
                    } else {
                        cb.onSuccess(null);
                    }
                })
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Creates or replaces a user profile document in Firestore.
     *
     * @param user user profile to save; its device ID is used as the Firestore document ID
     * @param cb callback invoked on success or failure
     */
    public void upsertUser(@NonNull User user, @NonNull SimpleCallback cb) {
        db.collection(COLLECTION_USERS)
                .document(user.getDeviceId())
                .set(user)
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Fetches all user profiles from Firestore.
     *
     * @param cb callback receiving all users or error
     */
    public void fetchAllUsers(@NonNull UsersCallback cb) {
        db.collection(COLLECTION_USERS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> out = new ArrayList<>();
                    for (var doc : snapshot.getDocuments()) {
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            out.add(u);
                        }
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Deletes a user profile by device id.
     *
     * @param deviceId Firestore user document id
     * @param cb callback for success/failure
     */
    public void deleteUser(@NonNull String deviceId, @NonNull SimpleCallback cb) {
        db.collection(COLLECTION_USERS)
                .document(deviceId)
                .delete()
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }
}