package com.example.eventplanner;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for CRUD operations on event comments.
 *
 * Firestore path: events/{eventId}/comments/{commentId}
 *
 * - addComment()       — writes a new comment document
 * - deleteComment()    — deletes a comment by ID (only call if deviceId matches)
 * - listenToComments() — real-time listener ordered by createdAt ascending
 */
public class CommentRepository {

    private static final String COLLECTION_EVENTS   = "events";
    private static final String COLLECTION_COMMENTS = "comments";

    private final FirebaseFirestore db;

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface CommentsCallback {
        void onUpdate(List<Comment> comments);
        void onFailure(Exception e);
    }

    public CommentRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Adds a new comment to the event's comments subcollection.
     *
     * @param eventId    the event to comment on
     * @param deviceId   the commenter's device ID
     * @param authorName display name shown next to the comment
     * @param text       the comment body
     * @param cb         success/failure callback
     */
    public void addComment(@NonNull String eventId,
                           @NonNull String deviceId,
                           @NonNull String authorName,
                           @NonNull String text,
                           @NonNull SimpleCallback cb) {
        String commentId = db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .collection(COLLECTION_COMMENTS)
                .document()
                .getId();

        Comment comment = new Comment(commentId, deviceId, authorName, text, Timestamp.now());

        db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .collection(COLLECTION_COMMENTS)
                .document(commentId)
                .set(comment)
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Deletes a comment. Always verify the caller's deviceId matches
     * comment.getDeviceId() before calling this.
     *
     * @param eventId   the event the comment belongs to
     * @param commentId the comment to delete
     * @param cb        success/failure callback
     */
    public void deleteComment(@NonNull String eventId,
                              @NonNull String commentId,
                              @NonNull SimpleCallback cb) {
        db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .collection(COLLECTION_COMMENTS)
                .document(commentId)
                .delete()
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onFailure);
    }

    /**
     * Attaches a real-time listener to the comments subcollection,
     * ordered oldest-first. Fires immediately and on every change.
     *
     * Store the returned {@link ListenerRegistration} and call
     * {@code registration.remove()} in {@code onDestroy()} to avoid leaks.
     *
     * @param eventId the event to listen to
     * @param cb      callback that receives the updated comment list
     * @return a ListenerRegistration to remove when done
     */
    public ListenerRegistration listenToComments(@NonNull String eventId,
                                                 @NonNull CommentsCallback cb) {
        return db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .collection(COLLECTION_COMMENTS)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        cb.onFailure(error);
                        return;
                    }
                    List<Comment> comments = new ArrayList<>();
                    if (snapshot != null) {
                        for (var doc : snapshot.getDocuments()) {
                            Comment c = doc.toObject(Comment.class);
                            if (c != null) comments.add(c);
                        }
                    }
                    cb.onUpdate(comments);
                });
    }
}