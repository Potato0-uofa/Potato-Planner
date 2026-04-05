package com.example.eventplanner;

import com.google.firebase.Timestamp;


/** Model storing an entrant's geographic coordinates for geolocation-required events. */
public class EntrantLocation{

    private String deviceId;
    private double lat;
    private double lng;
    private Timestamp updatedAt;

    public EntrantLocation() {}


    public EntrantLocation(String deviceId, double lat, double lng, Timestamp updatedAt) {
        this.deviceId = deviceId;
        this.lat = lat;
        this.lng = lng;
        this.updatedAt = updatedAt;
        }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
