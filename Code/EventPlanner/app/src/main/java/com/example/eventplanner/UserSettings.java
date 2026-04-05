package com.example.eventplanner;

/** Model holding user preference settings such as notification toggles. */
public class UserSettings {
    /** The user's name */
    private String name;

    /** The user's email address */
    private String email;

    /** The user's phone number */
    private String phoneNumber;

    /** The user's country */
    private String country;

    /** Whether the user recieves notifications from event organizers */
    private boolean notificationsFromOrganizers;

    /** Whether the user recieves notifications from administrators */
    private boolean notificationsFromAdmins;

    /** Whether the user recieves app update notifications */
    private boolean appUpdates;

    /**
     * Default constructor required for Firestore.
     */
    public UserSettings() { // Required for Firestore
    }

    // Constructor for UserSettings
    /**
     * Constructs a UserSettings object with the specified settings.
     *
     * @param name the user's name
     * @param username the username of the user (not currently stored)
     * @param email the user's email address
     * @param phoneNumber the user's phone number
     * @param country the user's country
     * @param notificationsFromOrganizers whether organizer notifications are enabled
     * @param notificationsFromAdmins whether admin notifications are enabled
     * @param appUpdates whether app update notifications are enabled
     */
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
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Gets the user's country.
     *
     * @return the user's country
     */

    public String getCountry() {
        return country;
    }

    /**
     * Checks if notifications from organizers are enabled.
     *
     * @return true if organizer notifications are enabled
     */
    public boolean isNotificationsFromOrganizers() {
        return notificationsFromOrganizers;
    }


    /**
     * Checks if notifications from administrators are enabled.
     *
     * @return true if admin notifications are enabled
     */
    public boolean isNotificationsFromAdmins() {
        return notificationsFromAdmins;
    }

    /**
     * Checks if app update notifications are enabled.
     *
     * @return true if app update notifications are enabled
     */
    public boolean isAppUpdates() {
        return appUpdates;
    }

    // Setting up Setters
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
     * @param phoneNumber the user's phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Sets the user's country.
     *
     * @param country the user's country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Enables or disables notifications from organizers.
     *
     * @param v true to enable organizer notifications
     */
    public void setNotificationsFromOrganizers(boolean v) {
        this.notificationsFromOrganizers = v;
    }

    /**
     * Enables or disables notifications from administrators.
     *
     * @param v true to enable admin notifications
     */
    public void setNotificationsFromAdmins(boolean v) {
        this.notificationsFromAdmins = v;
    }

    /**
     * Enables or disables app update notifications.
     *
     * @param v true to enable app update notifications
     */
    public void setAppUpdates(boolean v) {
        this.appUpdates = v;
    }

}
