package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;

public class EntrantLocationTest {

    @Test
    public void defaultConstructor_createsLocation() {
        EntrantLocation loc = new EntrantLocation();
        assertNotNull(loc);
    }

    @Test
    public void parameterizedConstructor_setsFieldsCorrectly() {
        EntrantLocation loc = new EntrantLocation("device1", 53.5461, -113.4938, null);

        assertEquals("device1", loc.getDeviceId());
        assertEquals(53.5461, loc.getLat(), 0.0001);
        assertEquals(-113.4938, loc.getLng(), 0.0001);
        assertNull(loc.getUpdatedAt());
    }

    @Test
    public void defaultConstructor_hasDefaultValues() {
        EntrantLocation loc = new EntrantLocation();

        assertNull(loc.getDeviceId());
        assertEquals(0.0, loc.getLat(), 0.0001);
        assertEquals(0.0, loc.getLng(), 0.0001);
        assertNull(loc.getUpdatedAt());
    }

    @Test
    public void setDeviceId_updatesCorrectly() {
        EntrantLocation loc = new EntrantLocation();
        loc.setDeviceId("device-xyz");
        assertEquals("device-xyz", loc.getDeviceId());
    }

    @Test
    public void setLat_updatesCorrectly() {
        EntrantLocation loc = new EntrantLocation();
        loc.setLat(51.0447);
        assertEquals(51.0447, loc.getLat(), 0.0001);
    }

    @Test
    public void setLng_updatesCorrectly() {
        EntrantLocation loc = new EntrantLocation();
        loc.setLng(-114.0719);
        assertEquals(-114.0719, loc.getLng(), 0.0001);
    }

    @Test
    public void setLat_negativeValue() {
        EntrantLocation loc = new EntrantLocation();
        loc.setLat(-33.8688);
        assertEquals(-33.8688, loc.getLat(), 0.0001);
    }

    @Test
    public void setLng_positiveValue() {
        EntrantLocation loc = new EntrantLocation();
        loc.setLng(151.2093);
        assertEquals(151.2093, loc.getLng(), 0.0001);
    }

    @Test
    public void setLat_zeroValue() {
        EntrantLocation loc = new EntrantLocation();
        loc.setLat(10.0);
        loc.setLat(0.0);
        assertEquals(0.0, loc.getLat(), 0.0001);
    }

    @Test
    public void setLng_zeroValue() {
        EntrantLocation loc = new EntrantLocation();
        loc.setLng(50.0);
        loc.setLng(0.0);
        assertEquals(0.0, loc.getLng(), 0.0001);
    }

    @Test
    public void setDeviceId_allowsNull() {
        EntrantLocation loc = new EntrantLocation("d1", 10.0, 20.0, null);
        loc.setDeviceId(null);
        assertNull(loc.getDeviceId());
    }
}
