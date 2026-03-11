package com.example.eventplanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class UserTest {

    @Test
    public void defaultConstructor_createsUser() {
        User user = new User();
        assertNotNull(user);
    }

    @Test
    public void constructor_setsFieldsCorrectly() {
        User user = new User("device123", "Priyansha", "priyansha@email.com", "1234567890");

        assertEquals("device123", user.getDeviceId());
        assertEquals("Priyansha", user.getName());
        assertEquals("priyansha@email.com", user.getEmail());
        assertEquals("1234567890", user.getPhone());
    }

    @Test
    public void setters_updateFieldsCorrectly() {
        User user = new User();

        user.setDeviceId("abc");
        user.setName("Alex");
        user.setEmail("alex@email.com");
        user.setPhone("5551234");

        assertEquals("abc", user.getDeviceId());
        assertEquals("Alex", user.getName());
        assertEquals("alex@email.com", user.getEmail());
        assertEquals("5551234", user.getPhone());
    }
}