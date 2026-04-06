package com.example.eventplanner;

import java.util.ArrayList;

/** Represents an organizer who can create and manage events. */
public class Organizer extends User {

    /** No-argument constructor required for Firestore deserialization. */
    public Organizer() {}

    /**
     * Constructs an Organizer with the specified user details.
     *
     * @param deviceId unique device identifier for this organizer
     * @param name     the organizer's display name
     * @param email    the organizer's email address
     * @param phone    the organizer's phone number
     */
    public Organizer(String deviceId, String name, String email, String phone) {
        super(deviceId, name, email, phone);
    }

    /**
     * Returns the current entrants on the event's waiting list.
     *
     * @param event the event whose waiting list to view
     * @return list of entrants on the waiting list
     */
    public ArrayList<Entrant> viewWaitingList(Events event) {
        return event.getWaitingList().getEntrants();
    }

    /**
     * Sends a notification to an entrant about an event, logging it if the
     * entrant has notifications enabled.
     *
     * @param entrant   the entrant to notify
     * @param event     the event the notification is about
     * @param message   the notification message body
     * @param timestamp the time the notification is sent
     * @param type      the notification type
     * @param logs      the custom log to record the notification in
     */
    public void sendNotification(Entrant entrant, Events event, String message,
                                 String timestamp, String type, CustomLogs logs) {
        if (entrant != null && entrant.isNotificationsEnabled()) {
            Notification notification = new Notification(
                    message,
                    this.getName(),
                    entrant.getName(),
                    event.getName(),
                    timestamp,
                    type
            );
            if (logs != null) {
                logs.addNotificationLog(notification);
            }
        }
    }
}