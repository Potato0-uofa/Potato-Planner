package com.example.eventplanner;

public class Admin extends User {

    public Admin() {} // Required for Firestore

    public Admin(String deviceId, String name, String email, String phone) {
        super(deviceId, name, email, phone);
    }
}