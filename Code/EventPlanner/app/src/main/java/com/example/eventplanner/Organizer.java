package com.example.eventplanner;

import java.util.ArrayList;

/** Represents an organizer who can create and manage events. */
public class Organizer extends User {

    public Organizer() {} // Required for Firestore

    public Organizer(String deviceId, String name, String email, String phone) {
        super(deviceId, name, email, phone);
    }

    public ArrayList<Entrant> viewWaitingList(Events event) {
        return event.getWaitingList().getEntrants();
    }

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