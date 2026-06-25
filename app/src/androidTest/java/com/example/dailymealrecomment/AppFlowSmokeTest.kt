package com.example.dailymealrecomment

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppFlowSmokeTest {
    @get:Rule
    val intentsRule = IntentsRule()

    @Test
    fun loginToProfileToMainFlowIsReachable() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
            .putExtra(LoginActivity.EXTRA_SMOKE_TEST, true)

        ActivityScenario.launch<LoginActivity>(intent).use {
            onView(withId(R.id.btnGoogleSignIn)).perform(click())
            onView(withId(R.id.edtHeight)).perform(replaceText("170"))
            onView(withId(R.id.edtWeight)).perform(replaceText("65"))
            onView(withId(R.id.edtAge)).perform(replaceText("25"))
            closeSoftKeyboard()
            onView(withId(R.id.chipMaintain)).perform(click())
            onView(withId(R.id.chipNormal)).perform(click())
            onView(withId(R.id.btnCalculate)).perform(click())
            onView(withId(R.id.btnCamera)).check(matches(isDisplayed()))
            onView(withId(R.id.btnGallery)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun mainExposesCameraAndGalleryActions() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .putExtra(LoginActivity.EXTRA_SMOKE_TEST, true)

        intending(hasComponent(CameraActivity::class.java.name))
            .respondWith(ActivityResult(Activity.RESULT_CANCELED, null))
        intending(hasAction(Intent.ACTION_GET_CONTENT))
            .respondWith(ActivityResult(Activity.RESULT_CANCELED, null))

        ActivityScenario.launch<MainActivity>(intent).use {
            onView(withId(R.id.btnCamera)).perform(click())
            intended(hasComponent(CameraActivity::class.java.name))
            onView(withId(R.id.btnGallery)).perform(click())
            intended(hasAction(Intent.ACTION_GET_CONTENT))
        }
    }

    @Test
    fun analysisResultShowsEditableCalorieOnlyFields() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), FoodAnalysisActivity::class.java)

        ActivityScenario.launch<FoodAnalysisActivity>(intent).use {
            onView(withId(R.id.etFoodName)).check(matches(isDisplayed()))
            onView(withId(R.id.etWeight)).check(matches(isDisplayed()))
            onView(withId(R.id.etCalories)).check(matches(isDisplayed()))
            onView(withId(R.id.btnSave)).check(matches(isDisplayed()))
        }
    }
}
