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
 * Instrumented tests for MyCreatedEventsActivity.
 * Verifies the created events screen loads and UI elements are present.
 */
@RunWith(AndroidJUnit4.class)
public class MyCreatedEventsActivityTest {

    @Rule
    public ActivityScenarioRule<MyCreatedEventsActivity> scenario =
            new ActivityScenarioRule<>(MyCreatedEventsActivity.class);

    @Test
    public void testScreenLoads() {
        onView(withId(R.id.tv_created_events_title)).check(matches(isDisplayed()));
    }

    @Test
    public void testTitleText() {
        onView(withId(R.id.tv_created_events_title))
                .check(matches(withText("My Created Events")));
    }

    @Test
    public void testListViewIsPresent() {
        onView(withId(R.id.list_created_events)).check(matches(isDisplayed()));
    }

    @Test
    public void testExitButtonIsDisplayed() {
        onView(withId(R.id.exit_button_created_events)).check(matches(isDisplayed()));
    }

    @Test
    public void testHomeNavButtonIsDisplayed() {
        onView(withId(R.id.home_button_created)).check(matches(isDisplayed()));
    }

    @Test
    public void testBrowseNavButtonIsDisplayed() {
        onView(withId(R.id.browse_button_created)).check(matches(isDisplayed()));
    }

    @Test
    public void testProfileNavButtonIsDisplayed() {
        onView(withId(R.id.profile_button_created)).check(matches(isDisplayed()));
    }

    @Test
    public void testQrNavButtonIsDisplayed() {
        onView(withId(R.id.qr_button_created)).check(matches(isDisplayed()));
    }

    @Test
    public void testNewEventNavButtonIsDisplayed() {
        onView(withId(R.id.new_event_button_created)).check(matches(isDisplayed()));
    }

    @Test
    public void testExitButtonClosesActivity() {
        onView(withId(R.id.exit_button_created_events)).perform(click());
    }
}
