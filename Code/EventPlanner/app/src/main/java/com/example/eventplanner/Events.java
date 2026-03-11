package com.example.eventplanner;

import com.google.firebase.Timestamp;

/**
 * Represents an event in the event planner application.
 * Supports Firestore persistence + waiting list behavior.
 */
public class Events {

    private String eventId;
    private String organizerId;
    private String name;
    private String date;
    private String description;
    private String location;

    // Firebase/event management fields
    private int capacity;
    private String status; // open, closed, cancelled
    private Timestamp createdAt;

    // Existing app behavior
    private WaitingList waitingList;

    /**
     * Default constructor required for Firestore.
     */
    public Events() {
        this.waitingList = new WaitingList();
    }

    /**
     * Backward-compatible constructor used by existing UI/tests.
     */
    public Events(String name, String date, String description, String location) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.location = location;
        this.waitingList = new WaitingList();
        this.status = "open";
    }

    /**
     * Extended constructor for Firebase-backed event data.
     */
    public Events(String eventId, String organizerId, String name, String date,
                  String description, String location, int capacity,
                  String status, Timestamp createdAt) {

        this.eventId = eventId;
        this.organizerId = organizerId;
        this.name = name;
        this.date = date;
        this.description = description;
        this.location = location;
        this.capacity = capacity;
        this.status = status;
        this.createdAt = createdAt;
        this.waitingList = new WaitingList();
    }

    // Waiting list behavior

    public void addToWaitingList(Entrant entrant) {
        if (!waitingList.hasEntrant(entrant)) {
            waitingList.addEntrant(entrant);
        }
    }

    public void removeFromWaitingList(Entrant entrant) {
        if (waitingList.hasEntrant(entrant)) {
            waitingList.deleteEntrant(entrant);
        }
    }

    public boolean isOnWaitingList(Entrant entrant) {
        return waitingList.hasEntrant(entrant);
    }

    public WaitingList getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(WaitingList waitingList) {
        this.waitingList = (waitingList != null) ? waitingList : new WaitingList();
    }

    // Getters and setters

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Backward compatibility for old typo
    public String getLoction() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}