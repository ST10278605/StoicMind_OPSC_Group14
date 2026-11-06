package com.example.stoicmind.activities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stoicmind.adapters.CommunityAdapter
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.database.dao.CommunityDao
import com.example.stoicmind.database.entity.CommunityEntity
import com.example.stoicmind.databinding.ActivityCommunityBinding
import com.example.stoicmind.databinding.DialogAddCommunityPostBinding
import com.example.stoicmind.models.CommunityPost
import com.example.stoicmind.repository.SyncRepository
import com.example.stoicmind.repository.SyncStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class CommunityActivity : BaseActivity() {

    private lateinit var binding: ActivityCommunityBinding
    private lateinit var communityAdapter: CommunityAdapter

    @Inject
    lateinit var communityDao: CommunityDao

    @Inject
    lateinit var firebaseManager: FirebaseManager

    @Inject
    lateinit var syncRepository: SyncRepository

    private var allPosts: List<CommunityPost> = emptyList()
    private var selectedCategory: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupCategoryChips()
        setupSyncBanner()
        setupClickListeners()
        seedThenObserve()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Stoic Community"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        communityAdapter = CommunityAdapter { post -> toggleLikePost(post) }
        binding.rvCommunityPosts.apply {
            layoutManager = LinearLayoutManager(this@CommunityActivity)
            adapter = communityAdapter
        }
    }

    private fun setupCategoryChips() {
        selectedCategory = when (binding.chipGroupCategories.checkedChipId) {
            binding.chipWisdom.id -> "Stoic Wisdom"
            binding.chipReflections.id -> "Reflections"
            binding.chipDiscussions.id -> "Discussions"
            else -> "All"
        }

        binding.chipGroupCategories.setOnCheckedChangeListener { _, checkedId ->
            selectedCategory = when (checkedId) {
                binding.chipWisdom.id -> "Stoic Wisdom"
                binding.chipReflections.id -> "Reflections"
                binding.chipDiscussions.id -> "Discussions"
                else -> "All"
            }
            filterPosts()
        }
    }

    private fun setupSyncBanner() {
        binding.syncBannerInclude.btnSyncNow.setOnClickListener {
            lifecycleScope.launch {
                Toast.makeText(
                    this@CommunityActivity,
                    "Syncing community posts...",
                    Toast.LENGTH_SHORT
                ).show()
                runCatching { syncRepository.syncAll() }
                    .onFailure { Log.e(TAG, "syncAll failed", it) }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddPost.setOnClickListener { showAddPostDialog() }
    }

    private fun seedThenObserve() {
        lifecycleScope.launch {
            ensureStarterPosts()
            observeData()
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    communityDao.getCommunityPostsFlow().collect { entities ->
                        allPosts = entities.map { it.toModel() }
                        filterPosts()
                    }
                }
                launch {
                    syncRepository.syncState.collect { syncState ->
                        updateSyncBannerUI(syncState.status, syncState.message)
                    }
                }
            }
        }
    }

    private fun updateSyncBannerUI(status: SyncStatus, message: String) {
        with(binding.syncBannerInclude) {
            when (status) {
                SyncStatus.OFFLINE -> {
                    cardSyncBanner.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                    tvSyncStatus.setTextColor(Color.parseColor("#E65100"))
                    ivSyncIcon.setColorFilter(Color.parseColor("#E65100"))
                    tvSyncStatus.text = "Offline Mode • Community posts cached in Room"
                    btnSyncNow.visibility = View.VISIBLE
                }
                SyncStatus.SYNCING -> {
                    cardSyncBanner.setCardBackgroundColor(Color.parseColor("#E3F2FD"))
                    tvSyncStatus.setTextColor(Color.parseColor("#1565C0"))
                    ivSyncIcon.setColorFilter(Color.parseColor("#1565C0"))
                    tvSyncStatus.text = "Syncing community data..."
                    btnSyncNow.visibility = View.GONE
                }
                SyncStatus.SYNCED -> {
                    cardSyncBanner.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                    tvSyncStatus.setTextColor(Color.parseColor("#2E7D32"))
                    ivSyncIcon.setColorFilter(Color.parseColor("#2E7D32"))
                    tvSyncStatus.text = "Online • Community connected"
                    btnSyncNow.visibility = View.VISIBLE
                }
                SyncStatus.ERROR -> {
                    cardSyncBanner.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                    tvSyncStatus.setTextColor(Color.parseColor("#C62828"))
                    ivSyncIcon.setColorFilter(Color.parseColor("#C62828"))
                    tvSyncStatus.text = "Sync status: $message"
                    btnSyncNow.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun filterPosts() {
        val filtered = if (selectedCategory == "All") {
            allPosts
        } else {
            allPosts.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }

        communityAdapter.updatePosts(filtered)
        val empty = filtered.isEmpty()
        binding.rvCommunityPosts.visibility = if (empty) View.GONE else View.VISIBLE
        binding.tvEmptyCommunity.visibility = if (empty) View.VISIBLE else View.GONE
    }

    private fun toggleLikePost(post: CommunityPost) {
        val newLiked = !post.isLikedByMe
        val delta = if (newLiked) 1 else -1

        lifecycleScope.launch {
            runCatching { communityDao.updateLikeStatus(post.id, newLiked, delta) }
                .onFailure {
                    Log.e(TAG, "toggleLikePost failed", it)
                    Toast.makeText(
                        this@CommunityActivity,
                        "Couldn't update like, try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showAddPostDialog() {
        val dialogBinding = DialogAddCommunityPostBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancelPost.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSubmitPost.setOnClickListener {
            val title = dialogBinding.etPostTitle.text.toString().trim()
            val category = dialogBinding.etPostCategory.text.toString().trim()
            val content = dialogBinding.etPostContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill in title and content", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialogBinding.btnSubmitPost.isEnabled = false

            lifecycleScope.launch {
                try {
                    val userId = firebaseManager.getCurrentUserId() ?: "guest_user"
                    val newPost = CommunityPost(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        authorName = "Stoic Member",
                        title = title,
                        content = content,
                        category = category.ifEmpty { "Stoic Wisdom" },
                        date = System.currentTimeMillis(),
                        likesCount = 1,
                        isLikedByMe = true,
                        isSynced = true
                    )
                    communityDao.insertCommunityPost(CommunityEntity.fromModel(newPost))
                    Toast.makeText(
                        this@CommunityActivity,
                        "Community post published!",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                } catch (e: Exception) {
                    Log.e(TAG, "Insert post failed", e)
                    Toast.makeText(
                        this@CommunityActivity,
                        "Couldn't publish post: ${e.message ?: "unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    dialogBinding.btnSubmitPost.isEnabled = true
                }
            }
        }

        dialog.show()
    }

    private suspend fun ensureStarterPosts() {
        runCatching {
            if (communityDao.getCommunityPosts().isEmpty()) {
                val now = System.currentTimeMillis()
                communityDao.insertCommunityPosts(
                    listOf(
                        CommunityEntity(
                            id = "starter_1",
                            userId = "m_aurelius",
                            authorName = "Marcus Aurelius",
                            title = "Focus on what you control",
                            content = "You have power over your mind - not outside events. Realize this, and you will find strength.",
                            category = "Stoic Wisdom",
                            date = now - 3_600_000L * 2,
                            likesCount = 24,
                            isLikedByMe = true,
                            isSynced = true
                        ),
                        CommunityEntity(
                            id = "starter_2",
                            userId = "epictetus",
                            authorName = "Epictetus",
                            title = "The dichotomy of control",
                            content = "Some things are in our control and others not. Things in our control are opinion, pursuit, desire, aversion, and whatever are our own actions.",
                            category = "Discussions",
                            date = now - 3_600_000L * 6,
                            likesCount = 18,
                            isLikedByMe = false,
                            isSynced = true
                        ),
                        CommunityEntity(
                            id = "starter_3",
                            userId = "seneca",
                            authorName = "Seneca",
                            title = "On the shortness of life",
                            content = "It is not that we have a short time to live, but that we waste a lot of it. Life is long enough if you know how to use it.",
                            category = "Reflections",
                            date = now - 3_600_000L * 12,
                            likesCount = 31,
                            isLikedByMe = true,
                            isSynced = true
                        )
                    )
                )
            }
        }.onFailure { Log.e(TAG, "ensureStarterPosts failed", it) }
    }

    private companion object {
        private const val TAG = "CommunityActivity"
    }
}
