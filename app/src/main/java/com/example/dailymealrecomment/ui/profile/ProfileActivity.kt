package com.example.dailymealrecomment.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.example.dailymealrecomment.BuildConfig
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

        sessionPreferences.cachedProfile()?.let(::showProfile)
        if (!smokeTestMode) loadProfileFromFirestore()
        binding.btnCalculate.setOnClickListener { saveProfileAndContinue() }
        binding.btnSignOut.setOnClickListener { signOut() }
    }

    private fun saveProfileAndContinue() {
        val height = binding.edtHeight.text.toString().toDoubleOrNull()
        val weight = binding.edtWeight.text.toString().toDoubleOrNull()
        val age = binding.edtAge.text.toString().toIntOrNull()

        if (height == null || height !in 100.0..250.0) {
            binding.edtHeight.error = getString(R.string.invalid_height)
            return
        }
        if (weight == null || weight !in 30.0..350.0) {
            binding.edtWeight.error = getString(R.string.invalid_weight)
            return
        }
        if (age == null || age !in 13..100) {
            binding.edtAge.error = getString(R.string.invalid_age)
            return
        }

        val goal = when (binding.chipGroupGoal.checkedChipId) {
            R.id.chipLose -> Goal.LOSE_WEIGHT
            R.id.chipGain -> Goal.GAIN_WEIGHT
            else -> Goal.MAINTAIN_WEIGHT
        }
        val dietType = if (binding.chipGroupDiet.checkedChipId == R.id.chipVegan) {
            DietType.VEGAN
        } else {
            DietType.NORMAL
        }
        val profile = UserProfile(
            heightCm = height,
            weightKg = weight,
            age = age,
            isMale = binding.rgGender.checkedRadioButtonId != R.id.rbFemale,
            goal = goal,
            dietType = dietType,
            activityLevel = 1.2,
        )
        val calorieTarget = CalorieCalculator.calculateDailyCalorieTarget(profile)
        binding.tvResult.text = getString(R.string.calorie_result, calorieTarget)

        if (smokeTestMode) {
            openMain()
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
            "heightCm" to height,
            "weightKg" to weight,
            "age" to age,
            "isMale" to profile.isMale,
            "goal" to goal.name,
            "dietType" to dietType.name,
            "activityLevel" to profile.activityLevel,
            "dailyCalorieTarget" to calorieTarget,
            "profileCompleted" to true,
        )
        firestore.collection("users").document(user.uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { openMain() }
            .addOnFailureListener {
                Toast.makeText(this, R.string.profile_saved_locally, Toast.LENGTH_LONG).show()
                openMain()
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
                }
            }
    }

    private fun showProfile(profile: UserProfile) {
        binding.edtHeight.setText(profile.heightCm.toDisplayNumber())
        binding.edtWeight.setText(profile.weightKg.toDisplayNumber())
        binding.edtAge.setText(profile.age.toString())
        binding.rgGender.check(if (profile.isMale) R.id.rbMale else R.id.rbFemale)
        binding.chipGroupGoal.check(
            when (profile.goal) {
                Goal.LOSE_WEIGHT -> R.id.chipLose
                Goal.GAIN_WEIGHT -> R.id.chipGain
                Goal.MAINTAIN_WEIGHT -> R.id.chipMaintain
            },
        )
        binding.chipGroupDiet.check(
            if (profile.dietType == DietType.VEGAN) R.id.chipVegan else R.id.chipNormal,
        )
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra(LoginActivity.EXTRA_SMOKE_TEST, smokeTestMode)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
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
