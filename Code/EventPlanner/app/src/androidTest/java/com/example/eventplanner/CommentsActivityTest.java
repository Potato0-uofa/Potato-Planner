package com.example.eventplanner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for CommentsActivity.
 * Uses an intent with a test eventId since the activity requires one.
 */
@RunWith(AndroidJUnit4.class)
public class CommentsActivityTest {

    private ActivityScenario<CommentsActivity> launchWithEventId(String eventId) {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                CommentsActivity.class
        );
        intent.putExtra("eventId", eventId);
        return ActivityScenario.launch(intent);
    }

    @Test
    public void testScreenLoads() {
        try (ActivityScenario<CommentsActivity> s = launchWithEventId("TEST_EVENT")) {
            onView(withId(R.id.comments_title)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testTitleText() {
        try (ActivityScenario<CommentsActivity> s = launchWithEventId("TEST_EVENT")) {
            onView(withId(R.id.comments_title)).check(matches(withText("Comments")));
        }
    }

    @Test
    public void testCommentInputIsDisplayed() {
        try (ActivityScenario<CommentsActivity> s = launchWithEventId("TEST_EVENT")) {
            onView(withId(R.id.comment_input)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testPostButtonIsDisplayed() {
        try (ActivityScenario<CommentsActivity> s = launchWithEventId("TEST_EVENT")) {
            onView(withId(R.id.post_comment_button)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testRecyclerViewIsDisplayed() {
        try (ActivityScenario<CommentsActivity> s = launchWithEventId("TEST_EVENT")) {
            onView(withId(R.id.comments_recycler_view)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testExitButtonIsDisplayed() {
        try (ActivityScenario<CommentsActivity> s = launchWithEventId("TEST_EVENT")) {
            onView(withId(R.id.exit_button_comments)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testExitButtonClosesActivity() {
        try (ActivityScenario<CommentsActivity> s = launchWithEventId("TEST_EVENT")) {
            onView(withId(R.id.exit_button_comments)).perform(click());
        }
    }
}
