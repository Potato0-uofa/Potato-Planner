package com.example.eventplanner;

/** Model representing a notification sent to a user about an event action. */
public class Notification {
    private String message;
    private String senderName;
    private String receiverName;
    private String eventName;
    private String timestamp;
    private String type;

    public Notification() {}

    public Notification(String message, String senderName, String receiverName,
                        String eventName, String timestamp, String type) {
        this.message = message;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.eventName = eventName;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getEventName() {
        return eventName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(String type) {
        this.type = type;
    }
}