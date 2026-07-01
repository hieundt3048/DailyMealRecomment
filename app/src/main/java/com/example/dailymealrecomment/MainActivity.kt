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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailymealrecomment.data.SessionPreferences
import com.example.dailymealrecomment.data.dashboard.DashboardProgressCalculator
import com.example.dailymealrecomment.data.diary.DiaryLogGrouper
import com.example.dailymealrecomment.data.diary.MealLogEntry
import com.example.dailymealrecomment.data.diary.MealLogEntryFactory
import com.example.dailymealrecomment.data.diary.MealLogRepository
import com.example.dailymealrecomment.data.diary.MealType
import com.example.dailymealrecomment.data.model.DietType
import com.example.dailymealrecomment.data.model.MealSuggestion
import com.example.dailymealrecomment.data.model.MealSuggestionCatalog
import com.example.dailymealrecomment.data.xampp.XamppRepository
import com.example.dailymealrecomment.databinding.ActivityMainBinding
import com.example.dailymealrecomment.ui.profile.ProfileActivity
import com.example.dailymealrecomment.utilities.DietSuggestionFilter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionPreferences: SessionPreferences
    private lateinit var foodLogAdapter: FoodLogAdapter
    private lateinit var homeMealSuggestionAdapter: MealSuggestionAdapter
    private val xamppRepository by lazy { XamppRepository() }
    private val mealLogRepository by lazy { MealLogRepository(xamppRepository) }
    private var selectedDiaryDayMillis: Long = startOfDay(System.currentTimeMillis())
    private var diaryEntries: List<MealLogEntry> = emptyList()
    private var dashboardTargetCalories: Int = 0
    private var shouldRefreshDashboardOnResume: Boolean = false
    private val mealSuggestions = MealSuggestionCatalog.all
    private val smokeTestMode: Boolean
        get() = BuildConfig.DEBUG && intent.getBooleanExtra(LoginActivity.EXTRA_SMOKE_TEST, false)
    private val selectedDiaryDateKey: String
        get() = MealLogEntryFactory.dateKey(selectedDiaryDayMillis)

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

        if (!sessionPreferences.isLoggedIn && !smokeTestMode) {
            sessionPreferences.clear()
            openLogin()
            return
        }

        foodLogAdapter = FoodLogAdapter()
        binding.rvTodayLog.layoutManager = LinearLayoutManager(this)
        binding.rvTodayLog.adapter = foodLogAdapter
        homeMealSuggestionAdapter = MealSuggestionAdapter(::openMealSuggestionDetail)
        binding.rvHomeMealSuggestions.layoutManager = GridLayoutManager(this, 2)
        binding.rvHomeMealSuggestions.adapter = homeMealSuggestionAdapter
        binding.rvHomeMealSuggestions.isNestedScrollingEnabled = false
        binding.toolbar.subtitle = sessionPreferences.userName ?: sessionPreferences.userEmail
        setupToolbar()
        setupImageActions()
        setupDiaryActions()
        setupDietSortButton()
        setupBottomNavigation()
        dashboardTargetCalories = if (smokeTestMode) {
            SMOKE_TEST_TARGET_CALORIES
        } else {
            sessionPreferences.dailyCalorieTarget
        }
        updateDashboard()
        updateSuggestionCards()
        loadDiaryForSelectedDate()
        if (!smokeTestMode) loadCalorieTarget()
        selectStartPage(intent.getStringExtra(EXTRA_START_PAGE))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val startPage = intent.getStringExtra(EXTRA_START_PAGE)
        if (startPage == PAGE_DIARY) {
            selectedDiaryDayMillis = startOfDay(System.currentTimeMillis())
        }
        selectStartPage(startPage)
        if (startPage == PAGE_DIARY) {
            loadDiaryForSelectedDate()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::sessionPreferences.isInitialized || smokeTestMode) return
        if (!shouldRefreshDashboardOnResume) {
            shouldRefreshDashboardOnResume = true
            return
        }

        refreshDashboardSnapshot()
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

    private fun setupDiaryActions() {
        binding.btnPreviousDiaryDate.setOnClickListener {
            moveDiaryDate(days = -1)
        }
        binding.btnNextDiaryDate.setOnClickListener {
            moveDiaryDate(days = 1)
        }
        binding.btnDiaryAddMeal.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }

    private fun setupDietSortButton() {
        binding.btnDietSortStatus.setOnClickListener {
            openProfile()
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
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun selectStartPage(startPage: String?) {
        binding.bottomNavigation.selectedItemId = when (startPage) {
            PAGE_DIARY -> R.id.nav_diary
            else -> R.id.nav_home
        }
    }

    private fun showHomePage() {
        binding.pageHome.visibility = View.VISIBLE
        binding.pageDiary.visibility = View.GONE
        binding.toolbar.title = getString(R.string.nav_home)
    }

    private fun showDiaryPage() {
        updateDiaryDateHeader()
        binding.pageHome.visibility = View.GONE
        binding.pageDiary.visibility = View.VISIBLE
        binding.toolbar.title = getString(R.string.nav_diary)
    }

    private fun updateSuggestionCards() {
        val remainingCalories = DashboardProgressCalculator.calculate(
            diaryEntries,
            dashboardTargetCalories,
        ).remainingCalories
        val dietType = sessionPreferences.cachedProfile()?.dietType ?: DietType.NORMAL
        val sortedSuggestions = DietSuggestionFilter.sortForDiet(mealSuggestions, dietType)

        homeMealSuggestionAdapter.submitList(sortedSuggestions, remainingCalories)
        binding.btnDietSortStatus.setText(
            if (dietType == DietType.VEGAN) {
                R.string.home_sort_vegan
            } else {
                R.string.home_sort_normal
            }
        )
        resizeHomeMealList(sortedSuggestions.size)
    }

    private fun resizeHomeMealList(itemCount: Int) {
        val columnCount = 2
        val rowCount = (itemCount + columnCount - 1) / columnCount
        val rowHeight = dpToPx(310)
        binding.rvHomeMealSuggestions.layoutParams =
            binding.rvHomeMealSuggestions.layoutParams.apply {
                height = rowCount * rowHeight
            }
    }

    private fun dpToPx(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun loadCalorieTarget() {
        val token = sessionPreferences.authToken ?: return
        lifecycleScope.launch {
            runCatching { xamppRepository.fetchProfile(token) }
                .onSuccess { profile ->
                    if (profile != null) {
                        sessionPreferences.saveProfile(profile.profile, profile.dailyCalorieTarget)
                        dashboardTargetCalories = profile.dailyCalorieTarget
                    } else {
                        dashboardTargetCalories = sessionPreferences.dailyCalorieTarget
                    }
                    updateDashboard()
                    updateSuggestionCards()
                }
                .onFailure {
                    dashboardTargetCalories = sessionPreferences.dailyCalorieTarget
                    updateDashboard()
                    updateSuggestionCards()
                }
        }
    }

    private fun refreshDashboardSnapshot() {
        dashboardTargetCalories = sessionPreferences.dailyCalorieTarget
        updateDashboard()
        loadCalorieTarget()
        loadDiaryForSelectedDate()
    }

    private fun loadDiaryForSelectedDate() {
        updateDiaryDateHeader()

        if (smokeTestMode) {
            showDiaryEntries(smokeDiaryEntriesForSelectedDate())
            return
        }

        val token = sessionPreferences.authToken
        if (token.isNullOrBlank()) {
            openLogin()
            return
        }

        showDiaryLoading()
        lifecycleScope.launch {
            runCatching {
                mealLogRepository.loadMealItemsForDate(
                    token = token,
                    dateKey = selectedDiaryDateKey,
                )
            }.onSuccess(::showDiaryEntries)
                .onFailure { showDiaryError() }
        }
    }

    private fun showDiaryLoading() {
        diaryEntries = emptyList()
        foodLogAdapter.submitSections(emptyList())
        binding.tvDiarySummary.setText(R.string.diary_summary_loading)
        binding.diaryStatus.setText(R.string.diary_loading)
        binding.diaryStatus.visibility = View.VISIBLE
        binding.diaryEmptyState.visibility = View.GONE
        binding.rvTodayLog.visibility = View.GONE
        updateDashboard()
        updateSuggestionCards()
    }

    private fun showDiaryEntries(entries: List<MealLogEntry>) {
        diaryEntries = DiaryLogGrouper.sortEntries(entries)
        val sections = DiaryLogGrouper.sectionsFor(diaryEntries)
        val totalCalories = DiaryLogGrouper.totalCalories(diaryEntries)
        foodLogAdapter.submitSections(sections)

        binding.tvDiarySummary.text = getString(
            R.string.diary_summary,
            diaryEntries.size,
            totalCalories,
        )
        binding.diaryStatus.visibility = View.GONE
        binding.diaryEmptyState.visibility = if (diaryEntries.isEmpty()) View.VISIBLE else View.GONE
        binding.rvTodayLog.visibility = if (diaryEntries.isEmpty()) View.GONE else View.VISIBLE
        updateDashboard()
        updateSuggestionCards()
    }

    private fun showDiaryError() {
        diaryEntries = emptyList()
        foodLogAdapter.submitSections(emptyList())
        binding.tvDiarySummary.setText(R.string.diary_summary_load_failed)
        binding.diaryStatus.setText(R.string.diary_load_failed)
        binding.diaryStatus.visibility = View.VISIBLE
        binding.diaryEmptyState.visibility = View.GONE
        binding.rvTodayLog.visibility = View.GONE
        updateDashboard()
        updateSuggestionCards()
    }

    private fun updateDashboard() {
        val progress = DashboardProgressCalculator.calculate(diaryEntries, dashboardTargetCalories)
        binding.tvCaloriesConsumed.text = getString(R.string.calories_consumed, progress.consumedCalories)
        binding.tvCaloriesTarget.text = getString(R.string.calories_target, progress.targetCalories)
        binding.tvCaloriesRemaining.text = progress.remainingCalories.toString()
        binding.progressCalories.progress = progress.progressPercent
    }

    private fun moveDiaryDate(days: Int) {
        val targetDay = addDays(selectedDiaryDayMillis, days)
        val today = startOfDay(System.currentTimeMillis())
        selectedDiaryDayMillis = targetDay.coerceAtMost(today)
        loadDiaryForSelectedDate()
    }

    private fun updateDiaryDateHeader() {
        val todayKey = MealLogEntryFactory.dateKey(System.currentTimeMillis())
        val formattedDate = diaryDateFormatter.format(Date(selectedDiaryDayMillis))
        binding.tvDiaryDate.text = if (selectedDiaryDateKey == todayKey) {
            getString(R.string.diary_date_today, formattedDate)
        } else {
            getString(R.string.diary_date_regular, formattedDate)
        }
        binding.btnNextDiaryDate.isEnabled = selectedDiaryDayMillis < startOfDay(System.currentTimeMillis())
    }

    private fun smokeDiaryEntriesForSelectedDate(): List<MealLogEntry> {
        val todayKey = MealLogEntryFactory.dateKey(System.currentTimeMillis())
        if (selectedDiaryDateKey != todayKey) return emptyList()

        return listOf(
            MealLogEntry(
                name = "Sữa chua",
                weight = 150,
                calories = 150,
                mealType = MealType.BREAKFAST,
                dateKey = todayKey,
                sourceImageUri = null,
                createdAtMillis = selectedDiaryDayMillis + 8 * HOUR_MILLIS,
            ),
            MealLogEntry(
                name = "Cơm gà áp chảo",
                weight = 320,
                calories = 520,
                mealType = MealType.LUNCH,
                dateKey = todayKey,
                sourceImageUri = null,
                createdAtMillis = selectedDiaryDayMillis + 12 * HOUR_MILLIS,
            ),
            MealLogEntry(
                name = "Chuối",
                weight = 100,
                calories = 89,
                mealType = MealType.SNACK,
                dateKey = todayKey,
                sourceImageUri = null,
                createdAtMillis = selectedDiaryDayMillis + 16 * HOUR_MILLIS,
            ),
        )
    }

    private fun openAnalysis(uri: Uri) {
        startActivity(Intent(this, FoodAnalysisActivity::class.java).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(FoodAnalysisActivity.EXTRA_IMAGE_URI, uri.toString())
        })
    }

    private fun persistGalleryReadPermission(uri: Uri, data: Intent) {
        val hasReadPermission = data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (hasReadPermission != 0) {
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
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

    private fun openMealSuggestionDetail(suggestion: MealSuggestion) {
        val remainingCalories = DashboardProgressCalculator.calculate(
            diaryEntries,
            dashboardTargetCalories,
        ).remainingCalories
        startActivity(Intent(this, MealSuggestionDetailActivity::class.java).apply {
            putExtra(MealSuggestionDetailActivity.EXTRA_SUGGESTION_ID, suggestion.id)
            putExtra(MealSuggestionDetailActivity.EXTRA_REMAINING_CALORIES, remainingCalories)
        })
    }

    private fun signOut() {
        val token = sessionPreferences.authToken
        sessionPreferences.clear()
        lifecycleScope.launch {
            if (!token.isNullOrBlank()) {
                xamppRepository.logout(token)
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
        private const val SMOKE_TEST_TARGET_CALORIES = 2_000
        private const val HOUR_MILLIS = 60 * 60 * 1_000L
        private val diaryDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))

        private fun startOfDay(timeMillis: Long): Long {
            return Calendar.getInstance().apply {
                timeInMillis = timeMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        private fun addDays(timeMillis: Long, days: Int): Long {
            return Calendar.getInstance().apply {
                timeInMillis = timeMillis
                add(Calendar.DAY_OF_YEAR, days)
            }.timeInMillis
        }
    }
}
