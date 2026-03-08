package com.example.eventplanner;

public class Organizer extends User {

    public Organizer() {} // Required for Firestore

    public Organizer(String deviceId, String name, String email, String phone) {
        super(deviceId, name, email, phone);
    }
}