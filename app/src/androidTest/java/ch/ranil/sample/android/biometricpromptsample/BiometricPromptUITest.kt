package ch.ranil.sample.android.biometricpromptsample

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BiometricPromptUITest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun successfulEncryptDecrypt() {

        onView(withId(R.id.textView1))
            .check(matches(withText("Secure Text!")))
        onView(withId(R.id.textView2))
            .check(matches(withText("")))

        onView(withId(R.id.buttonEncrypt))
            .perform(click())

        Runtime.getRuntime().exec("adb emu touch finger 1")

        onView(withId(R.id.textView1))
            .check(matches(not(withText("Secure Text!"))))
        onView(withId(R.id.textView2))
            .check(matches(withText("encrypt success")))
    }

    @Test
    fun cancelledEncryptDecrypt() {

        onView(withId(R.id.textView1))
            .check(matches(withText("Secure Text!")))
        onView(withId(R.id.textView2))
            .check(matches(withText("")))

        onView(withId(R.id.buttonEncrypt))
            .perform(click())

        pressBack()

        onView(withId(R.id.textView1))
            .check(matches(not(withText("Secure Text!"))))
        onView(withId(R.id.textView2))
            .check(matches(withText("encrypt fallback")))
    }
}
