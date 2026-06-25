package com.example.dailymealrecomment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailymealrecomment.data.SessionPreferences
import com.example.dailymealrecomment.databinding.ActivityMainBinding
import com.example.dailymealrecomment.model.FoodItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionPreferences: SessionPreferences
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val loggedItems = mutableListOf(
        FoodItem("Sữa chua", 150, 150),
        FoodItem("Hạt hạnh nhân", 30, 170),
    )
    private val smokeTestMode: Boolean
        get() = BuildConfig.DEBUG && intent.getBooleanExtra(LoginActivity.EXTRA_SMOKE_TEST, false)

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let(::openAnalysis)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionPreferences = SessionPreferences(this)

        if (firebaseAuth.currentUser == null && !smokeTestMode) {
            sessionPreferences.clear()
            openLogin()
            return
        }

        binding.rvTodayLog.layoutManager = LinearLayoutManager(this)
        binding.rvTodayLog.adapter = FoodLogAdapter(loggedItems)
        binding.toolbar.subtitle = firebaseAuth.currentUser?.displayName
        setupToolbar()
        setupImageActions()
        updateDashboard(if (smokeTestMode) 2_000 else sessionPreferences.dailyCalorieTarget)
        if (!smokeTestMode) loadCalorieTarget()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) {
                signOut()
                true
            } else {
                false
            }
        }
    }

    private fun setupImageActions() {
        binding.btnCamera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        binding.btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun loadCalorieTarget() {
        val user = firebaseAuth.currentUser ?: return
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val target = document.getLong("dailyCalorieTarget")?.toInt()
                    ?: sessionPreferences.dailyCalorieTarget
                updateDashboard(target)
            }
            .addOnFailureListener { updateDashboard(sessionPreferences.dailyCalorieTarget) }
    }

    private fun updateDashboard(targetCalories: Int) {
        val consumedCalories = loggedItems.sumOf { it.calories }
        val remainingCalories = (targetCalories - consumedCalories).coerceAtLeast(0)
        binding.tvCaloriesConsumed.text = getString(R.string.calories_consumed, consumedCalories)
        binding.tvCaloriesTarget.text = getString(R.string.calories_target, targetCalories)
        binding.tvCaloriesRemaining.text = remainingCalories.toString()
        binding.progressCalories.progress = if (targetCalories > 0) {
            ((consumedCalories * 100.0) / targetCalories).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    private fun openAnalysis(uri: Uri) {
        startActivity(Intent(this, FoodAnalysisActivity::class.java).apply {
            putExtra(FoodAnalysisActivity.EXTRA_IMAGE_URI, uri.toString())
        })
    }

    private fun signOut() {
        firebaseAuth.signOut()
        sessionPreferences.clear()
        lifecycleScope.launch {
            runCatching {
                CredentialManager.create(this@MainActivity)
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
}
