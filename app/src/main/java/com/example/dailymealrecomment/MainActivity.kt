package com.example.dailymealrecomment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailymealrecomment.data.SessionPreferences
import com.example.dailymealrecomment.data.model.DietType
import com.example.dailymealrecomment.data.model.MealSuggestion
import com.example.dailymealrecomment.databinding.ActivityMainBinding
import com.example.dailymealrecomment.model.FoodItem
import com.example.dailymealrecomment.ui.profile.ProfileActivity
import com.example.dailymealrecomment.utilities.DietSuggestionFilter
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
    private val mealSuggestions = listOf(
        MealSuggestion(
            id = SUGGESTION_CHICKEN_RICE,
            name = "Cơm gà áp chảo",
            calories = 520,
            isVegan = false,
        ),
        MealSuggestion(
            id = SUGGESTION_TOFU_SALAD,
            name = "Salad đậu phụ",
            calories = 430,
            isVegan = true,
        ),
    )
    private val smokeTestMode: Boolean
        get() = BuildConfig.DEBUG && intent.getBooleanExtra(LoginActivity.EXTRA_SMOKE_TEST, false)

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            showGalleryStatus(R.string.gallery_picker_cancelled)
            return@registerForActivityResult
        }

        val data = result.data
        val uri = data?.data
        if (uri == null) {
            showGalleryStatus(R.string.gallery_picker_missing_image)
            Toast.makeText(this, R.string.gallery_picker_missing_image, Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        persistGalleryReadPermission(uri, data)
        if (!smokeTestMode && !canReadGalleryImage(uri)) {
            showGalleryStatus(R.string.gallery_picker_unreadable)
            Toast.makeText(this, R.string.gallery_picker_unreadable, Toast.LENGTH_LONG).show()
            return@registerForActivityResult
        }

        showGalleryStatus(R.string.gallery_picker_selected)
        openAnalysis(uri)
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
        setupBottomNavigation()
        updateDashboard(if (smokeTestMode) 2_000 else sessionPreferences.dailyCalorieTarget)
        updateSuggestionCards()
        if (!smokeTestMode) loadCalorieTarget()
        selectStartPage(intent.getStringExtra(EXTRA_START_PAGE))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        selectStartPage(intent.getStringExtra(EXTRA_START_PAGE))
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
            openGalleryPicker()
        }
    }

    private fun openGalleryPicker() {
        showGalleryStatus(R.string.gallery_picker_opening)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }

        try {
            pickImageLauncher.launch(intent)
        } catch (exception: ActivityNotFoundException) {
            showGalleryStatus(R.string.gallery_picker_unavailable)
            Toast.makeText(this, R.string.gallery_picker_unavailable, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showHomePage()
                    true
                }
                R.id.nav_diary -> {
                    showDiaryPage()
                    true
                }
                R.id.nav_scan -> {
                    startActivity(Intent(this, CameraActivity::class.java))
                    false
                }
                R.id.nav_suggestions -> {
                    showSuggestionsPage()
                    true
                }
                R.id.nav_profile -> {
                    openProfile()
                    false
                }
                else -> false
            }
        }
        binding.bottomNavigation.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showHomePage()
                R.id.nav_diary -> showDiaryPage()
                R.id.nav_suggestions -> showSuggestionsPage()
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun selectStartPage(startPage: String?) {
        binding.bottomNavigation.selectedItemId = when (startPage) {
            PAGE_DIARY -> R.id.nav_diary
            PAGE_SUGGESTIONS -> R.id.nav_suggestions
            else -> R.id.nav_home
        }
    }

    private fun showHomePage() {
        binding.pageHome.visibility = View.VISIBLE
        binding.pageDiary.visibility = View.GONE
        binding.pageSuggestions.visibility = View.GONE
        binding.toolbar.title = getString(R.string.nav_home)
    }

    private fun showDiaryPage() {
        binding.pageHome.visibility = View.GONE
        binding.pageDiary.visibility = View.VISIBLE
        binding.pageSuggestions.visibility = View.GONE
        binding.toolbar.title = getString(R.string.nav_diary)
    }

    private fun showSuggestionsPage() {
        updateSuggestionCards()
        binding.pageHome.visibility = View.GONE
        binding.pageDiary.visibility = View.GONE
        binding.pageSuggestions.visibility = View.VISIBLE
        binding.toolbar.title = getString(R.string.nav_suggestions)
    }

    private fun updateSuggestionCards() {
        val dietType = sessionPreferences.cachedProfile()?.dietType ?: DietType.NORMAL
        val visibleSuggestionIds = DietSuggestionFilter.filterForDiet(mealSuggestions, dietType)
            .map { it.id }
            .toSet()

        binding.cardChickenSuggestion.visibility = if (SUGGESTION_CHICKEN_RICE in visibleSuggestionIds) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.cardTofuSuggestion.visibility = if (SUGGESTION_TOFU_SALAD in visibleSuggestionIds) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.tvSuggestionDietStatus.text = if (dietType == DietType.VEGAN) {
            getString(R.string.suggestions_status_vegan)
        } else {
            getString(R.string.suggestions_status_normal)
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
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(FoodAnalysisActivity.EXTRA_IMAGE_URI, uri.toString())
        })
    }

    private fun persistGalleryReadPermission(uri: Uri, data: Intent) {
        val takeFlags = data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (takeFlags != 0) {
            runCatching {
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            }
        }
    }

    private fun canReadGalleryImage(uri: Uri): Boolean = runCatching {
        contentResolver.openInputStream(uri)?.use { true } ?: false
    }.getOrDefault(false)

    private fun showGalleryStatus(messageRes: Int) {
        binding.tvGalleryStatus.setText(messageRes)
        binding.tvGalleryStatus.visibility = View.VISIBLE
    }

    private fun openProfile() {
        startActivity(Intent(this, ProfileActivity::class.java).apply {
            putExtra(LoginActivity.EXTRA_SMOKE_TEST, smokeTestMode)
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

    companion object {
        const val EXTRA_START_PAGE = "com.example.dailymealrecomment.START_PAGE"
        const val PAGE_HOME = "home"
        const val PAGE_DIARY = "diary"
        const val PAGE_SUGGESTIONS = "suggestions"
        private const val SUGGESTION_CHICKEN_RICE = "chicken_rice"
        private const val SUGGESTION_TOFU_SALAD = "tofu_salad"
    }
}
