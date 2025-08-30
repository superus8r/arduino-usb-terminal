package org.kabiri.android.usbterminal.ui.setting

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.ui.theme.UsbTerminalTheme

@RunWith(AndroidJUnit4::class)
class SettingContentAndroidTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun showContet(viewModel: SettingViewModel) {
        composeRule.setContent {
            UsbTerminalTheme {
                SettingContent(
                    settingViewModel = viewModel,
                    onDismiss = {},
                )
            }
        }
    }

    @Test
    fun settingContent_displaysExpectedTexts() {
        // arrange
        val context = composeRule.activity
        assertThat(context, notNullValue())
        val viewModel = mockk<SettingViewModel>(relaxed = true)
        every { viewModel.currentBaudRate } returns flowOf(9600)
        every { viewModel.currentAutoScroll } returns flowOf(true)

        // act
        showContet(viewModel)

        // assert
        composeRule.onNodeWithText(context.getString(R.string.settings_title)).assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.settings_subtitle))
            .assertIsDisplayed()

        // Baud rate label (ensure unmerged tree so label is found)
        composeRule
            .onNodeWithText(
                context.getString(R.string.settings_label_baud_rate),
                useUnmergedTree = true,
            ).assertIsDisplayed()

        // Buttons and bottom text
        composeRule
            .onNodeWithText(context.getString(R.string.settings_bt_reset_default))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.settings_bt_dismiss_sheet))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.settings_bottom_text))
            .assertIsDisplayed()

        // Auto-scroll label
        composeRule
            .onNodeWithText(context.getString(R.string.settings_label_auto_scroll))
            .assertIsDisplayed()
    }
}
