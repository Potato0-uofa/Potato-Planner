package com.example.eventplanner;

import java.util.List;

/** Displays a list of events using a custom log-based layout. */
public class EventList extends CustomLogs {

    /** Local list of events managed by this log. */
    private List<Events> events;

    /**
     * Adds an event to the list.
     *
     * @param event the event to add
     */
    public void addEvent(Events event) {
        addItem(event);
    }

    /**
     * Checks if an event exists in the list.
     *
     * @param event the event to check
     * @return true if the event exists in the list
     */
    public boolean hasEvent(Events event) {
        return hasItem(event);
    }

    /**
     * Deletes an event from the list.
     *
     * @param event the event to delete
     */
    public void deleteEvent(Events event) {
        deleteItem(event);
    }

    /**
     * Returns the number of events in the list.
     *
     * @return the event count
     */
    public int countEvents() {
        return countItems();
    }
}
