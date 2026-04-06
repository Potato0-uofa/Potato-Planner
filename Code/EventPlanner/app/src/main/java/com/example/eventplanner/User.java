package com.example.eventplanner;

/**
 * Represents a user of the Event Planner application.
 * Stores the user's basic contact information and device ID
 * used to identify the device in Firestore.
 */
public class User {
    private String name;
    private String username;
    private String email;
    private String phone;
    private String description;
    private String country;
    private String address;
    private String deviceId;
    private boolean isEntrant = true;
    private boolean isOrganizer = true;
    private boolean isAdmin = false;

    /** Master notification toggle for the user. Defaults to true for new users. */
    private boolean notificationsEnabled = true;

    /**
     * Default constructor required for Firestore.
     */
    public User() {} 

    /**
     * Constructs a User with the specified information.
     */
    public User(String deviceId, String name, String email, String phone) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.description = "";
        this.country = "";
        this.address = "";
        // Note: notificationsEnabled remains true by default unless changed
    }

    // Getters and Setter

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isEntrant() { return isEntrant; }
    public void setEntrant(boolean isEntrant) { this.isEntrant = isEntrant; }

    public boolean isOrganizer() { return isOrganizer; }
    public void setOrganizer(boolean isOrganizer) { this.isOrganizer = isOrganizer; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }
}