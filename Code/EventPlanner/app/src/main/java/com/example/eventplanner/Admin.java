package com.example.eventplanner;

import java.util.ArrayList;

/** Represents an admin user with elevated privileges for managing events, users, and content. */
public class Admin extends User {

    public Admin() {} // Required for Firestore

    public Admin(String deviceId, String name, String email, String phone) {
        super(deviceId, name, email, phone);
    }

    public ArrayList<Notification> reviewNotificationLogs(CustomLogs logs) {
        return logs.getNotificationLogs();
    }
}