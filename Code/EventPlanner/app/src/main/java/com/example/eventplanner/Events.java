package com.example.eventplanner;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an event in the event planner application.
 * Supports Firestore persistence + waiting list behavior.
 */
public class Events {

    /** Unique Firestore document ID for this event. */
    private String eventId;

    /** Device or user ID of the organizer who created this event. */
    private String organizerId;

    /** Display name of the event. */
    private String name;

    /** Date of the event as a formatted string. */
    private String date;

    /** Full description of the event. */
    private String description;

    /** Location where the event will be held. */
    private String location;

    /** Maximum number of attendees allowed at the event. */
    private int capacity;

    /**
     * Maximum number of entrants permitted on the waitlist.
     * A value of -1 indicates no limit.
     */
    private int waitlistLimit;

    /** Current status of the event. One of open, closed or cancelled */
    private String status;

    /** Timestamp recording when this event was created in Firestore. */
    private Timestamp createdAt;

    /** The category of the event (e.g., "Sport", "Music"). */
    private String category;

    /** The date/time for availability checks as a timestamp. */
    private long eventTimestamp;

    /** The waiting list associated with this event. Excluded from Firestore to prevent deserialization crashes. */
    private WaitingList waitingListObject;

    /** Download URL of the event image stored in Firebase Storage. */
    private String imageUrl;
    private List<String> coOrganizerIds = new ArrayList<>();


    /** Registration period start date as a string (yyyy-MM-dd). */
    private String registrationStart;

    /** Registration period end date as a string (yyyy-MM-dd). */
    private String registrationEnd;

    /** Whether the event is private or not */
    private boolean isPrivate;

    /** Whether the event requires geolocation */
    private boolean geolocationRequired;

    /** Details for the events */
    private String details;

    /**
     * No-argument constructor required for Firestore deserialization.
     * Initializes an empty waiting list and sets waitlist limit to -1 (unlimited).
     */
    public Events() {
        this.waitingListObject = new WaitingList();
        this.waitlistLimit = -1;
    }

