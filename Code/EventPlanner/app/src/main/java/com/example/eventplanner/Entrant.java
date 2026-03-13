package com.example.eventplanner;

/**
 * Model class that represents an entrant in the app.
 * Inherits notification preferences from the User class.
 */
public class Entrant extends User {

    /**
     * No argument constructor required for firestore
     */
    public Entrant() {
        super();
    }

    /**
     * Constructs an Entrant with the specified user details.
     *
     * @param deviceId unique device identifier for this entrant
     * @param name Entrant's display name
     * @param email Entrant's email
     * @param phone Entrant's phone number
     */
    public Entrant(String deviceId, String name, String email, String phone) {
        super(deviceId, name, email, phone);
    }

    /**
     * Disables notifications for the entrant.
     */
    public void optOutNotifications() {
        this.setNotificationsEnabled(false);
    }

    /**
     * Enables notifications for the entrant.
     */
    public void optInNotifications() {
        this.setNotificationsEnabled(true);
    }
}