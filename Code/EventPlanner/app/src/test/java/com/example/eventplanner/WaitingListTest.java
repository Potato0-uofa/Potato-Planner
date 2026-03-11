package com.example.eventplanner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WaitingListTest {

    @Test
    public void addAndRemoveEntrant_updatesWaitingListCorrectly() {
        Events event = new Events();
        Entrant entrant = new Entrant();

        event.addToWaitingList(entrant);
        assertTrue(event.isOnWaitingList(entrant));

        event.removeFromWaitingList(entrant);
        assertFalse(event.isOnWaitingList(entrant));
    }
}