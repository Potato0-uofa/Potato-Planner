package com.example.eventplanner;

import static org.junit.Assert.*;
import org.junit.Test;

public class CommentTest {

    @Test
    public void defaultConstructor_createsComment() {
        Comment c = new Comment();
        assertNotNull(c);
    }

    @Test
    public void parameterizedConstructor_setsFieldsCorrectly() {
        Comment c = new Comment("c1", "device1", "Alice", "Great event!", null);

        assertEquals("c1", c.getCommentId());
        assertEquals("device1", c.getDeviceId());
        assertEquals("Alice", c.getAuthorName());
        assertEquals("Great event!", c.getText());
        assertNull(c.getCreatedAt());
    }

    @Test
    public void defaultConstructor_fieldsAreNull() {
        Comment c = new Comment();

        assertNull(c.getCommentId());
        assertNull(c.getDeviceId());
        assertNull(c.getAuthorName());
        assertNull(c.getText());
        assertNull(c.getCreatedAt());
    }

    @Test
    public void setCommentId_updatesCorrectly() {
        Comment c = new Comment();
        c.setCommentId("comment-99");
        assertEquals("comment-99", c.getCommentId());
    }

    @Test
    public void setDeviceId_updatesCorrectly() {
        Comment c = new Comment();
        c.setDeviceId("device-abc");
        assertEquals("device-abc", c.getDeviceId());
    }

    @Test
    public void setAuthorName_updatesCorrectly() {
        Comment c = new Comment();
        c.setAuthorName("Bob");
        assertEquals("Bob", c.getAuthorName());
    }

    @Test
    public void setText_updatesCorrectly() {
        Comment c = new Comment();
        c.setText("This is a comment");
        assertEquals("This is a comment", c.getText());
    }

    @Test
    public void setters_allowEmptyStrings() {
        Comment c = new Comment();
        c.setCommentId("");
        c.setDeviceId("");
        c.setAuthorName("");
        c.setText("");

        assertEquals("", c.getCommentId());
        assertEquals("", c.getDeviceId());
        assertEquals("", c.getAuthorName());
        assertEquals("", c.getText());
    }

    @Test
    public void setters_allowNull() {
        Comment c = new Comment("c1", "d1", "Alice", "text", null);
        c.setCommentId(null);
        c.setText(null);
        assertNull(c.getCommentId());
        assertNull(c.getText());
    }

    @Test
    public void setText_overwritesPreviousValue() {
        Comment c = new Comment();
        c.setText("First");
        assertEquals("First", c.getText());
        c.setText("Second");
        assertEquals("Second", c.getText());
    }
}
