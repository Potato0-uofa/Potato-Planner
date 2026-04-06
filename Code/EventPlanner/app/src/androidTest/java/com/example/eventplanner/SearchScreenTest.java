package com.example.eventplanner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for SearchScreen (QR scanner) activity.
 * Verifies that the QR scan screen loads and UI elements are present.
 */
@RunWith(AndroidJUnit4.class)
public class SearchScreenTest {

    @Rule
    public ActivityScenarioRule<SearchScreen> scenario =
            new ActivityScenarioRule<>(SearchScreen.class);

    @Test
    public void testScreenLoads() {
        onView(withId(R.id.tv_qr_title)).check(matches(isDisplayed()));
    }

    @Test
    public void testTitleText() {
        onView(withId(R.id.tv_qr_title)).check(matches(withText("Scan QR Code")));
    }

    @Test
    public void testSubtitleIsDisplayed() {
        onView(withId(R.id.tv_qr_subtitle)).check(matches(isDisplayed()));
    }

    @Test
    public void testScanButtonIsDisplayed() {
        onView(withId(R.id.btn_scan_qr)).check(matches(isDisplayed()));
    }

    @Test
    public void testHomeNavButtonIsDisplayed() {
        onView(withId(R.id.home_button_search)).check(matches(isDisplayed()));
    }

    @Test
    public void testBrowseNavButtonIsDisplayed() {
        onView(withId(R.id.browse_button_search)).check(matches(isDisplayed()));
    }

    @Test
    public void testProfileNavButtonIsDisplayed() {
        onView(withId(R.id.profile_button_search)).check(matches(isDisplayed()));
    }

    @Test
    public void testQrNavButtonIsDisplayed() {
        onView(withId(R.id.qr_button_search)).check(matches(isDisplayed()));
    }

    @Test
    public void testNewEventNavButtonIsDisplayed() {
        onView(withId(R.id.new_event_button_search)).check(matches(isDisplayed()));
    }

    @Test
    public void testToolbarIsDisplayed() {
        onView(withId(R.id.home_bar)).check(matches(isDisplayed()));
    }
}
