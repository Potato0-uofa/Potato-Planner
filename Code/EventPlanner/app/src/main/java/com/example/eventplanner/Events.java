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
    private int capacity;
    private int waitlistLimit;
    private String status; // open, closed, cancelled
    private Timestamp createdAt;
    private WaitingList waitingList;

    public Events() {
        this.waitingList = new WaitingList();
        this.waitlistLimit = -1;
    }

    public Events(String name, String date, String description, String location) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.location = location;
        this.waitingList = new WaitingList();
        this.status = "open";
        this.waitlistLimit = -1;
    }

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
        this.waitlistLimit = -1;
    }

    // Waitlist limit getter and setter
    public int getWaitlistLimit() {
        return waitlistLimit;
    }

    public void setWaitlistLimit(int waitlistLimit) {
        this.waitlistLimit = waitlistLimit;
    }

    public boolean hasWaitlistLimit() {
        return waitlistLimit != -1;
    }

    public void addToWaitingList(Entrant entrant) {
        if (!waitingList.hasEntrant(entrant)) {
            if (hasWaitlistLimit() && waitingList.getCount() >= waitlistLimit) {
                throw new IllegalStateException("Waiting list is full");
            }
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

    public void addEntrantToWaitingList(Entrant entrant) {
        addToWaitingList(entrant);
    }

    public void removeEntrantFromWaitingList(Entrant entrant) {
        removeFromWaitingList(entrant);
    }

    public WaitingList getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(WaitingList waitingList) {
        this.waitingList = (waitingList != null) ? waitingList : new WaitingList();
    }

    public int getWaitingListCount() {
        return waitingList.getCount();
    }

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