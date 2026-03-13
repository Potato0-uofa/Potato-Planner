package com.example.eventplanner;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Smoke test for AdminBrowseEventsActivity.
 * Passes if activity launches without crashing.
 */
@RunWith(AndroidJUnit4.class)
public class AdminBrowseEventsActivityTest {

    @Rule
    public ActivityScenarioRule<AdminBrowseEventsActivity> activityRule =
            new ActivityScenarioRule<>(AdminBrowseEventsActivity.class);

    @Test
    public void activity_launches() {
// No-op. Launch is verified by ActivityScenarioRule.
    }
}
