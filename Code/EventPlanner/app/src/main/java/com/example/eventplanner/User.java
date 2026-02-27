package com.example.eventplanner;

public class User {
    // Attributes of user
    private String name;
    private String email;
    private String phone;

    // When the app launches, it checks if this device is in our Firestore collection
    private String deviceId;

    public User() {} // Required for Firestore

    // Constructor for the User class
    public User(String deviceId, String name, String email, String phone) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters for the attributes of User

    // Getters
    public String getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    // Setterse
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
