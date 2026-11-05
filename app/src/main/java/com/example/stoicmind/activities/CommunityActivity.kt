package com.example.stoicmind.activities

import android.graphics.Color
import android.os.Bundle
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
        observeData()
        ensureStarterPosts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Stoic Community"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        communityAdapter = CommunityAdapter { post ->
            toggleLikePost(post)
        }
        binding.rvCommunityPosts.apply {
            layoutManager = LinearLayoutManager(this@CommunityActivity)
            adapter = communityAdapter
        }
    }

    private fun setupCategoryChips() {
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
                Toast.makeText(this@CommunityActivity, "Syncing community posts...", Toast.LENGTH_SHORT).show()
                syncRepository.syncAll()
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddPost.setOnClickListener {
            showAddPostDialog()
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
            allPosts.filter { it.category.contains(selectedCategory, ignoreCase = true) }
        }

        if (filtered.isNotEmpty()) {
            communityAdapter.updatePosts(filtered)
            binding.rvCommunityPosts.visibility = View.VISIBLE
            binding.tvEmptyCommunity.visibility = View.GONE
        } else {
            communityAdapter.updatePosts(emptyList())
            binding.rvCommunityPosts.visibility = View.GONE
            binding.tvEmptyCommunity.visibility = View.VISIBLE
        }
    }

    private fun toggleLikePost(post: CommunityPost) {
        val newLiked = !post.isLikedByMe
        val delta = if (newLiked) 1 else -1

        lifecycleScope.launch {
            communityDao.updateLikeStatus(post.id, newLiked, delta)
        }
    }

    private fun showAddPostDialog() {
        val dialogBinding = DialogAddCommunityPostBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancelPost.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSubmitPost.setOnClickListener {
            val title = dialogBinding.etPostTitle.text.toString().trim()
            val category = dialogBinding.etPostCategory.text.toString().trim()
            val content = dialogBinding.etPostContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill in title and content", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

            lifecycleScope.launch {
                communityDao.insertCommunityPost(CommunityEntity.fromModel(newPost))
                Toast.makeText(this@CommunityActivity, "Community post published!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun ensureStarterPosts() {
        lifecycleScope.launch {
            val existing = communityDao.getCommunityPosts()
            if (existing.isEmpty()) {
                val starterPosts = listOf(
                    CommunityEntity(
                        id = "starter_1",
                        userId = "m_aurelius",
                        authorName = "Marcus Aurelius",
                        title = "Focus on what you control",
                        content = "You have power over your mind - not outside events. Realize this, and you will find strength.",
                        category = "Stoic Wisdom",
                        date = System.currentTimeMillis() - 3600000 * 2,
                        likesCount = 24,
                        isLikedByMe = true
                    ),
                    CommunityEntity(
                        id = "starter_2",
                        userId = "epictetus",
                        authorName = "Epictetus",
                        title = "The dichotomy of control",
                        content = "Some things are in our control and others not. Things in our control are opinion, pursuit, desire, aversion, and whatever are our own actions.",
                        category = "Discussions",
                        date = System.currentTimeMillis() - 3600000 * 6,
                        likesCount = 18,
                        isLikedByMe = false
                    ),
                    CommunityEntity(
                        id = "starter_3",
                        userId = "seneca",
                        authorName = "Seneca",
                        title = "On the shortness of life",
                        content = "It is not that we have a short time to live, but that we waste a lot of it. Life is long enough if you know how to use it.",
                        category = "Reflections",
                        date = System.currentTimeMillis() - 3600000 * 12,
                        likesCount = 31,
                        isLikedByMe = true
                    )
                )
                communityDao.insertCommunityPosts(starterPosts)
            }
        }
    }
}
