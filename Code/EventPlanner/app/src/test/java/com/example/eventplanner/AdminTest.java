package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;

public class AdminTest {

    @Test
    public void defaultConstructor_createsAdmin() {
        Admin admin = new Admin();
        assertNotNull(admin);
    }

    @Test
    public void parameterizedConstructor_setsFieldsCorrectly() {
        Admin admin = new Admin("device1", "AdminUser", "admin@test.com", "555-0001");

        assertEquals("device1", admin.getDeviceId());
        assertEquals("AdminUser", admin.getName());
        assertEquals("admin@test.com", admin.getEmail());
        assertEquals("555-0001", admin.getPhone());
    }

    @Test
    public void admin_isSubclassOfUser() {
        Admin admin = new Admin("d1", "Admin", "a@b.com", "123");
        assertTrue(admin instanceof User);
    }

    @Test
    public void reviewNotificationLogs_emptyLogs_returnsEmptyList() {
        Admin admin = new Admin("d1", "Admin", "a@b.com", "123");
        EventList logs = new EventList();

        ArrayList<Notification> result = admin.reviewNotificationLogs(logs);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void reviewNotificationLogs_withNotifications_returnsAll() {
        Admin admin = new Admin("d1", "Admin", "a@b.com", "123");
        EventList logs = new EventList();

        Notification n1 = new Notification("msg1", "sender1", "receiver1",
                "event1", "2026-01-01", "invitation");
        Notification n2 = new Notification("msg2", "sender2", "receiver2",
                "event2", "2026-01-02", "update");
        logs.addNotificationLog(n1);
        logs.addNotificationLog(n2);

        ArrayList<Notification> result = admin.reviewNotificationLogs(logs);
        assertEquals(2, result.size());
    }

    @Test
    public void reviewNotificationLogs_mixedItems_returnsOnlyNotifications() {
        Admin admin = new Admin("d1", "Admin", "a@b.com", "123");
        EventList logs = new EventList();

        // Add a non-notification item
        Events event = new Events("Test", "2026-01-01", "desc", "loc");
        logs.addEvent(event);

        // Add a notification
        Notification n = new Notification("msg", "sender", "receiver",
                "event", "2026-01-01", "type");
        logs.addNotificationLog(n);

        ArrayList<Notification> result = admin.reviewNotificationLogs(logs);
        assertEquals(1, result.size());
        assertEquals("msg", result.get(0).getMessage());
    }

    @Test
    public void admin_inheritsUserProperties() {
        Admin admin = new Admin("d1", "Admin", "a@b.com", "123");
        admin.setDescription("Admin user");
        admin.setCountry("Canada");
        admin.setAddress("123 Main St");

        assertEquals("Admin user", admin.getDescription());
        assertEquals("Canada", admin.getCountry());
        assertEquals("123 Main St", admin.getAddress());
    }
}
