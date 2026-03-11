package com.example.eventplanner;

import java.util.List;

public class History extends CustomLogs{
    private List<Events> events;

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
