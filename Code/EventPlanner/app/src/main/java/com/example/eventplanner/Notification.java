package com.example.eventplanner;

/** Model representing a notification sent to a user about an event action. */
public class Notification {
    /** Unique identifier for this notification. */
    private String notificationId;
    /** The notification message body. */
    private String message;
    /** Display name of the notification sender. */
    private String senderName;
    /** Display name of the notification receiver. */
    private String receiverName;
    /** Name of the event this notification relates to. */
    private String eventName;
    /** Timestamp when the notification was created. */
    private String timestamp;
    /** Type of notification (e.g., invitation, update). */
    private String type;
    /** Current status of the notification. */
    private String status;
    /** Event ID this notification relates to. */
    private String eventId;
    /** Device ID of the organizer who sent the notification. */
    private String fromOrganizerId;

    /** No-argument constructor required for Firestore deserialization. */
    public Notification() {}

    /**
     * Constructs a Notification with the specified details.
     *
     * @param message      the notification message body
     * @param senderName   the sender's display name
     * @param receiverName the receiver's display name
     * @param eventName    the name of the related event
     * @param timestamp    the time the notification was sent
     * @param type         the notification type
     */
    public Notification(String message, String senderName, String receiverName,
                        String eventName, String timestamp, String type) {
        this.message = message;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.eventName = eventName;
        this.timestamp = timestamp;
        this.type = type;
    }

    /** @return the unique notification identifier */
    public String getNotificationId() { return notificationId; }
    /** @param notificationId the notification identifier to set */
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    /** @return the notification message body */
    public String getMessage() { return message; }

    /** @return the sender's display name */
    public String getSenderName() { return senderName; }

    /** @return the receiver's display name */
    public String getReceiverName() { return receiverName; }

    /** @return the name of the related event */
    public String getEventName() { return eventName; }

    /** @return the notification timestamp */
    public String getTimestamp() { return timestamp; }

    /** @return the notification type */
    public String getType() { return type; }

    /** @param message the message body to set */
    public void setMessage(String message) { this.message = message; }

    /** @param senderName the sender name to set */
    public void setSenderName(String senderName) { this.senderName = senderName; }

    /** @param receiverName the receiver name to set */
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    /** @param eventName the event name to set */
    public void setEventName(String eventName) { this.eventName = eventName; }

    /** @param timestamp the timestamp to set */
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    /** @param type the notification type to set */
    public void setType(String type) { this.type = type; }

    /** @return the notification status */
    public String getStatus() { return status; }
    /** @param status the status to set */
    public void setStatus(String status) { this.status = status; }

    /** @return the event ID this notification relates to */
    public String getEventId() { return eventId; }
    /** @param eventId the event ID to set */
    public void setEventId(String eventId) { this.eventId = eventId; }
}