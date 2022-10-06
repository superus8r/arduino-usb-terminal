package org.kabiri.android.usbterminal

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    var rule = activityScenarioRule<MainActivity>()

    @Test
    fun checkUiViewsAreDisplayed() {
        onView(withId(R.id.tvOutput)).check(matches(isDisplayed()))
        onView(withId(R.id.btEnter)).check(matches(isDisplayed()))
        onView(withId(R.id.etInput)).check(matches(isDisplayed()))
    }
}