package org.kabiri.android.usbterminal.ui.setting

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kabiri.android.usbterminal.MainActivity
import org.kabiri.android.usbterminal.R

@RunWith(AndroidJUnit4::class)
internal class SettingModalBottomSheetAndroidTest {
    @get:Rule
    var composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun ensureMenuIsAccessible(
        @Suppress("SameParameterValue") menuItemId: Int,
        onVisible: () -> Unit,
        onOverflow: () -> Unit,
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
    fun opensAndDismissesSettingsBottomSheet() {
        val context = composeTestRule.activity

        // 1. Click the settings menu item to open the bottom sheet
        ensureMenuIsAccessible(
            menuItemId = R.id.actionSettings,
            onVisible = {
                onView(withId(R.id.actionSettings)).perform(click())
            },
            onOverflow = {
                onView(withText(R.string.title_settings)).perform(click())
            },
        )

        // 2. Verify the bottom sheet is displayed
        onView(withId(R.id.composeViewSettingContent)).check(matches(isDisplayed()))

        // 3. Click the dismiss button inside Compose
        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_bt_dismiss_sheet))
            .performClick()

        // Wait a bit for the dismiss animation or transaction
        composeTestRule.waitForIdle()

        // 4. Verify the bottom sheet is closed
        onView(withId(R.id.composeViewSettingContent)).check(doesNotExist())
    }
}
