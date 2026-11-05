package com.example.stoicmind.activities

import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.stoicmind.R
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.databinding.ActivityAchievementsBinding
import com.example.stoicmind.databinding.ItemBadgeBinding
import com.example.stoicmind.models.Habit
import com.example.stoicmind.repository.SyncRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AchievementsActivity : BaseActivity() {

    private lateinit var binding: ActivityAchievementsBinding

    @Inject
    lateinit var firebaseManager: FirebaseManager

    @Inject
    lateinit var syncRepository: SyncRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        loadUserProgressAndStats()
    }

    override fun onResume() {
        super.onResume()
        loadUserProgressAndStats()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Progress"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadUserProgressAndStats() {
        val userId = firebaseManager.getCurrentUserId() ?: "guest_user"

        lifecycleScope.launch {
            val journalEntries = syncRepository.getJournalEntriesForUser(userId)
            val moodCheckIns = syncRepository.getMoodCheckInsForUser(userId)

            val prefs = getSharedPreferences("stoic_mind_prefs", MODE_PRIVATE)
            val habitsJson = prefs.getString("stoic_mind_habits_$userId", null)
            var habitsCompletedCount = 0
            if (!habitsJson.isNullOrBlank()) {
                try {
                    val listType = object : TypeToken<List<Habit>>() {}.type
                    val habitsList: List<Habit> = Gson().fromJson(habitsJson, listType)
                    habitsCompletedCount = habitsList.count { it.isCompleted }
                } catch (_: Exception) {}
            }

            val reflectionsCount = journalEntries.count {
                it.title.contains("Reflection", ignoreCase = true) || it.content.contains("Reflection", ignoreCase = true)
            }
            val journalEntriesCount = journalEntries.size
            val moodCheckInsCount = moodCheckIns.size

            val activityTimestamps = mutableListOf<Long>()
            activityTimestamps.addAll(journalEntries.map { it.date })
            activityTimestamps.addAll(moodCheckIns.map { it.date })

            val (currentStreak, longestStreak) = calculateStreak(activityTimestamps)

            val isDailyWriterUnlocked = journalEntriesCount >= 1
            val isExplorerUnlocked = reflectionsCount >= 1
            val isFireStarterUnlocked = currentStreak >= 1 || longestStreak >= 1
            val isPeaceMakerUnlocked = moodCheckInsCount >= 1
            val isDisciplinedUnlocked = habitsCompletedCount >= 1

            val unlockedCount = listOf(
                isDailyWriterUnlocked,
                isExplorerUnlocked,
                isFireStarterUnlocked,
                isPeaceMakerUnlocked,
                isDisciplinedUnlocked
            ).count { it }

            val isStoicMasterUnlocked = unlockedCount >= 5

            var earnedPoints = 0
            if (isDailyWriterUnlocked) earnedPoints += 50
            if (isExplorerUnlocked) earnedPoints += 50
            if (isFireStarterUnlocked) earnedPoints += 50
            if (isPeaceMakerUnlocked) earnedPoints += 50
            if (isDisciplinedUnlocked) earnedPoints += 50
            if (isStoicMasterUnlocked) earnedPoints += 100

            earnedPoints += (journalEntriesCount * 10)
            earnedPoints += (reflectionsCount * 15)
            earnedPoints += (moodCheckInsCount * 5)
            earnedPoints += (habitsCompletedCount * 5)

            val level = earnedPoints / 100
            val levelProgress = earnedPoints % 100

            try {
                val currentUser = firebaseManager.getUser(userId)
                val updatedUser = currentUser.copy(
                    points = earnedPoints,
                    level = level,
                    streak = currentStreak,
                    updatedAt = System.currentTimeMillis()
                )
                firebaseManager.saveUser(updatedUser)
            } catch (_: Exception) {}

            val rankTitle = when {
                level >= 20 -> "Stoic Master"
                level >= 10 -> "Philosopher"
                level >= 5 -> "Stoic Learner"
                level >= 1 -> "Practitioner"
                else -> "Initiate"
            }

            binding.tvUserLevel.text = "Level $level"
            binding.tvUserTitle.text = rankTitle
            binding.progressBarLevel.progress = levelProgress
            binding.tvUserPoints.text = "$earnedPoints Points"

            binding.tvTotalReflections.text = "$reflectionsCount"
            binding.tvTotalJournalEntries.text = "$journalEntriesCount"
            binding.tvLongestStreak.text = "$longestStreak Days"
            binding.tvHabitsCompleted.text = "$habitsCompletedCount"

            updateBadgeUI(
                binding.badge1,
                "Daily Writer",
                R.drawable.ic_journal,
                "#EAF2F8",
                isDailyWriterUnlocked
            )
            updateBadgeUI(
                binding.badge2,
                "Explorer",
                R.drawable.ic_explore,
                "#D1F2EB",
                isExplorerUnlocked
            )
            updateBadgeUI(
                binding.badge3,
                "Fire Starter",
                R.drawable.ic_habits,
                "#F5B7B1",
                isFireStarterUnlocked
            )
            updateBadgeUI(
                binding.badge4,
                "Peace Maker",
                R.drawable.ic_mood,
                "#D6EAF8",
                isPeaceMakerUnlocked
            )
            updateBadgeUI(
                binding.badge5,
                "Disciplined",
                R.drawable.ic_habits,
                "#FCF3CF",
                isDisciplinedUnlocked
            )
            updateBadgeUI(
                binding.badge6,
                "Stoic Master",
                R.mipmap.ic_launcher,
                "#FAD7A0",
                isStoicMasterUnlocked
            )
        }
    }

    private fun updateBadgeUI(
        badgeBinding: ItemBadgeBinding,
        badgeName: String,
        iconResId: Int,
        bgColorHex: String,
        isUnlocked: Boolean
    ) {
        badgeBinding.tvBadgeName.text = badgeName
        badgeBinding.ivBadgeIcon.setImageResource(iconResId)

        if (isUnlocked) {
            badgeBinding.cvBadgeIcon.setCardBackgroundColor(Color.parseColor(bgColorHex))
            badgeBinding.cvBadgeIcon.alpha = 1.0f
            badgeBinding.tvBadgeStatus.text = "Unlocked"
            badgeBinding.tvBadgeStatus.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            badgeBinding.cvBadgeIcon.setCardBackgroundColor(Color.parseColor("#E0E0E0"))
            badgeBinding.cvBadgeIcon.alpha = 0.5f
            badgeBinding.tvBadgeStatus.text = "Locked"
            badgeBinding.tvBadgeStatus.setTextColor(Color.GRAY)
        }
    }

    private fun calculateStreak(datesMillis: List<Long>): Pair<Int, Int> {
        if (datesMillis.isEmpty()) return Pair(0, 0)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val activeDaysSorted = datesMillis.map { sdf.format(Date(it)) }
            .distinct()
            .sorted()

        if (activeDaysSorted.isEmpty()) return Pair(0, 0)

        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0

        val dateObjects = activeDaysSorted.mapNotNull {
            try {
                sdf.parse(it)
            } catch (e: Exception) {
                null
            }
        }

        if (dateObjects.isEmpty()) return Pair(0, 0)

        for (i in dateObjects.indices) {
            if (i == 0) {
                tempStreak = 1
            } else {
                val diffMs = dateObjects[i].time - dateObjects[i - 1].time
                val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
                if (diffDays == 1) {
                    tempStreak++
                } else if (diffDays > 1) {
                    tempStreak = 1
                }
            }
            if (tempStreak > longestStreak) {
                longestStreak = tempStreak
            }
        }

        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lastActiveDate = Calendar.getInstance().apply {
            time = dateObjects.last()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMs = todayCalendar.timeInMillis - lastActiveDate.timeInMillis
        val diffDaysFromToday = (diffMs / (1000 * 60 * 60 * 24)).toInt()

        currentStreak = if (diffDaysFromToday <= 1) tempStreak else 0

        return Pair(currentStreak, longestStreak)
    }
}