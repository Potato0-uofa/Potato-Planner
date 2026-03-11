package com.example.eventplanner;

public class UserSettings {
    private String name;
    private String email;
    private String phoneNumber;
    private String country;
    private boolean notificationsFromOrganizers;
    private boolean notificationsFromAdmins;
    private boolean appUpdates;

    public UserSettings() { // Required for Firestore
    }

    // Constructor for UserSettingss
    public UserSettings(String name, String username, String email, String phoneNumber, String
                        country, boolean notificationsFromOrganizers,
                        boolean notificationsFromAdmins, boolean appUpdates) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.country = country;
        this.notificationsFromOrganizers = notificationsFromOrganizers;
        this.notificationsFromAdmins = notificationsFromAdmins;
        this.appUpdates = appUpdates;
    }

    // Setting up Getters
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getCountry() {
        return country;
    }
    public boolean isNotificationsFromOrganizers() {
        return notificationsFromOrganizers;
    }
    public boolean isNotificationsFromAdmins() {
        return notificationsFromAdmins;
    }
    public boolean isAppUpdates() {
        return appUpdates;
    }

    // Setting up Setters
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public void setNotificationsFromOrganizers(boolean v) {
        this.notificationsFromOrganizers = v;
    }
    public void setNotificationsFromAdmins(boolean v) {
        this.notificationsFromAdmins = v;
    }
    public void setAppUpdates(boolean v) {
        this.appUpdates = v;
    }

}
