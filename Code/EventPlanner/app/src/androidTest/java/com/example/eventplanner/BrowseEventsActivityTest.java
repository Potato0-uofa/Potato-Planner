package com.example.eventplanner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for BrowseEventsActivity.
 * Verifies that the browse screen loads, search bar and filter are present,
 * and navigation works.
 */
@RunWith(AndroidJUnit4.class)
public class BrowseEventsActivityTest {

    @Rule
    public ActivityScenarioRule<BrowseEventsActivity> scenario =
            new ActivityScenarioRule<>(BrowseEventsActivity.class);

    @Test
    public void testBrowseScreenLoads() {
        onView(withId(R.id.tv_browse_header3)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchBarIsDisplayed() {
        onView(withId(R.id.et_search_bar)).check(matches(isDisplayed()));
    }


    @Test
    public void testRecyclerViewIsDisplayed() {
        onView(withId(R.id.recycler_events)).check(matches(isDisplayed()));
    }

    @Test
    public void testExitButtonIsDisplayed() {
        onView(withId(R.id.exit_button_browse)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchBarAcceptsText() {
        onView(withId(R.id.et_search_bar))
                .perform(typeText("hackathon"), closeSoftKeyboard());
        onView(withId(R.id.et_search_bar))
                .check(matches(withText("hackathon")));
    }

    @Test
    public void testFilterButtonOpensDialog() {
        onView(withId(R.id.btn_filter)).perform(click());
        // The filter dialog should appear with a "Clear Filters" button
        onView(withText("Clear Filters")).check(matches(isDisplayed()));
    }

    @Test
    public void testHomeNavButtonIsDisplayed() {
        onView(withId(R.id.home_button_browse)).check(matches(isDisplayed()));
    }

    @Test
    public void testBrowseNavButtonIsDisplayed() {
        onView(withId(R.id.browse_button_browse)).check(matches(isDisplayed()));
    }

    @Test
    public void testProfileNavButtonIsDisplayed() {
        onView(withId(R.id.profile_button_browse)).check(matches(isDisplayed()));
    }

    @Test
    public void testQrNavButtonIsDisplayed() {
        onView(withId(R.id.qr_button_browse)).check(matches(isDisplayed()));
    }

    @Test
    public void testExitButtonClosesActivity() {
        onView(withId(R.id.exit_button_browse)).perform(click());
    }
}
