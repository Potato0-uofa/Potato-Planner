package com.example.eventplanner;

import com.google.firebase.Timestamp;

/**
 * Represents a single comment on an event.
 * Stored in Firestore at: events/{eventId}/comments/{commentId}
 */
public class Comment {

    private String commentId;
    private String deviceId;   // author's device ID
    private String authorName; // display name of the author
    private String text;
    private Timestamp createdAt;

    /** Required no-arg constructor for Firestore deserialization. */
    public Comment() {}

    public Comment(String commentId, String deviceId, String authorName,
                   String text, Timestamp createdAt) {
        this.commentId  = commentId;
        this.deviceId   = deviceId;
        this.authorName = authorName;
        this.text       = text;
        this.createdAt  = createdAt;
    }

    public String getCommentId()  { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getDeviceId()   { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getText()       { return text; }
    public void setText(String text) { this.text = text; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}