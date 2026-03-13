package com.example.eventplanner;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

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

    public interface UsersCallback {
        void onSuccess(List<User> users);
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
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            // Ensure the deviceId is populated from the document ID if missing in fields
                            if (user.getDeviceId() == null || user.getDeviceId().isEmpty()) {
                                user.setDeviceId(doc.getId());
                            }
                        }
                        cb.onSuccess(user);
                    } else {
                        cb.onSuccess(null);
                    }
                })
                .addOnFailureListener(cb::onFailure);
    }

    public void upsertUser(@NonNull User user, @NonNull SimpleCallback cb) {
        if (user.getDeviceId() == null || user.getDeviceId().isEmpty()) {
            cb.onFailure(new Exception("Cannot save user: Device ID is missing."));
            return;
        }
        db.collection(COLLECTION_USERS)
                .document(user.getDeviceId())
                .set(user)
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    public void fetchAllUsers(@NonNull UsersCallback cb) {
        db.collection(COLLECTION_USERS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> out = new ArrayList<>();
                    for (var doc : snapshot.getDocuments()) {
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            if (u.getDeviceId() == null || u.getDeviceId().isEmpty()) {
                                u.setDeviceId(doc.getId());
                            }
                            out.add(u);
                        }
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onFailure);
    }

    public void deleteUser(@NonNull String deviceId, @NonNull SimpleCallback cb) {
        db.collection(COLLECTION_USERS)
                .document(deviceId)
                .delete()
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }
}