package com.example.eventplanner;

public class Entrant extends User {
    private boolean notificationsEnabled;

    public Entrant() {
        this.notificationsEnabled = true;
    } // Required for Firestore

    public Entrant(String deviceId, String name, String email, String phone) {
        super(deviceId, name, email, phone);
        this.notificationsEnabled = true;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public void optOutNotifications() {
        this.notificationsEnabled = false;
    }

    public void optInNotifications() {
        this.notificationsEnabled = true;
    }
}