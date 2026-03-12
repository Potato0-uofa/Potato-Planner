package com.example.eventplanner;

/**
 * Model class that represents an entrant in the app
 */
public class Entrant extends User {

    /**
     * Whether this entrant has opted in to notifications.
     * Defaults to true
     */
    private boolean notificationsEnabled;

    /**
     * No argument constructor required for firestore
     */
    public Entrant() {
        this.notificationsEnabled = true;
    } // Required for Firestore

    /**
     * Constructs an Entrant with the specified user details.
     * Initializes notificationsEnabled to true.
     *
     * @param deviceId unique device identifier for this entrant
     * @param name Entrant's display name
     * @param email Entrant's email
     * @param phone Entrant's phone number
     */
    public Entrant(String deviceId, String name, String email, String phone) {
        super(deviceId, name, email, phone);
        this.notificationsEnabled = true;
    }

    /**
     * Checks to see if the entrant's notifications are enabled.
     * @return true if notifications are enabled and false if not
     */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Sets notifications preference for the entrant
     * @param notificationsEnabled
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * Disables notifications for the entrant.
     */
    public void optOutNotifications() {
        this.notificationsEnabled = false;
    }

    /**
     * Enables notifications for the entrant.
     */
    public void optInNotifications() {
        this.notificationsEnabled = true;
    }
}