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


    public void fetchAllUsers(@NonNull UsersCallback cb) {
        db.collection(COLLECTION_USERS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> out = new ArrayList<>();
                    for (var doc : snapshot.getDocuments()) {
                        User u = doc.toObject(User.class);
                        if (u != null) out.add(u);
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onFailure);
    }

}