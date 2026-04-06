package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;

public class EventsFullConstructorTest {

    @Test
    public void fullConstructor_setsAllFields() {
        Events event = new Events("eid1", "org1", "Hackathon", "2026-05-01",
                "A coding event", "Edmonton", 100, "open", null);

        assertEquals("eid1", event.getEventId());
        assertEquals("org1", event.getOrganizerId());
        assertEquals("Hackathon", event.getName());
        assertEquals("2026-05-01", event.getDate());
        assertEquals("A coding event", event.getDescription());
        assertEquals("Edmonton", event.getLocation());
        assertEquals(100, event.getCapacity());
        assertEquals("open", event.getStatus());
        assertNull(event.getCreatedAt());
    }

    @Test
    public void fullConstructor_defaultWaitlistLimitIsUnlimited() {
        Events event = new Events("eid1", "org1", "Test", "2026-01-01",
                "desc", "loc", 50, "open", null);
        assertEquals(-1, event.getWaitlistLimit());
        assertFalse(event.hasWaitlistLimit());
    }

    @Test
    public void fullConstructor_hasEmptyWaitingList() {
        Events event = new Events("eid1", "org1", "Test", "2026-01-01",
                "desc", "loc", 50, "open", null);
        assertNotNull(event.getWaitingList());
        assertEquals(0, event.getWaitingListCount());
    }

    @Test
    public void simpleConstructor_statusIsOpen() {
        Events event = new Events("Name", "2026-01-01", "desc", "loc");
        assertEquals("open", event.getStatus());
    }

    @Test
    public void defaultConstructor_waitlistLimitNegativeOne() {
        Events event = new Events();
        assertEquals(-1, event.getWaitlistLimit());
    }

    @Test
    public void defaultConstructor_capacityIsZero() {
        Events event = new Events();
        assertEquals(0, event.getCapacity());
    }

    @Test
    public void setStatus_closed() {
        Events event = new Events("E", "2026-01-01", "d", "l");
        event.setStatus("closed");
        assertEquals("closed", event.getStatus());
    }

    @Test
    public void setStatus_cancelled() {
        Events event = new Events("E", "2026-01-01", "d", "l");
        event.setStatus("cancelled");
        assertEquals("cancelled", event.getStatus());
    }

    @Test
    public void coOrganizerIds_defaultEmpty() {
        Events event = new Events();
        assertNotNull(event.getCoOrganizerIds());
        assertTrue(event.getCoOrganizerIds().isEmpty());
    }

    @Test
    public void tags_defaultEmpty() {
        Events event = new Events();
        assertNotNull(event.getTags());
        assertTrue(event.getTags().isEmpty());
    }

    @Test
    public void isPrivate_defaultFalse() {
        Events event = new Events();
        assertFalse(event.isPrivate());
    }

    @Test
    public void geolocationRequired_defaultFalse() {
        Events event = new Events();
        assertFalse(event.isGeolocationRequired());
    }
}
