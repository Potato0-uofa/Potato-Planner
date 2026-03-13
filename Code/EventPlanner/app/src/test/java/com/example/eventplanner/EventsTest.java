package com.example.eventplanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class EventsTest {

    @Test
    public void defaultConstructor_createsEvent() {
        Events event = new Events();
        assertNotNull(event);
    }

    @Test
    public void constructor_setsBasicFieldsCorrectly() {
        Events event = new Events("Hackathon", "2026-03-20", "Coding event", "Edmonton");

        assertEquals("Hackathon", event.getName());
        assertEquals("2026-03-20", event.getDate());
        assertEquals("Coding event", event.getDescription());
        assertEquals("Edmonton", event.getLocation());
    }

    @Test
    public void setters_updateFieldsCorrectly() {
        Events event = new Events();

        event.setEventId("event1");
        event.setOrganizerId("org1");
        event.setName("Workshop");
        event.setDate("2026-03-25");
        event.setDescription("ML workshop");
        event.setLocation("UAlberta");
        event.setCapacity(50);
        event.setStatus("closed");

        assertEquals("event1", event.getEventId());
        assertEquals("org1", event.getOrganizerId());
        assertEquals("Workshop", event.getName());
        assertEquals("2026-03-25", event.getDate());
        assertEquals("ML workshop", event.getDescription());
        assertEquals("UAlberta", event.getLocation());
        assertEquals(50, event.getCapacity());
        assertEquals("closed", event.getStatus());
    }
    @Test
    public void defaultConstructor_hasNullBasicFieldsInitially() {
        Events event = new Events();

        assertEquals(null, event.getEventId());
        assertEquals(null, event.getOrganizerId());
        assertEquals(null, event.getName());
        assertEquals(null, event.getDate());
        assertEquals(null, event.getDescription());
        assertEquals(null, event.getLocation());
    }

    @Test
    public void setters_allowEmptyStrings() {
        Events event = new Events();

        event.setEventId("");
        event.setOrganizerId("");
        event.setName("");
        event.setDate("");
        event.setDescription("");
        event.setLocation("");
        event.setStatus("");

        assertEquals("", event.getEventId());
        assertEquals("", event.getOrganizerId());
        assertEquals("", event.getName());
        assertEquals("", event.getDate());
        assertEquals("", event.getDescription());
        assertEquals("", event.getLocation());
        assertEquals("", event.getStatus());
    }

    @Test
    public void capacitySetter_updatesCorrectly() {
        Events event = new Events();

        event.setCapacity(0);
        assertEquals(0, event.getCapacity());

        event.setCapacity(100);
        assertEquals(100, event.getCapacity());
    }
    @Test
    public void descriptionSetter_updatesCorrectly() {
        Events event = new Events();

        event.setDescription("Music Festival");

        assertEquals("Music Festival", event.getDescription());
    }
}