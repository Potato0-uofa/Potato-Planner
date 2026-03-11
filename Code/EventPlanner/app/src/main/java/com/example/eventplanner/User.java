package com.example.eventplanner;

/**
 * Represents a user of the Event Planner application.
 * Stores the user's basic contact information and device ID
 * used to identify the device in Firestore.
 */
public class User {
    // Attributes of user
    /** The user's name */
    private String name;

    /** The user's email address */
    private String email;

    /** The user's phone number */
    private String phone;

    // When the app launches, it checks if this device is in our Firestore collection
    /** The unique device identifier */
    private String deviceId;

    /**
     * Default constructor required for Firestore.
     */
    public User() {} // Required for Firestore

    // Constructor for the User class
    /**
     * Constructs a User with the specified information.
     *
     * @param deviceId unique identifier for the user's device
     * @param name the user's name
     * @param email the user's email address
     * @param phone the user's phone number
     */
    public User(String deviceId, String name, String email, String phone) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters for the attributes of User

    // Getters

    /**
     * Gets the device ID associated with the user.
     *
     * @return the device ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Gets the user's name.
     *
     * @return the user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the user's email address.
     *
     * @return the user's email
     */

    public String getEmail() {
        return email;
    }

    /**
     * Gets the user's phone number.
     *
     * @return the user's phone number
     */

    public String getPhone() {
        return phone;
    }

    // Setters

    /**
     * Sets the device ID for the user.
     *
     * @param deviceId the new device ID
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Sets the user's name.
     *
     * @param name the user's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the user's email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the user's phone number.
     *
     * @param phone the user's phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

}
