package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;

public class HistoryTest {

    @Test
    public void constructor_createsEmptyHistory() {
        History history = new History();
        assertEquals(0, history.countEvents());
    }

    @Test
    public void addEvent_increasesCount() {
        History history = new History();
        Events event = new Events("Past Event", "2025-01-01", "desc", "loc");

        history.addEvent(event);
        assertEquals(1, history.countEvents());
    }

    @Test
    public void hasEvent_existingEvent_returnsTrue() {
        History history = new History();
        Events event = new Events("Past Event", "2025-01-01", "desc", "loc");

        history.addEvent(event);
        assertTrue(history.hasEvent(event));
    }

    @Test
    public void hasEvent_nonExistingEvent_returnsFalse() {
        History history = new History();
        Events event = new Events("Past Event", "2025-01-01", "desc", "loc");

        assertFalse(history.hasEvent(event));
    }

    @Test
    public void deleteEvent_removesEvent() {
        History history = new History();
        Events event = new Events("Past Event", "2025-01-01", "desc", "loc");

        history.addEvent(event);
        history.deleteEvent(event);

        assertFalse(history.hasEvent(event));
        assertEquals(0, history.countEvents());
    }

    @Test
    public void addMultipleEvents_countIsCorrect() {
        History history = new History();
        history.addEvent(new Events("E1", "2025-01-01", "d1", "l1"));
        history.addEvent(new Events("E2", "2025-02-01", "d2", "l2"));
        history.addEvent(new Events("E3", "2025-03-01", "d3", "l3"));

        assertEquals(3, history.countEvents());
    }

    @Test
    public void deleteEvent_fromMultiple_removesOnlyThat() {
        History history = new History();
        Events e1 = new Events("E1", "2025-01-01", "d1", "l1");
        Events e2 = new Events("E2", "2025-02-01", "d2", "l2");

        history.addEvent(e1);
        history.addEvent(e2);
        history.deleteEvent(e1);

        assertFalse(history.hasEvent(e1));
        assertTrue(history.hasEvent(e2));
        assertEquals(1, history.countEvents());
    }
}
