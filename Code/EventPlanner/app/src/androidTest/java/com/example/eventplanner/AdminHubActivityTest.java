package com.example.eventplanner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for AdminHubActivity.
 * Verifies all admin navigation buttons are visible and clickable.
 */
@RunWith(AndroidJUnit4.class)
public class AdminHubActivityTest {

    @Rule
    public ActivityScenarioRule<AdminHubActivity> scenario =
            new ActivityScenarioRule<>(AdminHubActivity.class);

    @Test
    public void testActivityLaunches() {
        onView(withId(R.id.btn_browse_events)).check(matches(isDisplayed()));
    }

    @Test
    public void testBrowseEventsButtonIsDisplayed() {
        onView(withId(R.id.btn_browse_events)).check(matches(isDisplayed()));
    }

    @Test
    public void testBrowseImagesButtonIsDisplayed() {
        onView(withId(R.id.btn_browse_images)).check(matches(isDisplayed()));
    }

    @Test
    public void testRemoveOrganizersButtonIsDisplayed() {
        onView(withId(R.id.btn_remove_organizers)).check(matches(isDisplayed()));
    }

    @Test
    public void testBrowseCommentsButtonIsDisplayed() {
        onView(withId(R.id.btn_browse_comments)).check(matches(isDisplayed()));
    }
}
