package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;

public class OrganizerEntrantInteractionTest {

    private Organizer organizer;
    private Events event;
    private EventList logs;

    @Before
    public void setUp() {
        organizer = new Organizer("org1", "OrganizerName", "org@test.com", "111");
        event = new Events("Conference", "2026-06-15", "Tech conference", "Calgary");
        logs = new EventList();
    }

    @Test
    public void organizer_addsEntrantsToWaitlist_viewsAll() {
        Entrant e1 = new Entrant("d1", "Alice", "a@b.com", "1");
        Entrant e2 = new Entrant("d2", "Bob", "b@b.com", "2");
        Entrant e3 = new Entrant("d3", "Charlie", "c@b.com", "3");

        event.addToWaitingList(e1);
        event.addToWaitingList(e2);
        event.addToWaitingList(e3);

        ArrayList<Entrant> waitlist = organizer.viewWaitingList(event);
        assertEquals(3, waitlist.size());
    }

    @Test
    public void organizer_sendsNotificationToMultipleEntrants() {
        Entrant e1 = new Entrant("d1", "Alice", "a@b.com", "1");
        Entrant e2 = new Entrant("d2", "Bob", "b@b.com", "2");

        organizer.sendNotification(e1, event, "Welcome!", "2026-06-01", "invitation", logs);
        organizer.sendNotification(e2, event, "Welcome!", "2026-06-01", "invitation", logs);

        assertEquals(2, logs.getNotificationLogs().size());
    }

    @Test
    public void organizer_sendsToMixOfOptedInAndOut() {
        Entrant opted_in = new Entrant("d1", "Alice", "a@b.com", "1");
        Entrant opted_out = new Entrant("d2", "Bob", "b@b.com", "2");
        opted_out.optOutNotifications();

        organizer.sendNotification(opted_in, event, "msg", "time", "type", logs);
        organizer.sendNotification(opted_out, event, "msg", "time", "type", logs);

        assertEquals(1, logs.getNotificationLogs().size());
        assertEquals("Alice", logs.getNotificationLogs().get(0).getReceiverName());
    }

    @Test
    public void admin_reviewsOrganizerNotifications() {
        Admin admin = new Admin("adm1", "AdminUser", "admin@test.com", "999");

        Entrant e = new Entrant("d1", "Alice", "a@b.com", "1");
        organizer.sendNotification(e, event, "You won!", "2026-06-01", "lottery", logs);

        ArrayList<Notification> reviewed = admin.reviewNotificationLogs(logs);
        assertEquals(1, reviewed.size());
        assertEquals("You won!", reviewed.get(0).getMessage());
        assertEquals("OrganizerName", reviewed.get(0).getSenderName());
    }

    @Test
    public void entrant_joinsAndLeavesWaitlist() {
        Entrant e = new Entrant("d1", "Alice", "a@b.com", "1");

        event.addToWaitingList(e);
        assertTrue(event.isOnWaitingList(e));
        assertEquals(1, organizer.viewWaitingList(event).size());

        event.removeFromWaitingList(e);
        assertFalse(event.isOnWaitingList(e));
        assertEquals(0, organizer.viewWaitingList(event).size());
    }

    @Test
    public void entrant_optOutThenOptIn_receivesNotification() {
        Entrant e = new Entrant("d1", "Alice", "a@b.com", "1");

        e.optOutNotifications();
        organizer.sendNotification(e, event, "msg1", "t1", "type", logs);
        assertEquals(0, logs.getNotificationLogs().size());

        e.optInNotifications();
        organizer.sendNotification(e, event, "msg2", "t2", "type", logs);
        assertEquals(1, logs.getNotificationLogs().size());
    }

    @Test
    public void waitlistLimit_preventsExcessEntrants() {
        event.setWaitlistLimit(2);

        event.addToWaitingList(new Entrant("d1", "A", "a@b.com", "1"));
        event.addToWaitingList(new Entrant("d2", "B", "b@b.com", "2"));

        try {
            event.addToWaitingList(new Entrant("d3", "C", "c@b.com", "3"));
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }

        assertEquals(2, organizer.viewWaitingList(event).size());
    }
}
