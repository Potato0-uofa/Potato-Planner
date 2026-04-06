package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;

public class NotificationTest {

    @Test
    public void defaultConstructor_createsNotification() {
        Notification n = new Notification();
        assertNotNull(n);
    }

    @Test
    public void parameterizedConstructor_setsFieldsCorrectly() {
        Notification n = new Notification("Hello", "Alice", "Bob",
                "Hackathon", "2026-04-01", "invitation");

        assertEquals("Hello", n.getMessage());
        assertEquals("Alice", n.getSenderName());
        assertEquals("Bob", n.getReceiverName());
        assertEquals("Hackathon", n.getEventName());
        assertEquals("2026-04-01", n.getTimestamp());
        assertEquals("invitation", n.getType());
    }

    @Test
    public void defaultConstructor_fieldsAreNull() {
        Notification n = new Notification();

        assertNull(n.getNotificationId());
        assertNull(n.getMessage());
        assertNull(n.getSenderName());
        assertNull(n.getReceiverName());
        assertNull(n.getEventName());
        assertNull(n.getTimestamp());
        assertNull(n.getType());
        assertNull(n.getStatus());
        assertNull(n.getEventId());
    }

    @Test
    public void setNotificationId_updatesCorrectly() {
        Notification n = new Notification();
        n.setNotificationId("notif-123");
        assertEquals("notif-123", n.getNotificationId());
    }

    @Test
    public void setMessage_updatesCorrectly() {
        Notification n = new Notification();
        n.setMessage("You have been invited");
        assertEquals("You have been invited", n.getMessage());
    }

    @Test
    public void setSenderName_updatesCorrectly() {
        Notification n = new Notification();
        n.setSenderName("Organizer1");
        assertEquals("Organizer1", n.getSenderName());
    }

    @Test
    public void setReceiverName_updatesCorrectly() {
        Notification n = new Notification();
        n.setReceiverName("Entrant1");
        assertEquals("Entrant1", n.getReceiverName());
    }

    @Test
    public void setEventName_updatesCorrectly() {
        Notification n = new Notification();
        n.setEventName("Music Festival");
        assertEquals("Music Festival", n.getEventName());
    }

    @Test
    public void setTimestamp_updatesCorrectly() {
        Notification n = new Notification();
        n.setTimestamp("2026-05-01T10:00:00");
        assertEquals("2026-05-01T10:00:00", n.getTimestamp());
    }

    @Test
    public void setType_updatesCorrectly() {
        Notification n = new Notification();
        n.setType("update");
        assertEquals("update", n.getType());
    }

    @Test
    public void setStatus_updatesCorrectly() {
        Notification n = new Notification();
        n.setStatus("read");
        assertEquals("read", n.getStatus());
    }

    @Test
    public void setEventId_updatesCorrectly() {
        Notification n = new Notification();
        n.setEventId("event-456");
        assertEquals("event-456", n.getEventId());
    }

    @Test
    public void setters_allowEmptyStrings() {
        Notification n = new Notification();
        n.setMessage("");
        n.setSenderName("");
        n.setReceiverName("");
        n.setEventName("");
        n.setTimestamp("");
        n.setType("");

        assertEquals("", n.getMessage());
        assertEquals("", n.getSenderName());
        assertEquals("", n.getReceiverName());
        assertEquals("", n.getEventName());
        assertEquals("", n.getTimestamp());
        assertEquals("", n.getType());
    }

    @Test
    public void setters_allowNull() {
        Notification n = new Notification("msg", "sender", "receiver",
                "event", "time", "type");
        n.setMessage(null);
        n.setSenderName(null);
        assertNull(n.getMessage());
        assertNull(n.getSenderName());
    }
}
