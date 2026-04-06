package com.example.eventplanner;

import com.google.firebase.Timestamp;


/** Model storing an entrant's geographic coordinates for geolocation-required events. */
public class EntrantLocation{

    /** Device ID of the entrant. */
    private String deviceId;

    /** Latitude of the entrant's location. */
    private double lat;

    /** Longitude of the entrant's location. */
    private double lng;

    /** Timestamp of the last location update. */
    private Timestamp updatedAt;

    /** No-argument constructor required for Firestore deserialization. */
    public EntrantLocation() {}

    /**
     * Constructs an EntrantLocation with all fields specified.
     *
     * @param deviceId  the entrant's device ID
     * @param lat       latitude of the location
     * @param lng       longitude of the location
     * @param updatedAt timestamp of the location update
     */
    public EntrantLocation(String deviceId, double lat, double lng, Timestamp updatedAt) {
        this.deviceId = deviceId;
        this.lat = lat;
        this.lng = lng;
        this.updatedAt = updatedAt;
        }

    /** @return the entrant's device ID */
    public String getDeviceId() { return deviceId; }
    /** @param deviceId the device ID to set */
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    /** @return the latitude of the entrant's location */
    public double getLat() { return lat; }
    /** @param lat the latitude to set */
    public void setLat(double lat) { this.lat = lat; }

    /** @return the longitude of the entrant's location */
    public double getLng() { return lng; }
    /** @param lng the longitude to set */
    public void setLng(double lng) { this.lng = lng; }

    /** @return the timestamp of the last location update */
    public Timestamp getUpdatedAt() { return updatedAt; }
    /** @param updatedAt the update timestamp to set */
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