    /**
     * Constructs a minimal event with core details.
     * Status defaults to open and waitlist limit defaults to -1(unlimited).
     *
     * @param name        the display name of the event
     * @param date        the date of the event
     * @param description a full description of the event
     * @param location    the location where the event will be held
     */
    public Events(String name, String date, String description, String location) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.location = location;
        this.waitingListObject = new WaitingList();
        this.status = "open";
        this.waitlistLimit = -1;
    }

    /**
     * Constructs a fully specified event, typically used when reading from Firestore.
     * Initializes an empty waiting list and sets waitlist limit to {-1 (unlimited).
     *
     * @param eventId     the unique Firestore document ID
     * @param organizerId the ID of the organizer who created the event
     * @param name        the display name of the event
     * @param date        the date of the event
     * @param description a full description of the event
     * @param location    the location where the event will be held
     * @param capacity    the maximum number of attendees
     * @param status      the current status open, closed, cancelled
     * @param createdAt   the Firestore timestamp of when the event was created
     */
    public Events(String eventId, String organizerId, String name, String date,
                  String description, String location, int capacity,
                  String status, Timestamp createdAt) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.name = name;
        this.date = date;
        this.description = description;
        this.location = location;
        this.capacity = capacity;
        this.status = status;
        this.createdAt = createdAt;
        this.waitingListObject = new WaitingList();
        this.waitlistLimit = -1;
    }

    // Waitlist limit getter and setter

    /**
     * Returns the maximum number of entrants allowed on the waitlist.
     *
     * @return the waitlist limit, or -1 if there is no limit
     */
    public int getWaitlistLimit() {
        return waitlistLimit;
    }


    /**
     * Sets the maximum number of entrants allowed on the waitlist.
     *
     * @param waitlistLimit the limit to set; use -1 for no limit
     */
    public void setWaitlistLimit(int waitlistLimit) {
        this.waitlistLimit = waitlistLimit;
    }

    /**
     * Returns whether this event has a waitlist limit set.
     *
     * @return true if a limit is set, false if unlimited
     */
    public boolean hasWaitlistLimit() {
        return waitlistLimit != -1;
    }


    /**
     * Adds an entrant to the waiting list if they are not already on it.
     *
     * @param entrant the Entrant to add
     * @throws IllegalStateException if the waitlist is full
     */
    @Exclude
    public void addToWaitingList(Entrant entrant) {
        if (!waitingListObject.hasEntrant(entrant)) {
            if (hasWaitlistLimit() && waitingListObject.getCount() >= waitlistLimit) {
                throw new IllegalStateException("Waiting list is full");
            }
            waitingListObject.addEntrant(entrant);
        }
    }

    /**
     * Removes an entrant from the waiting list if they are present.
     *
     * @param entrant the entrant to remove
     */
    @Exclude
    public void removeFromWaitingList(Entrant entrant) {
        if (waitingListObject.hasEntrant(entrant)) {
            waitingListObject.deleteEntrant(entrant);
        }
    }

    /**
     * Returns whether the given entrant is currently on the waiting list.
     *
     * @param entrant the Entrant to check
     * @return true if the entrant is on the waiting list, false otherwise
     */
    @Exclude
    public boolean isOnWaitingList(Entrant entrant) {
        return waitingListObject.hasEntrant(entrant);
    }


    /**
     * Returns the waiting list associated with this event.
     *
     * @return the Waiting list for this event
     */
    @Exclude
    public WaitingList getWaitingList() {
        return waitingListObject;
    }

    /**
     * Sets the waiting list for this event.
     * If null is provided, a new empty waiting list is assigned instead.
     *
     * @param waitingList the waiting list to assign
     */
    @Exclude
    public void setWaitingList(WaitingList waitingList) {
        this.waitingListObject = (waitingList != null) ? waitingList : new WaitingList();
    }

    /**
     * Returns the current number of entrants on the waiting list.
     *
     * @return the waitlist headcount
     */
    @Exclude
    public int getWaitingListCount() {
        return waitingListObject.getCount();
    }

    /** @return the unique Firestore document ID for this event */
    public String getEventId() {
        return eventId;
    }

    /** @param eventId the Firestore document ID to assign */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /** @return the ID of the organizer who created this event */
    public String getOrganizerId() {
        return organizerId;
    }

    /** @param organizerId the organizer ID to assign */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /** @return the display name of the event */
    public String getName() {
        return name;
    }

    /** @param name the display name to assign */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the date of the event */
    public String getDate() {
        return date;
    }

    /** @param date the date to assign */
    public void setDate(String date) {
        this.date = date;
    }

    /** @return the full description of the event */
    public String getDescription() {
        return description;
    }

    /** @param description the description to assign */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return the location of the event */
    public String getLocation() {
        return location;
    }

    /** @param location the location to assign */
    public void setLocation(String location) {
        this.location = location;
    }

    /** @return the maximum number of attendees */
    public int getCapacity() {
        return capacity;
    }

    /** @param capacity the capacity to assign */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /** @return the current status of the event */
    public String getStatus() {
        return status;
    }

    /** @param status the status to assign ({@code "open"}, {@code "closed"}, or {@code "cancelled"}) */
    public void setStatus(String status) {
        this.status = status;
    }

    /** @return the Firestore timestamp of when this event was created */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /** @param createdAt the creation timestamp to assign */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /** @return the category of the event */
    public String getCategory() {
        return category;
    }

    /** @param category the category to assign */
    public void setCategory(String category) {
        this.category = category;
    }

    /** @return the event timestamp for availability checks */
    public long getEventTimestamp() {
        return eventTimestamp;
    }

    /** @param eventTimestamp the event timestamp to assign */
    public void setEventTimestamp(long eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    /** @return the download URL of the event image */
    public String getImageUrl() {
        return imageUrl;
    }

    /** @param imageUrl the download URL of the event image to assign */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /** @return isPrivate whether the event is private or not */
    public boolean isPrivate() { return isPrivate; }

    /** @param isPrivate the private notation to assign to an event */
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    /** @return geolocationRequired whether the event needs geolocation */
    public boolean isGeolocationRequired() { return geolocationRequired; }

    /** @param geolocationRequired the notation to assign an event if it needs geolocation or not */
    public void setGeolocationRequired(boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    /** @return registrationStart when event registration begins */
    public String getRegistrationStart() { return registrationStart; }

    /** @param registrationStart the field to fill out when creating an event */
    public void setRegistrationStart(String registrationStart) {
        this.registrationStart = registrationStart;
    }

    /** @return registrationEnd when event registration ends */
    public String getRegistrationEnd() { return registrationEnd; }

    /** @param registrationEnd the field to fill out when creating an event */
    public void setRegistrationEnd(String registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public List<String> getCoOrganizerIds() { return coOrganizerIds; }
    public void setCoOrganizerIds(List<String> coOrganizerIds) { this.coOrganizerIds = coOrganizerIds; }

    /** @return getDetails details for an event */
    public String getDetails() { return details; }

    /** @param details the details for the event */
    public void setDetails(String details) { this.details = details; }

}