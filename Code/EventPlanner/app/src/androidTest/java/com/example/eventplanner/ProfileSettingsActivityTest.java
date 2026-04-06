package com.example.eventplanner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for ProfileSettingsActivity.
 * Verifies the profile settings screen loads and all fields/buttons are present.
 */
@RunWith(AndroidJUnit4.class)
public class ProfileSettingsActivityTest {

    @Rule
    public ActivityScenarioRule<ProfileSettingsActivity> scenario =
            new ActivityScenarioRule<>(ProfileSettingsActivity.class);

    @Test
    public void testScreenLoads() {
        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
    }

    @Test
    public void testNameFieldIsDisplayed() {
        onView(withId(R.id.tv_name_value)).check(matches(isDisplayed()));
    }

    @Test
    public void testUsernameFieldIsDisplayed() {
        onView(withId(R.id.tv_username_value)).check(matches(isDisplayed()));
    }

    @Test
    public void testEmailFieldIsDisplayed() {
        onView(withId(R.id.tv_email_value)).check(matches(isDisplayed()));
    }

    @Test
    public void testPhoneFieldIsDisplayed() {
        onView(withId(R.id.tv_phone_value)).check(matches(isDisplayed()));
    }

    @Test
    public void testCountryFieldIsDisplayed() {
        onView(withId(R.id.tv_country_value)).check(matches(isDisplayed()));
    }

    @Test
    public void testAddressFieldIsDisplayed() {
        onView(withId(R.id.tv_address_value)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditNameButtonIsDisplayed() {
        onView(withId(R.id.btn_edit_name)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditEmailButtonIsDisplayed() {
        onView(withId(R.id.btn_edit_email)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditPhoneButtonIsDisplayed() {
        onView(withId(R.id.btn_edit_phone)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditCountryButtonIsDisplayed() {
        onView(withId(R.id.btn_edit_country)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditUsernameButtonIsDisplayed() {
        onView(withId(R.id.btn_edit_username)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditAddressButtonIsDisplayed() {
        onView(withId(R.id.btn_edit_address)).check(matches(isDisplayed()));
    }

    @Test
    public void testNotificationsSwitchIsDisplayed() {
        onView(withId(R.id.switch_notifications)).check(matches(isDisplayed()));
    }

    @Test
    public void testDeleteProfileButtonIsDisplayed() {
        onView(withId(R.id.btn_delete_profile)).check(matches(isDisplayed()));
    }

    @Test
    public void testSignOutButtonIsDisplayed() {
        onView(withId(R.id.btn_sign_out)).check(matches(isDisplayed()));
    }

    @Test
    public void testCloseButtonIsDisplayed() {
        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditNameOpensDialog() {
        onView(withId(R.id.btn_edit_name)).perform(click());
        onView(withText("Edit Name")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditEmailOpensDialog() {
        onView(withId(R.id.btn_edit_email)).perform(click());
        onView(withText("Edit Email")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditPhoneOpensDialog() {
        onView(withId(R.id.btn_edit_phone)).perform(click());
        onView(withText("Edit Phone Number")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditCountryOpensDialog() {
        onView(withId(R.id.btn_edit_country)).perform(click());
        onView(withText("Edit Country")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditUsernameOpensDialog() {
        onView(withId(R.id.btn_edit_username)).perform(click());
        onView(withText("Edit Username")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditAddressOpensDialog() {
        onView(withId(R.id.btn_edit_address)).perform(click());
        onView(withText("Edit Business Address")).check(matches(isDisplayed()));
    }

    @Test
    public void testCancelEditDialogCloses() {
        onView(withId(R.id.btn_edit_name)).perform(click());
        onView(withText("Cancel")).perform(click());
        onView(withId(R.id.tv_name_value)).check(matches(isDisplayed()));
    }
}
