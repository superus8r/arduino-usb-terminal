package org.kabiri.android.usbterminal.ui.setting

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.ui.theme.UsbTerminalTheme

@RunWith(AndroidJUnit4::class)
class SettingSwitchItemAndroidTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun showContet(enabled: Boolean = true) {
        composeRule.setContent {
            UsbTerminalTheme {
                SettingSwitchItem(
                    enabled = enabled,
                    onToggle = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    @Test
    fun autoScrollSwitch_displaysLabel() {
        // arrange
        val context = composeRule.activity

        // act
        showContet()

        // assert
        composeRule
            .onNodeWithText(context.getString(R.string.settings_label_auto_scroll))
            .assertIsDisplayed()
    }

    @Test
    fun autoScrollSwitch_isOn_whenEnabledTrue() {
        // arrange
        val context = composeRule.activity

        // act
        showContet(enabled = true)

        // assert
        composeRule
            .onNode(
                hasAnySibling(
                    hasText(context.getString(R.string.settings_label_auto_scroll)),
                ).and(isToggleable()),
            ).assertIsOn()
    }

    @Test
    fun autoScrollSwitch_isOff_whenEnabledFalse() {
        // arrange
        val context = composeRule.activity

        // act
        showContet(enabled = false)

        // assert
        composeRule
            .onNode(
                hasAnySibling(
                    hasText(context.getString(R.string.settings_label_auto_scroll)),
                ).and(isToggleable()),
            ).assertIsOff()
    }
}
