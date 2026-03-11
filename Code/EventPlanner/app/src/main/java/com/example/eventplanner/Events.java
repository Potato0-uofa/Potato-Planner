package com.example.eventplanner;

public class Events {
    // attributes of the events
    private String name;
    private String date;
    private String description;
    private String location;
    private WaitingList waitingList;

    // Required for Firestore
    public Events() {
        this.waitingList = new WaitingList();
    }

    // constructor
    public Events(String name, String date, String description, String location) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.location = location;
        this.waitingList = new WaitingList();
    }

    // getters
    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public WaitingList getWaitingList() {
        return waitingList;
    }

    public int getWaitingListCount() {
        return waitingList.getCount();
    }

    // setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setWaitingList(WaitingList waitingList) {
        this.waitingList = waitingList;
    }

    // helper methods
    public void addEntrantToWaitingList(Entrant entrant) {
        waitingList.addEntrant(entrant);
    }

    public void removeEntrantFromWaitingList(Entrant entrant) {
        waitingList.removeEntrant(entrant);
    }
}