package org.kabiri.android.usbterminal

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
internal class MainActivityAndroidTest {

    @get:Rule
    var rule = activityScenarioRule<MainActivity>()

    private fun ensureMenuIsAccessible(menuItemId: Int) {
        try {
            // Try to find the menu item first
            onView(withId(menuItemId)).check(matches(isDisplayed()))
        } catch (e: NoMatchingViewException) {
            // If not found then open the overflow menu
            openActionBarOverflowOrOptionsMenu(
                InstrumentationRegistry.getInstrumentation().targetContext
            )
        }
    }

    @Test
    fun checkUiViewsAreDisplayed() {
        // arrange
        // act
        // assert
        onView(withId(R.id.tvOutput)).check(matches(isDisplayed()))
        onView(withId(R.id.btEnter)).check(matches(isDisplayed()))
        onView(withId(R.id.etInput)).check(matches(isDisplayed()))
    }

    @Test
    fun checkActionMenuItemsAreDisplayed() {
        // arrange
        // act
        // Ensure action items are accessible, either in the toolbar or via overflow
        ensureMenuIsAccessible(R.id.actionSettings)
        ensureMenuIsAccessible(R.id.actionConnect)
        ensureMenuIsAccessible(R.id.actionDisconnect)

        // assert
        // Check menu items are displayed
        onView(withId(R.id.actionSettings)).check(matches(isDisplayed()))
        onView(withId(R.id.actionConnect)).check(matches(isDisplayed()))
        onView(withId(R.id.actionDisconnect)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingSettingsOpensSettingsBottomSheet() {
        // arrange
        // Ensure the action item is accisble, either in the toolbar or via overflow
        ensureMenuIsAccessible(R.id.actionSettings)

        // act
        onView(withId(R.id.actionSettings)).perform(click())

        // assert
        onView(withId(R.id.composeViewSettingContent)).check(matches(isDisplayed()))
    }
}
