package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;

public class EventsWaitlistTest {

    @Test
    public void newEvent_hasEmptyWaitingList() {
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        assertEquals(0, event.getWaitingListCount());
    }

    @Test
    public void addToWaitingList_addsEntrant() {
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        Entrant entrant = new Entrant("d1", "Alice", "a@b.com", "123");

        event.addToWaitingList(entrant);

        assertTrue(event.isOnWaitingList(entrant));
        assertEquals(1, event.getWaitingListCount());
    }

    @Test
    public void addToWaitingList_duplicateEntrant_notAddedTwice() {
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        Entrant entrant = new Entrant("d1", "Alice", "a@b.com", "123");

        event.addToWaitingList(entrant);
        event.addToWaitingList(entrant);

        assertEquals(1, event.getWaitingListCount());
    }

    @Test
    public void removeFromWaitingList_removesEntrant() {
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        Entrant entrant = new Entrant("d1", "Alice", "a@b.com", "123");

        event.addToWaitingList(entrant);
        event.removeFromWaitingList(entrant);

        assertFalse(event.isOnWaitingList(entrant));
        assertEquals(0, event.getWaitingListCount());
    }

    @Test
    public void removeFromWaitingList_nonExistentEntrant_noEffect() {
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        Entrant e1 = new Entrant("d1", "Alice", "a@b.com", "123");
        Entrant e2 = new Entrant("d2", "Bob", "b@b.com", "456");

        event.addToWaitingList(e1);
        event.removeFromWaitingList(e2);

        assertTrue(event.isOnWaitingList(e1));
        assertEquals(1, event.getWaitingListCount());
    }

    @Test
    public void isOnWaitingList_notOnList_returnsFalse() {
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        Entrant entrant = new Entrant("d1", "Alice", "a@b.com", "123");

        assertFalse(event.isOnWaitingList(entrant));
    }

    @Test
    public void waitlistLimit_defaultIsUnlimited() {
        Events event = new Events();
        assertEquals(-1, event.getWaitlistLimit());
        assertFalse(event.hasWaitlistLimit());
    }

    @Test
    public void waitlistLimit_setAndGet() {
        Events event = new Events();
        event.setWaitlistLimit(5);
        assertEquals(5, event.getWaitlistLimit());
        assertTrue(event.hasWaitlistLimit());
    }

    @Test(expected = IllegalStateException.class)
    public void addToWaitingList_exceedsLimit_throwsException() {
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        event.setWaitlistLimit(2);

        event.addToWaitingList(new Entrant("d1", "A", "a@b.com", "1"));
        event.addToWaitingList(new Entrant("d2", "B", "b@b.com", "2"));
        event.addToWaitingList(new Entrant("d3", "C", "c@b.com", "3")); // should throw
    }

    @Test
    public void addToWaitingList_atLimit_doesNotThrow() {
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        event.setWaitlistLimit(2);

        event.addToWaitingList(new Entrant("d1", "A", "a@b.com", "1"));
        event.addToWaitingList(new Entrant("d2", "B", "b@b.com", "2"));

        assertEquals(2, event.getWaitingListCount());
    }

    @Test
    public void addToWaitingList_noLimit_allowsMany() {
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        // default is -1 (no limit)

        for (int i = 0; i < 100; i++) {
            event.addToWaitingList(new Entrant("d" + i, "E" + i, "e" + i + "@b.com", "" + i));
        }

        assertEquals(100, event.getWaitingListCount());
    }

    @Test
    public void getWaitingList_returnsWaitingListObject() {
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        assertNotNull(event.getWaitingList());
    }

    @Test
    public void setWaitingList_null_assignsNewEmptyList() {
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        event.addToWaitingList(new Entrant("d1", "A", "a@b.com", "1"));

        event.setWaitingList(null);

        assertNotNull(event.getWaitingList());
        assertEquals(0, event.getWaitingListCount());
    }

    @Test
    public void defaultConstructor_setsStatusOpen() {
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        assertEquals("open", event.getStatus());
    }

    @Test
    public void event_categoryGetterSetter() {
        Events event = new Events();
        event.setCategory("Music");
        assertEquals("Music", event.getCategory());
    }

    @Test
    public void event_imageUrlGetterSetter() {
        Events event = new Events();
        event.setImageUrl("https://example.com/image.png");
        assertEquals("https://example.com/image.png", event.getImageUrl());
    }

    @Test
    public void event_privateGetterSetter() {
        Events event = new Events();
        assertFalse(event.isPrivate());
        event.setPrivate(true);
        assertTrue(event.isPrivate());
    }

    @Test
    public void event_geolocationRequiredGetterSetter() {
        Events event = new Events();
        assertFalse(event.isGeolocationRequired());
        event.setGeolocationRequired(true);
        assertTrue(event.isGeolocationRequired());
    }

    @Test
    public void event_registrationStartEndGetterSetter() {
        Events event = new Events();
        event.setRegistrationStart("2026-01-01");
        event.setRegistrationEnd("2026-01-15");
        assertEquals("2026-01-01", event.getRegistrationStart());
        assertEquals("2026-01-15", event.getRegistrationEnd());
    }

    @Test
    public void event_coOrganizerIdsGetterSetter() {
        Events event = new Events();
        assertNotNull(event.getCoOrganizerIds());
        assertTrue(event.getCoOrganizerIds().isEmpty());

        event.setCoOrganizerIds(Arrays.asList("org1", "org2"));
        assertEquals(2, event.getCoOrganizerIds().size());
    }

    @Test
    public void event_tagsGetterSetter() {
        Events event = new Events();
        assertNotNull(event.getTags());

        event.setTags(Arrays.asList("music", "outdoor"));
        assertEquals(2, event.getTags().size());
        assertEquals("music", event.getTags().get(0));
    }

    @Test
    public void event_setTagsNull_setsEmptyList() {
        Events event = new Events();
        event.setTags(null);
        assertNotNull(event.getTags());
        assertTrue(event.getTags().isEmpty());
    }

    @Test
    public void event_detailsGetterSetter() {
        Events event = new Events();
        event.setDetails("Bring your laptop");
        assertEquals("Bring your laptop", event.getDetails());
    }

    @Test
    public void event_eventTimestampGetterSetter() {
        Events event = new Events();
        event.setEventTimestamp(1234567890L);
        assertEquals(1234567890L, event.getEventTimestamp());
    }
}
