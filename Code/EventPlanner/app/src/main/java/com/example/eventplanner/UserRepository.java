package com.example.eventplanner;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {

    private static final String COLLECTION_USERS = "users";
    private final FirebaseFirestore db;

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

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

    public void upsertUser(@NonNull User user, @NonNull SimpleCallback cb) {
        db.collection(COLLECTION_USERS)
                .document(user.getDeviceId())
                .set(user)
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }
}