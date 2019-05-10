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
import java.lang.Thread.sleep

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

        //
        // encrypt
        //
        onView(withId(R.id.textView))
            .check(matches(withText("Secure Text!")))

        onView(withId(R.id.buttonEncrypt))
            .perform(click())

        // time to "touch" fingerprint
        // $ adb emu finger touch 1
        sleep(3000L)

        onView(withId(R.id.textView))
            .check(matches(not(withText("Secure Text!"))))

        //
        // decrypt
        //
        onView(withId(R.id.buttonDecrypt))
            .perform(click())

        // time to "touch" fingerprint
        // $ adb emu finger touch 1
        sleep(5000L)

        onView(withId(R.id.textView))
            .check(matches(withText("Secure Text!")))


        //Runtime.getRuntime().exec("adb emu touch finger 1")
    }
}
