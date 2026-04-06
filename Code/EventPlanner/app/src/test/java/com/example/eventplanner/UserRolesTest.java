package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;

public class UserRolesTest {

    @Test
    public void defaultConstructor_isEntrantByDefault() {
        User user = new User();
        assertTrue(user.isEntrant());
    }

    @Test
    public void defaultConstructor_isOrganizerByDefault() {
        User user = new User();
        assertTrue(user.isOrganizer());
    }

    @Test
    public void defaultConstructor_isNotAdminByDefault() {
        User user = new User();
        assertFalse(user.isAdmin());
    }

    @Test
    public void setEntrant_togglesCorrectly() {
        User user = new User();
        user.setEntrant(false);
        assertFalse(user.isEntrant());
        user.setEntrant(true);
        assertTrue(user.isEntrant());
    }

    @Test
    public void setOrganizer_togglesCorrectly() {
        User user = new User();
        user.setOrganizer(false);
        assertFalse(user.isOrganizer());
        user.setOrganizer(true);
        assertTrue(user.isOrganizer());
    }

    @Test
    public void setAdmin_togglesCorrectly() {
        User user = new User();
        user.setAdmin(true);
        assertTrue(user.isAdmin());
        user.setAdmin(false);
        assertFalse(user.isAdmin());
    }

    @Test
    public void notificationsEnabled_defaultTrue() {
        User user = new User();
        assertTrue(user.isNotificationsEnabled());
    }

    @Test
    public void notificationsEnabled_togglesCorrectly() {
        User user = new User();
        user.setNotificationsEnabled(false);
        assertFalse(user.isNotificationsEnabled());
        user.setNotificationsEnabled(true);
        assertTrue(user.isNotificationsEnabled());
    }

    @Test
    public void paramConstructor_notificationsEnabledByDefault() {
        User user = new User("d1", "Name", "e@b.com", "123");
        assertTrue(user.isNotificationsEnabled());
    }

    @Test
    public void setUsername_updatesCorrectly() {
        User user = new User();
        user.setUsername("cool_user");
        assertEquals("cool_user", user.getUsername());
    }

    @Test
    public void setDescription_updatesCorrectly() {
        User user = new User();
        user.setDescription("I love events");
        assertEquals("I love events", user.getDescription());
    }

    @Test
    public void setCountry_updatesCorrectly() {
        User user = new User();
        user.setCountry("Canada");
        assertEquals("Canada", user.getCountry());
    }

    @Test
    public void setAddress_updatesCorrectly() {
        User user = new User();
        user.setAddress("123 Main St");
        assertEquals("123 Main St", user.getAddress());
    }

    @Test
    public void paramConstructor_setsEmptyDescriptionCountryAddress() {
        User user = new User("d1", "Name", "e@b.com", "123");
        assertEquals("", user.getDescription());
        assertEquals("", user.getCountry());
        assertEquals("", user.getAddress());
    }

    @Test
    public void multipleRolesCanBeSetSimultaneously() {
        User user = new User();
        user.setEntrant(true);
        user.setOrganizer(true);
        user.setAdmin(true);
        assertTrue(user.isEntrant());
        assertTrue(user.isOrganizer());
        assertTrue(user.isAdmin());
    }

    @Test
    public void allRolesCanBeDisabled() {
        User user = new User();
        user.setEntrant(false);
        user.setOrganizer(false);
        user.setAdmin(false);
        assertFalse(user.isEntrant());
        assertFalse(user.isOrganizer());
        assertFalse(user.isAdmin());
    }
}
