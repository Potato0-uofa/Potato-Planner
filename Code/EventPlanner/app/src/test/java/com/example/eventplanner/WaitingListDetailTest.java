package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;

public class WaitingListDetailTest {

    @Test
    public void constructor_createsEmptyList() {
        WaitingList wl = new WaitingList();
        assertEquals(0, wl.getCount());
        assertTrue(wl.getEntrants().isEmpty());
    }

    @Test
    public void addEntrant_nullEntrant_doesNotAdd() {
        WaitingList wl = new WaitingList();
        wl.addEntrant(null);
        assertEquals(0, wl.getCount());
    }

    @Test
    public void addEntrant_duplicateEntrant_notAddedTwice() {
        WaitingList wl = new WaitingList();
        Entrant e = new Entrant("d1", "Alice", "a@b.com", "123");
        wl.addEntrant(e);
        wl.addEntrant(e);
        assertEquals(1, wl.getCount());
    }

    @Test
    public void hasEntrant_existingEntrant_returnsTrue() {
        WaitingList wl = new WaitingList();
        Entrant e = new Entrant("d1", "Alice", "a@b.com", "123");
        wl.addEntrant(e);
        assertTrue(wl.hasEntrant(e));
    }

    @Test
    public void hasEntrant_nonExistingEntrant_returnsFalse() {
        WaitingList wl = new WaitingList();
        Entrant e = new Entrant("d1", "Alice", "a@b.com", "123");
        assertFalse(wl.hasEntrant(e));
    }

    @Test
    public void deleteEntrant_removesEntrant() {
        WaitingList wl = new WaitingList();
        Entrant e = new Entrant("d1", "Alice", "a@b.com", "123");
        wl.addEntrant(e);
        wl.deleteEntrant(e);
        assertFalse(wl.hasEntrant(e));
    }

    @Test
    public void getEntrants_returnsCopy() {
        WaitingList wl = new WaitingList();
        Entrant e = new Entrant("d1", "Alice", "a@b.com", "123");
        wl.addEntrant(e);

        ArrayList<Entrant> copy = wl.getEntrants();
        copy.clear(); // modifying the copy

        // original should still have the entrant
        assertTrue(wl.hasEntrant(e));
        assertEquals(1, wl.getCount());
    }

    @Test
    public void cloudCount_overridesLocalCount() {
        WaitingList wl = new WaitingList();
        wl.addEntrant(new Entrant("d1", "A", "a@b.com", "1"));

        wl.setCloudCount(10);
        assertEquals(10, wl.getCount());
    }

    @Test
    public void cloudCount_zeroFallsBackToLocalCount() {
        WaitingList wl = new WaitingList();
        wl.addEntrant(new Entrant("d1", "A", "a@b.com", "1"));
        wl.addEntrant(new Entrant("d2", "B", "b@b.com", "2"));

        wl.setCloudCount(0);
        assertEquals(2, wl.getCount());
    }

    @Test
    public void addMultipleEntrants_countIsCorrect() {
        WaitingList wl = new WaitingList();
        wl.addEntrant(new Entrant("d1", "A", "a@b.com", "1"));
        wl.addEntrant(new Entrant("d2", "B", "b@b.com", "2"));
        wl.addEntrant(new Entrant("d3", "C", "c@b.com", "3"));

        assertEquals(3, wl.getCount());
        assertEquals(3, wl.getEntrants().size());
    }
}
