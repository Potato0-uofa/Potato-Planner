package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;

public class OrganizerTest {

    @Test
    public void defaultConstructor_createsOrganizer() {
        Organizer org = new Organizer();
        assertNotNull(org);
    }

    @Test
    public void parameterizedConstructor_setsFieldsCorrectly() {
        Organizer org = new Organizer("device1", "OrgUser", "org@test.com", "555-0002");

        assertEquals("device1", org.getDeviceId());
        assertEquals("OrgUser", org.getName());
        assertEquals("org@test.com", org.getEmail());
        assertEquals("555-0002", org.getPhone());
    }

    @Test
    public void organizer_isSubclassOfUser() {
        Organizer org = new Organizer("d1", "Org", "o@b.com", "123");
        assertTrue(org instanceof User);
    }

    @Test
    public void viewWaitingList_emptyList_returnsEmpty() {
        Organizer org = new Organizer("d1", "Org", "o@b.com", "123");
        Events event = new Events("Test", "2026-01-01", "desc", "loc");

        ArrayList<Entrant> waitlist = org.viewWaitingList(event);
        assertNotNull(waitlist);
        assertTrue(waitlist.isEmpty());
    }

    @Test
    public void viewWaitingList_withEntrants_returnsAll() {
        Organizer org = new Organizer("d1", "Org", "o@b.com", "123");
        Events event = new Events("Test", "2026-01-01", "desc", "loc");

        Entrant e1 = new Entrant("ed1", "E1", "e1@test.com", "111");
        Entrant e2 = new Entrant("ed2", "E2", "e2@test.com", "222");
        event.addToWaitingList(e1);
        event.addToWaitingList(e2);

        ArrayList<Entrant> waitlist = org.viewWaitingList(event);
        assertEquals(2, waitlist.size());
    }

    @Test
    public void sendNotification_entrantWithNotifications_logsNotification() {
        Organizer org = new Organizer("d1", "OrgName", "o@b.com", "123");
        Entrant entrant = new Entrant("ed1", "EntrantName", "e@b.com", "456");
        Events event = new Events("Hackathon", "2026-01-01", "desc", "loc");
        EventList logs = new EventList();

        org.sendNotification(entrant, event, "You're invited!", "2026-01-01", "invitation", logs);

        ArrayList<Notification> notifications = logs.getNotificationLogs();
        assertEquals(1, notifications.size());
        assertEquals("You're invited!", notifications.get(0).getMessage());
        assertEquals("OrgName", notifications.get(0).getSenderName());
        assertEquals("EntrantName", notifications.get(0).getReceiverName());
        assertEquals("Hackathon", notifications.get(0).getEventName());
    }

    @Test
    public void sendNotification_entrantOptedOut_doesNotLog() {
        Organizer org = new Organizer("d1", "OrgName", "o@b.com", "123");
        Entrant entrant = new Entrant("ed1", "EntrantName", "e@b.com", "456");
        entrant.optOutNotifications();
        Events event = new Events("Hackathon", "2026-01-01", "desc", "loc");
        EventList logs = new EventList();

        org.sendNotification(entrant, event, "You're invited!", "2026-01-01", "invitation", logs);

        ArrayList<Notification> notifications = logs.getNotificationLogs();
        assertTrue(notifications.isEmpty());
    }

    @Test
    public void sendNotification_nullEntrant_doesNotLog() {
        Organizer org = new Organizer("d1", "OrgName", "o@b.com", "123");
        Events event = new Events("Hackathon", "2026-01-01", "desc", "loc");
        EventList logs = new EventList();

        org.sendNotification(null, event, "msg", "2026-01-01", "type", logs);

        ArrayList<Notification> notifications = logs.getNotificationLogs();
        assertTrue(notifications.isEmpty());
    }

    @Test
    public void sendNotification_nullLogs_doesNotCrash() {
        Organizer org = new Organizer("d1", "OrgName", "o@b.com", "123");
        Entrant entrant = new Entrant("ed1", "EntrantName", "e@b.com", "456");
        Events event = new Events("Hackathon", "2026-01-01", "desc", "loc");

        // Should not throw
        org.sendNotification(entrant, event, "msg", "2026-01-01", "type", null);
    }

    @Test
    public void organizer_inheritsUserProperties() {
        Organizer org = new Organizer("d1", "Org", "o@b.com", "123");
        org.setUsername("org_user");
        org.setDescription("Event organizer");

        assertEquals("org_user", org.getUsername());
        assertEquals("Event organizer", org.getDescription());
    }
}
