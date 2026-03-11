package com.example.eventplanner;

public class Entrant extends User {

    public Entrant() {} // Required for Firestore


    public Entrant(String deviceId, String name, String email, String phone) {
        super(deviceId, name, email, phone);

    }
}