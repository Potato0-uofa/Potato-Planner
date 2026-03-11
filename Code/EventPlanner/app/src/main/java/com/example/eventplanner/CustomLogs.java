package com.example.eventplanner;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an abstract class that defines a custom log/list
 */
public abstract class CustomLogs {
    private List<Object> items;

    public CustomLogs() {
        this.items = new ArrayList<>();
    }

    public void addItem(Object item) {
        /**
         * This adds an item to the list if the item does not exist
         * @param item
         *      This is a item that is to be added
         */
        items.add(item);
    }

    public boolean hasItem(Object item) {
        /**
         * Checks to see if an item is within the list or not
         * @param item
         *      This is a item that is to be checked
         */
        return items.contains(item);
    }

    public void deleteItem(Object item) {
        /**
         * This deletes an item if it exists within the list
         * @param item
         *      This is a item that is to be deleted
         */
        items.remove(item);
    }

    public int countItems() {
        /**
         * This checks the number of items in the list
         * @return
         *      returns an int value indicating the number of items in the list
         */
        return items.size();
    }

    public void clearItems() {
        /**
         * Clears all the items in the custom log
         */
        items.clear();
    }

    public void addNotificationLog(Notification notification) {
        /**
         * Adds a notification to the custom log
         * @param notification
         *      notification to add
         */
        if (notification != null) {
            items.add(notification);
        }
    }

    public ArrayList<Notification> getNotificationLogs() {
        /**
         * Returns all notification objects stored in the custom log
         * @return
         *      list of notifications
         */
        ArrayList<Notification> notifications = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Notification) {
                notifications.add((Notification) item);
            }
        }
        return notifications;
    }
}