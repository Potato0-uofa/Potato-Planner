package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;

public class EventListTest {

    @Test
    public void constructor_createsEmptyList() {
        EventList list = new EventList();
        assertEquals(0, list.countEvents());
    }

    @Test
    public void addEvent_increasesCount() {
        EventList list = new EventList();
        Events event = new Events("Test", "2026-01-01", "desc", "loc");

        list.addEvent(event);
        assertEquals(1, list.countEvents());
    }

    @Test
    public void addEvent_multipleEvents() {
        EventList list = new EventList();
        Events e1 = new Events("Event1", "2026-01-01", "desc1", "loc1");
        Events e2 = new Events("Event2", "2026-01-02", "desc2", "loc2");
        Events e3 = new Events("Event3", "2026-01-03", "desc3", "loc3");

        list.addEvent(e1);
        list.addEvent(e2);
        list.addEvent(e3);

        assertEquals(3, list.countEvents());
    }

    @Test
    public void hasEvent_existingEvent_returnsTrue() {
        EventList list = new EventList();
        Events event = new Events("Test", "2026-01-01", "desc", "loc");

        list.addEvent(event);
        assertTrue(list.hasEvent(event));
    }

    @Test
    public void hasEvent_nonExistingEvent_returnsFalse() {
        EventList list = new EventList();
        Events event = new Events("Test", "2026-01-01", "desc", "loc");

        assertFalse(list.hasEvent(event));
    }

    @Test
    public void deleteEvent_removesEvent() {
        EventList list = new EventList();
        Events event = new Events("Test", "2026-01-01", "desc", "loc");

        list.addEvent(event);
        assertTrue(list.hasEvent(event));

        list.deleteEvent(event);
        assertFalse(list.hasEvent(event));
        assertEquals(0, list.countEvents());
    }

    @Test
    public void deleteEvent_fromMultiple_removesOnlyThat() {
        EventList list = new EventList();
        Events e1 = new Events("Event1", "2026-01-01", "desc1", "loc1");
        Events e2 = new Events("Event2", "2026-01-02", "desc2", "loc2");

        list.addEvent(e1);
        list.addEvent(e2);
        list.deleteEvent(e1);

        assertFalse(list.hasEvent(e1));
        assertTrue(list.hasEvent(e2));
        assertEquals(1, list.countEvents());
    }

    @Test
    public void countEvents_emptyList_returnsZero() {
        EventList list = new EventList();
        assertEquals(0, list.countEvents());
    }
}
