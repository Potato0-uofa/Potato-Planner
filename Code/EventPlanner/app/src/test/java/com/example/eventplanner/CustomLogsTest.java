package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;

/**
 * Tests the CustomLogs abstract class via the EventList concrete subclass.
 */
public class CustomLogsTest {

    @Test
    public void addItem_increasesCount() {
        EventList logs = new EventList();
        logs.addItem("item1");
        assertEquals(1, logs.countItems());
    }

    @Test
    public void hasItem_existingItem_returnsTrue() {
        EventList logs = new EventList();
        String item = "test-item";
        logs.addItem(item);
        assertTrue(logs.hasItem(item));
    }

    @Test
    public void hasItem_nonExistingItem_returnsFalse() {
        EventList logs = new EventList();
        assertFalse(logs.hasItem("missing"));
    }

    @Test
    public void deleteItem_removesItem() {
        EventList logs = new EventList();
        String item = "to-delete";
        logs.addItem(item);
        logs.deleteItem(item);
        assertFalse(logs.hasItem(item));
        assertEquals(0, logs.countItems());
    }

    @Test
    public void countItems_emptyLog_returnsZero() {
        EventList logs = new EventList();
        assertEquals(0, logs.countItems());
    }

    @Test
    public void clearItems_removesAllItems() {
        EventList logs = new EventList();
        logs.addItem("a");
        logs.addItem("b");
        logs.addItem("c");
        assertEquals(3, logs.countItems());

        logs.clearItems();
        assertEquals(0, logs.countItems());
    }

    @Test
    public void addNotificationLog_nullNotification_doesNotAdd() {
        EventList logs = new EventList();
        logs.addNotificationLog(null);
        assertEquals(0, logs.countItems());
    }

    @Test
    public void addNotificationLog_validNotification_addsIt() {
        EventList logs = new EventList();
        Notification n = new Notification("msg", "sender", "receiver",
                "event", "time", "type");
        logs.addNotificationLog(n);
        assertEquals(1, logs.countItems());
    }

    @Test
    public void getNotificationLogs_returnsOnlyNotifications() {
        EventList logs = new EventList();

        // Add mixed items
        logs.addItem("string-item");
        Notification n = new Notification("msg", "sender", "receiver",
                "event", "time", "type");
        logs.addNotificationLog(n);
        logs.addItem(42);

        ArrayList<Notification> notifications = logs.getNotificationLogs();
        assertEquals(1, notifications.size());
        assertEquals("msg", notifications.get(0).getMessage());
    }

    @Test
    public void getNotificationLogs_empty_returnsEmptyList() {
        EventList logs = new EventList();
        ArrayList<Notification> notifications = logs.getNotificationLogs();
        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
    }

    @Test
    public void getNotificationLogs_multipleNotifications() {
        EventList logs = new EventList();
        logs.addNotificationLog(new Notification("m1", "s1", "r1", "e1", "t1", "type1"));
        logs.addNotificationLog(new Notification("m2", "s2", "r2", "e2", "t2", "type2"));
        logs.addNotificationLog(new Notification("m3", "s3", "r3", "e3", "t3", "type3"));

        ArrayList<Notification> notifications = logs.getNotificationLogs();
        assertEquals(3, notifications.size());
    }

    @Test
    public void clearItems_thenGetNotificationLogs_returnsEmpty() {
        EventList logs = new EventList();
        logs.addNotificationLog(new Notification("m", "s", "r", "e", "t", "ty"));
        logs.clearItems();

        ArrayList<Notification> notifications = logs.getNotificationLogs();
        assertTrue(notifications.isEmpty());
    }
}
