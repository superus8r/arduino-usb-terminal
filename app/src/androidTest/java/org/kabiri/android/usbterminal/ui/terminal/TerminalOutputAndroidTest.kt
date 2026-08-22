package org.kabiri.android.usbterminal.ui.terminal

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
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
        composeRule.onNodeWithText("Error!", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Info", substring = true).assertIsDisplayed()
    }

    @Test
    fun terminalOutput_longPressCopiesAllText() {
        // arrange
        val logs =
            mutableStateListOf(
                OutputText("A\n", OutputText.OutputType.TYPE_NORMAL),
                OutputText("B", OutputText.OutputType.TYPE_NORMAL),
            )

        var copiedText: String? = null
        composeRule.setContent {
            UsbTerminalTheme {
                TerminalOutput(
                    logs = logs,
                    autoScroll = true,
                    onCopyText = { copiedText = it },
                )
            }
        }

        // act: long-press on one of the visible lines (parent handles the gesture)
        composeRule.onNodeWithText("B").performTouchInput { longClick() }
        composeRule.waitForIdle()

        // assert clipboard contains concatenated text
        assertThat(copiedText).isEqualTo("A\nB")
    }

    @Test
    fun terminalOutput_appliesModifierTag() {
        // arrange
        val logs =
            mutableStateListOf(
                OutputText("Tagged line", OutputText.OutputType.TYPE_NORMAL),
            )

        // act
        composeRule.setContent {
            UsbTerminalTheme {
                TerminalOutput(
                    logs = logs,
                    autoScroll = false,
                    modifier = Modifier.testTag("terminal"),
                )
            }
        }

        // assert: the tagged node exists and is visible, and the text is displayed
        composeRule.onNodeWithTag("terminal").assertExists().assertIsDisplayed()
        composeRule.onNodeWithText("Tagged line").assertIsDisplayed()
    }

    @Test
    fun terminalOutput_handlesEmptyLogs_andLongPressCopiesEmpty() {
        // arrange
        val logs = mutableStateListOf<OutputText>()

        var copiedText: String? = null
        composeRule.setContent {
            UsbTerminalTheme {
                TerminalOutput(
                    logs = logs,
                    autoScroll = false,
                    modifier =
                        Modifier.testTag("terminal"),
                    onCopyText = { copiedText = it },
                )
            }
        }

        // act: long-press the list itself
        composeRule.onNodeWithTag("terminal").performTouchInput { longClick() }
        composeRule.waitForIdle()

        // assert: clipboard should contain empty string
        assertThat(copiedText).isEqualTo("")
    }

    @Test
    fun terminalOutput_autoScrollTrue_withEmptyList_isStable() {
        // arrange
        val logs = mutableStateListOf<OutputText>()

        // act
        composeRule.setContent {
            UsbTerminalTheme {
                TerminalOutput(
                    logs = logs,
                    autoScroll = true,
                    modifier = Modifier.testTag("terminal"),
                )
            }
        }

        // assert: composable exists but not enabled when with empty logs and autoScroll enabled
        composeRule.onNodeWithTag("terminal").assertExists()
    }

    @Test
    fun terminalOutput_noAutoScroll_append_rendersNewItem() {
        // arrange
        val logs =
            mutableStateListOf(
                OutputText("Initial", OutputText.OutputType.TYPE_NORMAL),
            )

        composeRule.setContent {
            UsbTerminalTheme {
                TerminalOutput(
                    logs = logs,
                    autoScroll = false,
                    modifier = Modifier.testTag("terminal"),
                )
            }
        }

        // act: append a new line while autoScroll is disabled
        composeRule.runOnUiThread {
            logs.add(OutputText("Next", OutputText.OutputType.TYPE_NORMAL))
        }
        composeRule.waitForIdle()

        // assert: new item is rendered (if condition path with autoScroll=false evaluated)
        composeRule.onNodeWithText("Next").assertIsDisplayed()
    }
}
