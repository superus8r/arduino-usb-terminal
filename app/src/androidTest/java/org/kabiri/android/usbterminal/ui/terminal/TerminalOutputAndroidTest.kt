package org.kabiri.android.usbterminal.ui.terminal

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kabiri.android.usbterminal.model.OutputText
import org.kabiri.android.usbterminal.ui.theme.UsbTerminalTheme

@RunWith(AndroidJUnit4::class)
class TerminalOutputAndroidTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun terminalOutput_displaysAllLines() {
        // arrange
        val logs =
            mutableStateListOf(
                OutputText("Line 1", OutputText.OutputType.TYPE_NORMAL),
                OutputText("Error!", OutputText.OutputType.TYPE_ERROR),
                OutputText("Info", OutputText.OutputType.TYPE_INFO),
            )

        // act
        composeRule.setContent {
            UsbTerminalTheme {
                TerminalOutput(logs = logs, autoScroll = false)
            }
        }

        // assert
        composeRule.onNodeWithText("Line 1").assertIsDisplayed()
        composeRule.onNodeWithText("Error!").assertIsDisplayed()
        composeRule.onNodeWithText("Info").assertIsDisplayed()
    }

    @Test
    fun terminalOutput_longPressCopiesAllText() {
        // arrange
        val context = composeRule.activity
        val logs =
            mutableStateListOf(
                OutputText("A\n", OutputText.OutputType.TYPE_NORMAL),
                OutputText("B", OutputText.OutputType.TYPE_NORMAL),
            )

        composeRule.setContent {
            UsbTerminalTheme {
                TerminalOutput(logs = logs, autoScroll = true)
            }
        }

        // act: long-press on one of the visible lines (parent handles the gesture)
        composeRule.onNodeWithText("B").performTouchInput { longClick() }
        composeRule.waitForIdle()

        // assert clipboard contains concatenated text
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val copied =
            clipboard.primaryClip
                ?.getItemAt(0)
                ?.coerceToText(context)
                ?.toString()
        assertThat(copied).isEqualTo("A\nB")
    }
}
