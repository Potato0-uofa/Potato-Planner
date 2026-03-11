package com.example.eventplanner;

import java.util.ArrayList;

public class Admin extends User {

    public Admin() {} // Required for Firestore

    public Admin(String deviceId, String name, String email, String phone) {
        super(deviceId, name, email, phone);
    }

    public ArrayList<Notification> reviewNotificationLogs(CustomLogs logs) {
        return logs.getNotificationLogs();
    }
}