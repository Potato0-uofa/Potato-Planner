package com.example.eventplanner;

public class EventList extends CustomLogs {

    public void addEvent(Events event) {
        addItem(event);
    }

    public boolean hasEvent(Events event) {
        return hasItem(event);
    }

    public void deleteEvent(Events event) {
        deleteItem(event);
    }

    public int countEvents() {
        return countItems();
    }
}
