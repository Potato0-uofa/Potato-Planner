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

    /** The user's username */
    private String username;

    /** The user's email address */
    private String email;

    /** The user's phone number */
    private String phone;

    /** The user's profile description */
    private String description;

    /** The user's country */
    private String country;

    /** The user's business address */
    private String address;

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
        this.description = "";
        this.country = "";
        this.address = "";
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
     * Gets the user's username.
     * Not prompted on signup, user can choose to have a username when navigating to their
     * profile settings.
     *
     * @return
     */
    public String getUsername() { return username; }

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

    /** Gets the user's profile description.
     *
     * @return the user's profile description
     */
    public String getDescription() { return description; }

    /** Gets the user's country.
     *
     * @return the user's country
     */
    public String getCountry() { return country; }

    /** Gets the user's business address.
     *
     * @return the user's business address
     */
    public String getAddress() { return address; }

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
     * Sets the user's username (if they choose to have one)
     *
     * @param username
     */
    public void setUsername(String username) { this.username = username; }

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

    /** Sets the user's profile description.
     *
     * @param description the user's profile description
     */
    public void setDescription(String description) { this.description = description; }

    /** Sets the user's country.
     *
     * @param country the user's country
     */
    public void setCountry(String country) { this.country = country; }

    /** Sets the user's business address.
     *
     * @param address the user's business address
     */
    public void setAddress(String address) { this.address = address; }



}
