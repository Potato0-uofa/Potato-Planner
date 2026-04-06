package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;

public class UserSettingsTest {

    @Test
    public void defaultConstructor_createsSettings() {
        UserSettings settings = new UserSettings();
        assertNotNull(settings);
    }

    @Test
    public void parameterizedConstructor_setsFieldsCorrectly() {
        UserSettings s = new UserSettings("Alice", "alice123", "a@b.com",
                "555-1234", "Canada", true, false, true);

        assertEquals("Alice", s.getName());
        assertEquals("a@b.com", s.getEmail());
        assertEquals("555-1234", s.getPhoneNumber());
        assertEquals("Canada", s.getCountry());
        assertTrue(s.isNotificationsFromOrganizers());
        assertFalse(s.isNotificationsFromAdmins());
        assertTrue(s.isAppUpdates());
    }

    @Test
    public void defaultConstructor_booleansDefaultToFalse() {
        UserSettings s = new UserSettings();
        assertFalse(s.isNotificationsFromOrganizers());
        assertFalse(s.isNotificationsFromAdmins());
        assertFalse(s.isAppUpdates());
    }

    @Test
    public void defaultConstructor_stringsAreNull() {
        UserSettings s = new UserSettings();
        assertNull(s.getName());
        assertNull(s.getEmail());
        assertNull(s.getPhoneNumber());
        assertNull(s.getCountry());
    }

    @Test
    public void setName_updatesCorrectly() {
        UserSettings s = new UserSettings();
        s.setName("Bob");
        assertEquals("Bob", s.getName());
    }

    @Test
    public void setEmail_updatesCorrectly() {
        UserSettings s = new UserSettings();
        s.setEmail("bob@test.com");
        assertEquals("bob@test.com", s.getEmail());
    }

    @Test
    public void setPhoneNumber_updatesCorrectly() {
        UserSettings s = new UserSettings();
        s.setPhoneNumber("780-555-0000");
        assertEquals("780-555-0000", s.getPhoneNumber());
    }

    @Test
    public void setCountry_updatesCorrectly() {
        UserSettings s = new UserSettings();
        s.setCountry("USA");
        assertEquals("USA", s.getCountry());
    }

    @Test
    public void setNotificationsFromOrganizers_toggles() {
        UserSettings s = new UserSettings();
        s.setNotificationsFromOrganizers(true);
        assertTrue(s.isNotificationsFromOrganizers());
        s.setNotificationsFromOrganizers(false);
        assertFalse(s.isNotificationsFromOrganizers());
    }

    @Test
    public void setNotificationsFromAdmins_toggles() {
        UserSettings s = new UserSettings();
        s.setNotificationsFromAdmins(true);
        assertTrue(s.isNotificationsFromAdmins());
        s.setNotificationsFromAdmins(false);
        assertFalse(s.isNotificationsFromAdmins());
    }

    @Test
    public void setAppUpdates_toggles() {
        UserSettings s = new UserSettings();
        s.setAppUpdates(true);
        assertTrue(s.isAppUpdates());
        s.setAppUpdates(false);
        assertFalse(s.isAppUpdates());
    }

    @Test
    public void allNotificationsEnabled() {
        UserSettings s = new UserSettings("X", "x", "x@x.com", "123",
                "CA", true, true, true);
        assertTrue(s.isNotificationsFromOrganizers());
        assertTrue(s.isNotificationsFromAdmins());
        assertTrue(s.isAppUpdates());
    }

    @Test
    public void allNotificationsDisabled() {
        UserSettings s = new UserSettings("X", "x", "x@x.com", "123",
                "CA", false, false, false);
        assertFalse(s.isNotificationsFromOrganizers());
        assertFalse(s.isNotificationsFromAdmins());
        assertFalse(s.isAppUpdates());
    }

    @Test
    public void setters_allowEmptyStrings() {
        UserSettings s = new UserSettings();
        s.setName("");
        s.setEmail("");
        s.setPhoneNumber("");
        s.setCountry("");
        assertEquals("", s.getName());
        assertEquals("", s.getEmail());
        assertEquals("", s.getPhoneNumber());
        assertEquals("", s.getCountry());
    }

    @Test
    public void setters_allowNull() {
        UserSettings s = new UserSettings("A", "u", "a@b.com", "123", "CA",
                true, true, true);
        s.setName(null);
        s.setEmail(null);
        assertNull(s.getName());
        assertNull(s.getEmail());
    }
}
