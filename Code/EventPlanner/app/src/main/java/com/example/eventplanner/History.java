package com.example.eventplanner;

import java.util.List;

/** Displays historical log entries using a custom log-based layout. */
public class History extends CustomLogs{
    /** Local list of historical events. */
    private List<Events> events;

    /**
     * Adds an event to the history log.
     *
     * @param event the event to add
     */
    public void addEvent(Events event) {
        addItem(event);
    }

    /**
     * Checks if an event exists in the history.
     *
     * @param event the event to check
     * @return true if the event is in the history
     */
    public boolean hasEvent(Events event) {
        return hasItem(event);
    }

    /**
     * Removes an event from the history log.
     *
     * @param event the event to remove
     */
    public void deleteEvent(Events event) {
        deleteItem(event);
    }

    /**
     * Returns the number of events in the history.
     *
     * @return the event count
     */
    public int countEvents() {
        return countItems();
    }
}
