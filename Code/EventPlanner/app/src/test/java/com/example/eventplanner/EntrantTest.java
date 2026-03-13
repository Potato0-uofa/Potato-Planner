package com.example.eventplanner;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EntrantTest {

    private Entrant entrant;

    @Before
    public void setUp() {
        entrant = new Entrant("device123", "John Doe", "john@example.com", "1234567890");
    }

    // --- Constructor Tests ---

    @Test
    public void testParameterizedConstructor_notificationsEnabledByDefault() {
        assertTrue(entrant.isNotificationsEnabled());
    }

    @Test
    public void testParameterizedConstructor_setsFieldsCorrectly() {
        assertEquals("device123", entrant.getDeviceId());
        assertEquals("John Doe", entrant.getName());
        assertEquals("john@example.com", entrant.getEmail());
        assertEquals("1234567890", entrant.getPhone());
    }

    @Test
    public void testNoArgConstructor_notificationsEnabledByDefault() {
        Entrant defaultEntrant = new Entrant();
        assertTrue(defaultEntrant.isNotificationsEnabled());
    }

    // --- Notification Tests ---

    @Test
    public void testOptOutNotifications_disablesNotifications() {
        entrant.optOutNotifications();
        assertFalse(entrant.isNotificationsEnabled());
    }

    @Test
    public void testOptInNotifications_enablesNotifications() {
        entrant.optOutNotifications(); // disable first
        entrant.optInNotifications();
        assertTrue(entrant.isNotificationsEnabled());
    }

    @Test
    public void testSetNotificationsEnabled_false_disablesNotifications() {
        entrant.setNotificationsEnabled(false);
        assertFalse(entrant.isNotificationsEnabled());
    }

    @Test
    public void testSetNotificationsEnabled_true_enablesNotifications() {
        entrant.setNotificationsEnabled(false); // disable first
        entrant.setNotificationsEnabled(true);
        assertTrue(entrant.isNotificationsEnabled());
    }

    @Test
    public void testOptOutThenOptIn_returnsToEnabled() {
        entrant.optOutNotifications();
        assertFalse(entrant.isNotificationsEnabled());
        entrant.optInNotifications();
        assertTrue(entrant.isNotificationsEnabled());
    }

    @Test
    public void testOptOutCalledTwice_remainsDisabled() {
        entrant.optOutNotifications();
        entrant.optOutNotifications();
        assertFalse(entrant.isNotificationsEnabled());
    }

    @Test
    public void testOptInCalledTwice_remainsEnabled() {
        entrant.optInNotifications();
        entrant.optInNotifications();
        assertTrue(entrant.isNotificationsEnabled());
    }
}