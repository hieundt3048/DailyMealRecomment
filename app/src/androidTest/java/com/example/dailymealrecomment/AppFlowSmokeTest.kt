package com.example.dailymealrecomment

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.dailymealrecomment.ui.profile.ProfileActivity
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
            onView(withId(R.id.btnAuthPrimary)).perform(click())
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
    fun profileRejectsInvalidHeightBeforeSaving() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
            .putExtra(LoginActivity.EXTRA_SMOKE_TEST, true)

        ActivityScenario.launch<ProfileActivity>(intent).use {
            onView(withId(R.id.edtHeight)).perform(replaceText("99"))
            onView(withId(R.id.edtWeight)).perform(replaceText("65"))
            onView(withId(R.id.edtAge)).perform(replaceText("25"))
            closeSoftKeyboard()
            onView(withId(R.id.btnCalculate)).perform(click())
            onView(withId(R.id.edtHeight)).check(matches(hasErrorText("Chiều cao phải từ 100 đến 250 cm.")))
        }
    }

    @Test
    fun mainExposesCameraAndGalleryActions() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .putExtra(LoginActivity.EXTRA_SMOKE_TEST, true)

        intending(hasComponent(CameraActivity::class.java.name))
            .respondWith(ActivityResult(Activity.RESULT_CANCELED, null))
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT))
            .respondWith(ActivityResult(Activity.RESULT_CANCELED, null))

        ActivityScenario.launch<MainActivity>(intent).use {
            onView(withId(R.id.rvHomeMealSuggestions)).perform(scrollTo()).check(matches(isDisplayed()))
            onView(withId(R.id.btnCamera)).perform(click())
            intended(hasComponent(CameraActivity::class.java.name))
            onView(withId(R.id.btnGallery)).perform(click())
            intended(hasAction(Intent.ACTION_OPEN_DOCUMENT))
            onView(withId(R.id.tvGalleryStatus)).check(matches(withText(R.string.gallery_picker_cancelled)))
            onView(withId(R.id.rvHomeMealSuggestions)).perform(scrollTo()).check(matches(isDisplayed()))
        }
    }

    @Test
    fun diaryPageShowsMealLogDateControlsAndEmptyState() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .putExtra(LoginActivity.EXTRA_SMOKE_TEST, true)

        ActivityScenario.launch<MainActivity>(intent).use {
            onView(withId(R.id.nav_diary)).perform(click())
            onView(withId(R.id.tvDiaryDate)).check(matches(isDisplayed()))
            onView(withId(R.id.tvDiarySummary)).check(matches(isDisplayed()))
            onView(withId(R.id.rvTodayLog)).check(matches(isDisplayed()))

            onView(withId(R.id.btnPreviousDiaryDate)).perform(click())
            onView(withId(R.id.diaryEmptyState)).check(matches(isDisplayed()))
            onView(withId(R.id.btnDiaryAddMeal)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun gallerySelectionOpensAnalysisScreen() {
        val selectedImageUri = Uri.parse("content://com.example.dailymealrecomment.smoke/selected-meal.jpg")
        val pickerResult = Intent().setData(selectedImageUri)
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .putExtra(LoginActivity.EXTRA_SMOKE_TEST, true)

        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT))
            .respondWith(ActivityResult(Activity.RESULT_OK, pickerResult))
        intending(hasComponent(FoodAnalysisActivity::class.java.name))
            .respondWith(ActivityResult(Activity.RESULT_CANCELED, null))

        ActivityScenario.launch<MainActivity>(intent).use {
            onView(withId(R.id.btnGallery)).perform(click())
            intended(hasAction(Intent.ACTION_OPEN_DOCUMENT))
            intended(hasComponent(FoodAnalysisActivity::class.java.name))
            onView(withId(R.id.tvGalleryStatus)).check(matches(withText(R.string.gallery_picker_selected)))
        }
    }

    @Test
    fun cameraShowsPermissionRecoveryState() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), CameraActivity::class.java)
            .putExtra(CameraActivity.EXTRA_FORCE_PERMISSION_DENIED, true)

        ActivityScenario.launch<CameraActivity>(intent).use {
            onView(withId(R.id.permissionState)).check(matches(isDisplayed()))
            onView(withId(R.id.btnRetryCameraPermission)).check(matches(isDisplayed()))
            onView(withId(R.id.btnOpenCameraSettings)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun analysisResultShowsEditableCalorieOnlyFields() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), FoodAnalysisActivity::class.java)

        ActivityScenario.launch<FoodAnalysisActivity>(intent).use {
            onView(withId(R.id.contentState)).check(matches(isDisplayed()))
            onView(withId(R.id.etFoodName)).check(matches(isDisplayed()))
            onView(withId(R.id.etWeight)).check(matches(isDisplayed()))
            onView(withId(R.id.etCalories)).check(matches(isDisplayed()))
            onView(withId(R.id.mealChipGroup)).check(matches(isDisplayed()))
            onView(withId(R.id.chipLunch)).check(matches(isDisplayed()))
            onView(withId(R.id.btnSave)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun analysisResultShowsErrorStateAndCanRetry() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), FoodAnalysisActivity::class.java)
            .putExtra(FoodAnalysisActivity.EXTRA_FORCE_ERROR, true)

        ActivityScenario.launch<FoodAnalysisActivity>(intent).use {
            onView(withId(R.id.errorState)).check(matches(isDisplayed()))
            onView(withId(R.id.btnRetry)).perform(click())
            onView(withId(R.id.contentState)).check(matches(isDisplayed()))
            onView(withId(R.id.etCalories)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun analysisResultShowsEmptyStateAndCanRetry() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), FoodAnalysisActivity::class.java)
            .putExtra(FoodAnalysisActivity.EXTRA_FORCE_EMPTY, true)

        ActivityScenario.launch<FoodAnalysisActivity>(intent).use {
            onView(withId(R.id.emptyState)).check(matches(isDisplayed()))
            onView(withId(R.id.btnRetryEmpty)).perform(click())
            onView(withId(R.id.contentState)).check(matches(isDisplayed()))
            onView(withId(R.id.etWeight)).check(matches(isDisplayed()))
        }
    }
}
