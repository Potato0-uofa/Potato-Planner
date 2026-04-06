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

    /**
     * This adds an item to the list if the item does not exist
     * @param item
     *      This is a item that is to be added
     */
    public void addItem(Object item) {
        items.add(item);
    }

    /**
     * Checks to see if an item is within the list or not
     * @param item
     *      This is a item that is to be checked
     */
    public boolean hasItem(Object item) {
        return items.contains(item);
    }

    /**
     * This deletes an item if it exists within the list
     * @param item
     *      This is a item that is to be deleted
     */
    public void deleteItem(Object item) {
        items.remove(item);
    }

    /**
     * This checks the number of items in the list
     * @return
     *      returns an int value indicating the number of items in the list
     */
    public int countItems() {
        return items.size();
    }

    /**
     * Clears all the items in the custom log
     */
    public void clearItems() {
        items.clear();
    }


    /**
     * Adds a notification to the custom log
     * @param notification
     *      notification to add
     */
    public void addNotificationLog(Notification notification) {
        if (notification != null) {
            items.add(notification);
        }
    }

    /**
     * Returns all notification objects stored in the custom log
     * @return
     *      list of notifications
     */
    public ArrayList<Notification> getNotificationLogs() {
        ArrayList<Notification> notifications = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Notification) {
                notifications.add((Notification) item);
            }
        }
        return notifications;
    }
}