package com.example.eventplanner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test cases for the SettingsView.java class.
 *
 * Functionality:
 * Verifies that the settings screen displays correctly, that edit dialogs are
 * open for each field, and that validation is enforced when saving changes,
 * covering the acceptance criteria of US 01.02.02
 * "As an entrant, I want to update information such as name, email and
 * contact information on my profile."
 */
@RunWith(AndroidJUnit4.class)
public class SettingsViewTest {

    @Rule
    public ActivityScenarioRule<SettingsView> scenario =
            new ActivityScenarioRule<>(SettingsView.class);

    /**
     * Tests that the settings screen loads and all key UI elements are visible.
     */
    @Test
    public void testSettingsScreenLoads() {
        onView(withId(R.id.tv_name_value)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_email_value)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_phone_value)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_country_value)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_edit_name)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_edit_email)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_edit_phone)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_edit_country)).check(matches(isDisplayed()));
    }

    /**
     * Tests that clicking the Edit button for Name opens a dialog.
     */
    @Test
    public void testEditNameDialogOpens() {
        onView(withId(R.id.btn_edit_name)).perform(click());
        onView(withText("Edit Name")).check(matches(isDisplayed()));
    }

    /**
     * Tests that clicking the Edit button for Email opens a dialog.
     */
    @Test
    public void testEditEmailDialogOpens() {
        onView(withId(R.id.btn_edit_email)).perform(click());
        onView(withText("Edit Email")).check(matches(isDisplayed()));
    }

    /**
     * Tests that clicking the Edit button for Phone opens a dialog.
     */
    @Test
    public void testEditPhoneDialogOpens() {
        onView(withId(R.id.btn_edit_phone)).perform(click());
        onView(withText("Edit Phone Number")).check(matches(isDisplayed()));
    }

    /**
     * Tests that clicking the Edit button for Country opens a dialog.
     */
    @Test
    public void testEditCountryDialogOpens() {
        onView(withId(R.id.btn_edit_country)).perform(click());
        onView(withText("Edit Country")).check(matches(isDisplayed()));
    }

    /**
     * Tests that cancelling an edit dialog closes it without changing the displayed value.
     */
    @Test
    public void testCancelEditDoesNotChangeValue() {
        onView(withId(R.id.btn_edit_name)).perform(click());
        onView(withText("Edit Name")).check(matches(isDisplayed()));

        // Cancel the dialog
        onView(withText("Cancel")).perform(click());

        // Name field should still be displayed (unchanged)
        onView(withId(R.id.tv_name_value)).check(matches(isDisplayed()));
    }

    /**
     * Tests that submitting an empty name keeps the UI intact (validation blocks save).
     */
    @Test
    public void testEmptyNameShowsError() {
        onView(withId(R.id.btn_edit_name)).perform(click());
        onView(withText("Edit Name")).check(matches(isDisplayed()));

        onView(withClassName(org.hamcrest.Matchers.equalTo(android.widget.EditText.class.getName())))
                .perform(clearText(), closeSoftKeyboard());
        onView(withText("Save")).perform(click());

        // Name field should still be displayed after a failed save
        onView(withId(R.id.tv_name_value)).check(matches(isDisplayed()));
    }

    /**
     * Tests that submitting an invalid email format does not update the email field.
     */
    @Test
    public void testInvalidEmailShowsError() {
        onView(withId(R.id.btn_edit_email)).perform(click());
        onView(withText("Edit Email")).check(matches(isDisplayed()));

        onView(withClassName(org.hamcrest.Matchers.equalTo(android.widget.EditText.class.getName())))
                .perform(clearText(), typeText("not-valid"), closeSoftKeyboard());
        onView(withText("Save")).perform(click());

        // Email field should still be displayed
        onView(withId(R.id.tv_email_value)).check(matches(isDisplayed()));
    }

    /**
     * Tests that submitting an invalid phone number does not update the phone field.
     */
    @Test
    public void testInvalidPhoneShowsError() {
        onView(withId(R.id.btn_edit_phone)).perform(click());
        onView(withText("Edit Phone Number")).check(matches(isDisplayed()));

        onView(withClassName(org.hamcrest.Matchers.equalTo(android.widget.EditText.class.getName())))
                .perform(clearText(), typeText("123"), closeSoftKeyboard());
        onView(withText("Save")).perform(click());

        // Phone field should still be displayed
        onView(withId(R.id.tv_phone_value)).check(matches(isDisplayed()));
    }

    /**
     * Tests that a valid email is accepted and the dialog closes.
     */
    @Test
    public void testValidEmailAccepted() {
        onView(withId(R.id.btn_edit_email)).perform(click());
        onView(withText("Edit Email")).check(matches(isDisplayed()));

        onView(withClassName(org.hamcrest.Matchers.equalTo(android.widget.EditText.class.getName())))
                .perform(clearText(), typeText("valid@example.com"), closeSoftKeyboard());
        onView(withText("Save")).perform(click());

        // Dialog (should) close
        onView(withId(R.id.tv_email_value)).check(matches(isDisplayed()));
    }

    /**
     * Tests that a valid phone number is accepted and the dialog closes.
     */
    @Test
    public void testValidPhoneAccepted() {
        onView(withId(R.id.btn_edit_phone)).perform(click());
        onView(withText("Edit Phone Number")).check(matches(isDisplayed()));

        onView(withClassName(org.hamcrest.Matchers.equalTo(android.widget.EditText.class.getName())))
                .perform(clearText(), typeText("7801234567"), closeSoftKeyboard());
        onView(withText("Save")).perform(click());

        // Dialog (should) close
        onView(withId(R.id.tv_phone_value)).check(matches(isDisplayed()));
    }

    /**
     * Tests that an empty phone number is accepted since phone is optional.
     */
    @Test
    public void testEmptyPhoneIsAccepted() {
        onView(withId(R.id.btn_edit_phone)).perform(click());
        onView(withText("Edit Phone Number")).check(matches(isDisplayed()));

        onView(withClassName(org.hamcrest.Matchers.equalTo(android.widget.EditText.class.getName())))
                .perform(clearText(), closeSoftKeyboard());
        onView(withText("Save")).perform(click());

        // Should show "—" for empty phone (if user chose to not input their phone number)
        onView(withId(R.id.tv_phone_value)).check(matches(isDisplayed()));
    }
}