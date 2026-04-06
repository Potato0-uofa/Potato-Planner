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

    /**
     * Constructs a Comment with all fields specified.
     *
     * @param commentId  unique identifier for this comment
     * @param deviceId   the author's device ID
     * @param authorName display name of the author
     * @param text       the comment body text
     * @param createdAt  timestamp when the comment was created
     */
    public Comment(String commentId, String deviceId, String authorName,
                   String text, Timestamp createdAt) {
        this.commentId  = commentId;
        this.deviceId   = deviceId;
        this.authorName = authorName;
        this.text       = text;
        this.createdAt  = createdAt;
    }

    /** @return the unique comment identifier */
    public String getCommentId()  { return commentId; }
    /** @param commentId the comment identifier to set */
    public void setCommentId(String commentId) { this.commentId = commentId; }

    /** @return the author's device ID */
    public String getDeviceId()   { return deviceId; }
    /** @param deviceId the author's device ID to set */
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    /** @return the display name of the comment author */
    public String getAuthorName() { return authorName; }
    /** @param authorName the author display name to set */
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    /** @return the comment body text */
    public String getText()       { return text; }
    /** @param text the comment body text to set */
    public void setText(String text) { this.text = text; }

    /** @return the timestamp when the comment was created */
    public Timestamp getCreatedAt() { return createdAt; }
    /** @param createdAt the creation timestamp to set */
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}