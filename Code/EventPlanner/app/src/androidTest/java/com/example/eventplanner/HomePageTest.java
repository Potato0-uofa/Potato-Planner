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
 * Instrumented tests for HomePage activity.
 * Verifies that the home screen loads correctly and navigation elements are present.
 */
@RunWith(AndroidJUnit4.class)
public class HomePageTest {

    @Rule
    public ActivityScenarioRule<HomePage> scenario =
            new ActivityScenarioRule<>(HomePage.class);

    @Test
    public void testHomePageLoads() {
        onView(withId(R.id.homepage)).check(matches(isDisplayed()));
    }

    @Test
    public void testCarouselHeaderIsDisplayed() {
        onView(withId(R.id.carousel_header)).check(matches(isDisplayed()));
        onView(withId(R.id.carousel_header)).check(matches(withText("Discover Events")));
    }

    @Test
    public void testCarouselSubheaderIsDisplayed() {
        onView(withId(R.id.carousel_subheader)).check(matches(isDisplayed()));
    }


    @Test
    public void testNotificationButtonIsDisplayed() {
        onView(withId(R.id.notification_button_home)).check(matches(isDisplayed()));
    }

    @Test
    public void testHomeNavButtonIsDisplayed() {
        onView(withId(R.id.home_button_home)).check(matches(isDisplayed()));
    }

    @Test
    public void testBrowseNavButtonIsDisplayed() {
        onView(withId(R.id.browse_button_home)).check(matches(isDisplayed()));
    }

    @Test
    public void testProfileNavButtonIsDisplayed() {
        onView(withId(R.id.profile_button_home)).check(matches(isDisplayed()));
    }

    @Test
    public void testQrNavButtonIsDisplayed() {
        onView(withId(R.id.qr_button_home)).check(matches(isDisplayed()));
    }

    @Test
    public void testNewEventNavButtonIsDisplayed() {
        onView(withId(R.id.new_event_button_home)).check(matches(isDisplayed()));
    }

    @Test
    public void testToolbarIsDisplayed() {
        onView(withId(R.id.home_bar)).check(matches(isDisplayed()));
    }
}
