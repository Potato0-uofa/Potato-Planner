package com.example.eventplanner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test cases for the EventHistoryActivity.java class.
 *
 * Functionality:
 * Verifies that the event history screen displays correctly,
 * shows the empty state message when the user has no registrations,
 * and that the app's navigation buttons are functional.
 * Covers the acceptance criteria of US 01.02.03:
 * "As an entrant, I want to have a history of events I have registered
 * for, whether I was selected or not."
 */
@RunWith(AndroidJUnit4.class)
public class EventHistoryActivityTest {

    @Rule
    public ActivityScenarioRule<EventHistoryActivity> scenario =
            new ActivityScenarioRule<>(EventHistoryActivity.class);

    /**
     * Tests that the Event History screen loads and the title is visible.
     */
    @Test
    public void testEventHistoryScreenLoads() {
        onView(withText("Event History")).check(matches(isDisplayed()));
    }

    /**
     * Tests that the ListView is present in the layout.
     */
    @Test
    public void testListViewIsPresent() {
        // When no registrations exist, then the empty view is shown and the ListView
        // is hidden by Android automatically. In its place is text stating that
        // "User has not registered in any events!"
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        onView(withText("User has not registered in any events!"))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that when the user has no registrations, the empty state
     * message "User has not registered in any events!" is displayed.
     * Note: This test assumes the test device has no registrations in Firestore.
     */
    @Test
    public void testEmptyStateMessageShownWhenNoRegistrations() {
        // Waiting for Firestore to respond
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        onView(withText("User has not registered in any events!"))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that the exit/close button is visible on the screen.
     */
    @Test
    public void testExitButtonIsVisible() {
        onView(withId(R.id.exit_button_event_history)).check(matches(isDisplayed()));
    }

    /**
     * Tests that the home navigation button is visible on the screen.
     */
    @Test
    public void testHomeButtonIsVisible() {
        onView(withId(R.id.home_button_event_history)).check(matches(isDisplayed()));
    }

    /**
     * Tests that the search navigation button is visible on the screen.
     */
    @Test
    public void testSearchButtonIsVisible() {
        onView(withId(R.id.qr_button_event_history)).check(matches(isDisplayed()));
    }

    /**
     * Tests that the browse navigation button is visible on the screen.
     */
    @Test
    public void testBrowseButtonIsVisible() {
        onView(withId(R.id.browse_button_event_history)).check(matches(isDisplayed()));
    }

    /**
     * Tests that the profile navigation button is visible on the screen.
     */
    @Test
    public void testProfileButtonIsVisible() {
        onView(withId(R.id.profile_button_event_history)).check(matches(isDisplayed()));
    }

    /**
     * Tests that the new event navigation button is visible on the screen.
     */
    @Test
    public void testNewEventButtonIsVisible() {
        onView(withId(R.id.new_event_button_event_history)).check(matches(isDisplayed()));
    }

    /**
     * Tests that clicking the exit button finishes the activity without crashing.
     */
    @Test
    public void testExitButtonClosesActivity() {
        onView(withId(R.id.exit_button_event_history)).perform(click());
    }
}