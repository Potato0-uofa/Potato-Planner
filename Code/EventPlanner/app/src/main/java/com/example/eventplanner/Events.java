package com.example.eventplanner;

import java.util.List;

/**
 * Represents an event in the event planner application.
 * Each event has a name, date, description, location, and a waiting list of entrants.
 */

public class Events {
    //attributes of the events
    private String name;
    private String date;
    private String description;
    private String location;
    private WaitingList waitingList;

    /**
     * Default constructor required for Firestore.
     * Initializes an empty waiting list.
     */
    public Events(){
        this.waitingList = new WaitingList();
    }

    /**
     * Constructs an Events object with the specified details.
     *
     * @param name        the name of the event
     * @param date        the date of the event
     * @param description a brief description of the event
     * @param location    the location where the event will be held
     */
    public Events(String name, String date, String description, String location) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.location = location;
        this.waitingList = new WaitingList();
    }

    /**
     * Adds an entrant to the waiting list for this event.
     * Does nothing if entrant is already on the list.
     *
     * @param entrant the entrant to add to the waiting list
     */
    public void addToWaitingList(Entrant entrant) {
        if (!waitingList.hasEntrant(entrant)) {
            waitingList.addEntrant(entrant);
        }
    }

    /**
     * Removes an entrant from the waiting list for this event.
     * Does nothing if the entrant is not on the waiting list.
     *
     * @param entrant the entrant to remove from the waiting list
     */
    public void removeFromWaitingList(Entrant entrant) {
        if (waitingList.hasEntrant(entrant)){
            waitingList.deleteEntrant(entrant);
        }
    }

    /**
     * Checks whether a given entrant is on the waiting list for this event.
     *
     * @param entrant the entrant to check
     * @return true if the entrant is on the waiting list, false otherwise
     */
    public boolean isOnWaitingList(Entrant entrant) {
        return waitingList.hasEntrant(entrant);
    }

    /**
     * Returns the waiting list for this event.
     *
     * @return the WaitingList object associated with this event
     */
    public WaitingList getWaitingList() {
        return waitingList;
    }

    /**
     * Returns the name of the event.
     * @return event name
     */
    public String getName() { return name; }

    /**
     * Returns the date of the event.
     * @return event date
     */
    public String getDate() { return date; }

    /**
     * Returns the event description.
     * @return event description
     */
    public String getDescription() { return description; }

    /**
     * Returns the location of the event.
     * @return event location
     */
    public String getLocation() {return location; }


    /**
     * Sets the name of the event.
     * @param name the event name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Sets the date of the event.
     * @param date the date of the event
     */
    public void setDate(String date) { this.date = date; }

    /**
     * Sets the description of the event.
     * @param description the event description
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Sets the location of the event
     * @param location the event's location
     */
    public void setLocation(String location) {this.location = location;}
}