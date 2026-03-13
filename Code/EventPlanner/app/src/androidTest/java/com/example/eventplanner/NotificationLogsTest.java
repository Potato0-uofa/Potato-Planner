package com.example.eventplanner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test cases for {@link NotificationLogs}.
 *
 * Functionality covered:
 * - Verifies that invitation response controls are visible.
 * - Verifies that decline action can be triggered from the UI without crashing.
 *
 * Related user story:
 * - US 01.05.03: Entrant declines invitation
 */
@RunWith(AndroidJUnit4.class)
public class NotificationLogsTest {

    /**
     * Verifies that the decline button is visible when the screen loads.
     */
    @Test
    public void declineButton_isDisplayed() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                NotificationLogs.class
        );
        intent.putExtra("eventId", "TEST_EVENT_ID");

        try (ActivityScenario<NotificationLogs> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.decline_notification_button))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Verifies that clicking decline executes the action path without UI failure.
     */
    @Test
    public void declineButton_isClickable() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                NotificationLogs.class
        );
        intent.putExtra("eventId", "TEST_EVENT_ID");

        try (ActivityScenario<NotificationLogs> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.decline_notification_button))
                    .perform(click());
        }
    }
}