package com.example.eventplanner;

import java.util.ArrayList;

/** Represents an admin user with elevated privileges for managing events, users, and content. */
public class Admin extends User {

    /** No-argument constructor required for Firestore deserialization. */
    public Admin() {}

    /**
     * Constructs an Admin with the specified user details.
     *
     * @param deviceId unique device identifier for this admin
     * @param name     the admin's display name
     * @param email    the admin's email address
     * @param phone    the admin's phone number
     */
    public Admin(String deviceId, String name, String email, String phone) {
        super(deviceId, name, email, phone);
    }

    /**
     * Retrieves all notification log entries from the given custom log.
     *
     * @param logs the custom log containing notification entries
     * @return list of notifications stored in the log
     */
    public ArrayList<Notification> reviewNotificationLogs(CustomLogs logs) {
        return logs.getNotificationLogs();
    }
}