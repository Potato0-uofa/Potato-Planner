package com.example.eventplanner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test cases for the ProfileSetup.java class.
 *
 * Functionality:
 * Verifies input validation for the user profile setup screen,
 * covering the acceptance criteria of US 01.02.01:
 * "As an entrant, I want to provide my personal information such as
 * name, email and optional phone number in the app."
 */
@RunWith(AndroidJUnit4.class)
public class ProfileSetupTest {

    @Rule
    public ActivityScenarioRule<ProfileSetup> scenario =
            new ActivityScenarioRule<>(ProfileSetup.class);

    /**
     * Tests that submitting with all fields empty shows errors on name and email.
     */
    @Test
    public void testEmptyFieldsShowErrors() {
        // Click confirm without entering anything
        onView(withId(R.id.btn_save_profile)).perform(click());

        // Name and email errors should appear
        onView(withId(R.id.edit_name)).check(matches(hasErrorText("Name is required!")));
        onView(withId(R.id.edit_email)).check(matches(hasErrorText("Email is required!")));
    }

    /**
     * Tests that submitting with only name filled shows an error on email.
     */
    @Test
    public void testMissingEmailShowsError() {
        onView(withId(R.id.edit_name)).perform(typeText("John Doe"), closeSoftKeyboard());

        onView(withId(R.id.btn_save_profile)).perform(click());

        onView(withId(R.id.edit_email)).check(matches(hasErrorText("Email is required!")));
    }

    /**
     * Tests that submitting with only email filled shows an error on name.
     */
    @Test
    public void testMissingNameShowsError() {
        onView(withId(R.id.edit_email)).perform(typeText("john@example.com"), closeSoftKeyboard());

        onView(withId(R.id.btn_save_profile)).perform(click());

        onView(withId(R.id.edit_name)).check(matches(hasErrorText("Name is required!")));
    }

    /**
     * Tests that an improperly formatted email shows a format error.
     */
    @Test
    public void testInvalidEmailFormatShowsError() {
        onView(withId(R.id.edit_name)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.edit_email)).perform(typeText("not-an-email"), closeSoftKeyboard());

        onView(withId(R.id.btn_save_profile)).perform(click());

        onView(withId(R.id.edit_email)).check(matches(hasErrorText("Enter a valid email address")));
    }

    /**
     * Tests that an invalid phone number (too short) shows a format error.
     */
    @Test
    public void testInvalidPhoneShowsError() {
        onView(withId(R.id.edit_name)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.edit_email)).perform(typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.edit_phone)).perform(typeText("123"), closeSoftKeyboard());

        onView(withId(R.id.btn_save_profile)).perform(click());

        onView(withId(R.id.edit_phone)).check(matches(hasErrorText("Enter a valid phone number (10-15 digits)")));
    }

    /**
     * Tests that a valid name and email with no phone passes validation.
     * A phone number is optional (US 01.02.01)
     */
    @Test
    public void testValidNameAndEmailNoPhone() {
        onView(withId(R.id.edit_name)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.edit_email)).perform(typeText("john@example.com"), closeSoftKeyboard());

        // No error should be set on any field after clicking confirm
        onView(withId(R.id.edit_name)).check(matches(hasErrorText((String) null)));
        onView(withId(R.id.edit_email)).check(matches(hasErrorText((String) null)));
    }

    /**
     * Tests that valid name, email, and a properly formatted phone number passes validation.
     */
    @Test
    public void testValidNameEmailAndPhone() {
        onView(withId(R.id.edit_name)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.edit_email)).perform(typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.edit_phone)).perform(typeText("7801234567"), closeSoftKeyboard());

        // No error should be set on any of the 3 fields
        onView(withId(R.id.edit_name)).check(matches(hasErrorText((String) null)));
        onView(withId(R.id.edit_email)).check(matches(hasErrorText((String) null)));
        onView(withId(R.id.edit_phone)).check(matches(hasErrorText((String) null)));
    }

    /**
     * Tests that a phone number with a leading + and 11 digits is accepted.
     */
    @Test
    public void testValidInternationalPhone() {
        onView(withId(R.id.edit_name)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.edit_email)).perform(typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.edit_phone)).perform(typeText("+17801234567"), closeSoftKeyboard());

        onView(withId(R.id.edit_phone)).check(matches(hasErrorText((String) null)));
    }

    /**
     * Tests that a name with more than two words (ex: middle name) is accepted.
     */
    @Test
    public void testNameWithMiddleNameAccepted() {
        onView(withId(R.id.edit_name)).perform(typeText("John Michael Doe"), closeSoftKeyboard());
        onView(withId(R.id.edit_email)).perform(typeText("john@example.com"), closeSoftKeyboard());

        onView(withId(R.id.edit_name)).check(matches(hasErrorText((String) null)));
        onView(withId(R.id.edit_email)).check(matches(hasErrorText((String) null)));
    }
}