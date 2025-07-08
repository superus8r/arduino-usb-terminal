package org.kabiri.android.usbterminal

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
internal class MainActivityAndroidTest {

    @get:Rule
    var rule = activityScenarioRule<MainActivity>()

    private fun ensureMenuIsAccessible(
        menuItemId: Int,
        onVisible: () -> Unit,
        onOverflow: () -> Unit
    ) {
        try {
            // Try to find the menu item first
            onView(withId(menuItemId)).check(matches(isDisplayed()))
            onVisible()
        } catch (_: NoMatchingViewException) {
            // If not found then open the overflow menu
            openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
            onOverflow()
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
    fun checkActionMenuItemSettingsIsDisplayed() = ensureMenuIsAccessible(
        menuItemId = R.id.actionSettings,
        onVisible = {

            // assert
            onView(withId(R.id.actionSettings)).check(matches(isDisplayed()))
        },
        onOverflow = {

            // assert
            onView(withText(R.string.title_settings)).check(matches(isDisplayed()))
        }
    )

    @Test
    fun checkActionMenuItemConnectIsDisplayed() = ensureMenuIsAccessible(
        menuItemId = R.id.actionSettings,
        onVisible = {

            // assert
            onView(withId(R.id.actionConnect)).check(matches(isDisplayed()))
        },
        onOverflow = {

            // assert
            onView(withText(R.string.title_connect)).check(matches(isDisplayed()))
        }
    )

    @Test
    fun checkActionMenuItemDisconnectIsDisplayed() = ensureMenuIsAccessible(
        menuItemId = R.id.actionSettings,
        onVisible = {

            // assert
            onView(withId(R.id.actionDisconnect)).check(matches(isDisplayed()))
        },
        onOverflow = {

            // assert
            onView(withText(R.string.title_disconnect)).check(matches(isDisplayed()))
        }
    )

    @Test
    fun clickingSettingsOpensSettingsBottomSheet() {
        // arrange
        ensureMenuIsAccessible(
            menuItemId = R.id.actionSettings,
            onVisible = {

                // act
                onView(withId(R.id.actionSettings)).perform(click())

                // assert
                onView(withId(R.id.composeViewSettingContent)).check(matches(isDisplayed()))
            },
            onOverflow = {
                // act
                onView(withText(R.string.title_settings)).perform(click())

                // assert
                onView(withId(R.id.composeViewSettingContent)).check(matches(isDisplayed()))
            }
        )
    }
}
