package com.example.dailymealrecomment.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.example.dailymealrecomment.BuildConfig
import com.example.dailymealrecomment.CameraActivity
import com.example.dailymealrecomment.LoginActivity
import com.example.dailymealrecomment.MainActivity
import com.example.dailymealrecomment.R
import com.example.dailymealrecomment.data.SessionPreferences
import com.example.dailymealrecomment.data.model.DietType
import com.example.dailymealrecomment.data.model.Goal
import com.example.dailymealrecomment.data.model.UserProfile
import com.example.dailymealrecomment.databinding.ActivityProfileBinding
import com.example.dailymealrecomment.utilities.CalorieCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var sessionPreferences: SessionPreferences
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private var hasCompletedProfile = false

    private val smokeTestMode: Boolean
        get() = BuildConfig.DEBUG && intent.getBooleanExtra(LoginActivity.EXTRA_SMOKE_TEST, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionPreferences = SessionPreferences(this)

        if (firebaseAuth.currentUser == null && !smokeTestMode) {
            openLogin()
            return
        }

        setupBottomNavigation()
        hasCompletedProfile = sessionPreferences.isProfileCompleted || smokeTestMode
        updateCompletedProfileUi()

        sessionPreferences.cachedProfile()?.let(::showProfile)
        if (!smokeTestMode) loadProfileFromFirestore()
        binding.btnCalculate.setOnClickListener { saveProfileAndContinue() }
        binding.btnSignOut.setOnClickListener { signOut() }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_profile
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    openMain(MainActivity.PAGE_HOME, clearTask = false)
                    false
                }
                R.id.nav_diary -> {
                    openMain(MainActivity.PAGE_DIARY, clearTask = false)
                    false
                }
                R.id.nav_scan -> {
                    startActivity(Intent(this, CameraActivity::class.java))
                    false
                }
                R.id.nav_suggestions -> {
                    openMain(MainActivity.PAGE_SUGGESTIONS, clearTask = false)
                    false
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun saveProfileAndContinue() {
        clearFormErrors()
        val validation = ProfileFormValidator.validate(
            heightText = binding.edtHeight.text.toString(),
            weightText = binding.edtWeight.text.toString(),
            ageText = binding.edtAge.text.toString(),
        )
        if (validation is ProfileValidationResult.Invalid) {
            showValidationError(validation.field)
            return
        }
        val validInput = validation as ProfileValidationResult.Valid

        val goal = ProfileGoalMapper.goalFromCheckedChip(binding.chipGroupGoal.checkedChipId)
        val dietType = ProfileDietMapper.dietTypeFromCheckedChip(binding.chipGroupDiet.checkedChipId)
        val activityLevel = ProfileActivityLevelMapper.activityLevelFromCheckedChip(
            binding.chipGroupActivity.checkedChipId,
        )
        val profile = UserProfile(
            heightCm = validInput.heightCm,
            weightKg = validInput.weightKg,
            age = validInput.age,
            isMale = binding.rgGender.checkedRadioButtonId != R.id.rbFemale,
            goal = goal,
            dietType = dietType,
            activityLevel = activityLevel,
        )
        val calorieTarget = CalorieCalculator.calculateDailyCalorieTarget(profile)
        binding.tvResult.text = getString(R.string.calorie_result, calorieTarget)

        if (smokeTestMode) {
            openMain(clearTask = true)
            return
        }

        sessionPreferences.saveProfile(profile, calorieTarget)
        val user = firebaseAuth.currentUser ?: return
        binding.btnCalculate.isEnabled = false
        binding.btnCalculate.text = getString(R.string.saving_profile)
        val data = mapOf(
            "uid" to user.uid,
            "name" to (user.displayName ?: ""),
            "email" to (user.email ?: ""),
            "heightCm" to profile.heightCm,
            "weightKg" to profile.weightKg,
            "age" to profile.age,
            "isMale" to profile.isMale,
            "goal" to goal.name,
            "dietType" to dietType.name,
            "activityLevel" to profile.activityLevel,
            "dailyCalorieTarget" to calorieTarget,
            "profileCompleted" to true,
        )
        firestore.collection("users").document(user.uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { openMain(clearTask = true) }
            .addOnFailureListener {
                Toast.makeText(this, R.string.profile_saved_locally, Toast.LENGTH_LONG).show()
                openMain(clearTask = true)
            }
    }

    private fun loadProfileFromFirestore() {
        val user = firebaseAuth.currentUser ?: return
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) return@addOnSuccessListener
                val profile = UserProfile(
                    heightCm = document.getDouble("heightCm") ?: return@addOnSuccessListener,
                    weightKg = document.getDouble("weightKg") ?: return@addOnSuccessListener,
                    age = document.getLong("age")?.toInt() ?: return@addOnSuccessListener,
                    isMale = document.getBoolean("isMale") != false,
                    goal = enumValueOrDefault(document.getString("goal"), Goal.MAINTAIN_WEIGHT),
                    dietType = enumValueOrDefault(document.getString("dietType"), DietType.NORMAL),
                    activityLevel = document.getDouble("activityLevel") ?: 1.2,
                )
                val target = document.getLong("dailyCalorieTarget")?.toInt()
                    ?: CalorieCalculator.calculateDailyCalorieTarget(profile)
                showProfile(profile)
                binding.tvResult.text = getString(R.string.calorie_result, target)
                if (document.getBoolean("profileCompleted") == true) {
                    sessionPreferences.saveProfile(profile, target)
                    hasCompletedProfile = true
                    updateCompletedProfileUi()
                }
            }
    }

    private fun showProfile(profile: UserProfile) {
        binding.edtHeight.setText(profile.heightCm.toDisplayNumber())
        binding.edtWeight.setText(profile.weightKg.toDisplayNumber())
        binding.edtAge.setText(profile.age.toString())
        binding.rgGender.check(if (profile.isMale) R.id.rbMale else R.id.rbFemale)
        binding.chipGroupGoal.check(ProfileGoalMapper.chipIdForGoal(profile.goal))
        binding.chipGroupDiet.check(ProfileDietMapper.chipIdForDietType(profile.dietType))
        binding.chipGroupActivity.check(
            ProfileActivityLevelMapper.chipIdForActivityLevel(profile.activityLevel),
        )
    }

    private fun openMain(
        startPage: String = MainActivity.PAGE_HOME,
        clearTask: Boolean,
    ) {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra(LoginActivity.EXTRA_SMOKE_TEST, smokeTestMode)
            putExtra(MainActivity.EXTRA_START_PAGE, startPage)
            flags = if (clearTask) {
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            } else {
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        })
        if (clearTask) finish()
    }

    private fun updateCompletedProfileUi() {
        binding.bottomNavigation.visibility = if (hasCompletedProfile) View.VISIBLE else View.GONE
        binding.btnCalculate.setText(
            if (hasCompletedProfile) R.string.profile_update else R.string.profile_save_continue,
        )
    }

    private fun clearFormErrors() {
        binding.edtHeight.error = null
        binding.edtWeight.error = null
        binding.edtAge.error = null
    }

    private fun showValidationError(field: ProfileField) {
        when (field) {
            ProfileField.HEIGHT -> {
                binding.edtHeight.error = getString(R.string.invalid_height)
                binding.edtHeight.requestFocus()
            }
            ProfileField.WEIGHT -> {
                binding.edtWeight.error = getString(R.string.invalid_weight)
                binding.edtWeight.requestFocus()
            }
            ProfileField.AGE -> {
                binding.edtAge.error = getString(R.string.invalid_age)
                binding.edtAge.requestFocus()
            }
        }
    }

    private fun signOut() {
        firebaseAuth.signOut()
        sessionPreferences.clear()
        lifecycleScope.launch {
            runCatching {
                CredentialManager.create(this@ProfileActivity)
                    .clearCredentialState(ClearCredentialStateRequest())
            }
            openLogin()
        }
    }

    private fun openLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, fallback: T): T {
        return runCatching { enumValueOf<T>(value.orEmpty()) }.getOrDefault(fallback)
    }

    private fun Double.toDisplayNumber(): String {
        return if (this % 1.0 == 0.0) toInt().toString() else toString()
    }
}
