package com.example.stoicmind.adapters

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stoicmind.databinding.ItemCommunityPostBinding
import com.example.stoicmind.models.CommunityPost

class CommunityAdapter(
    private var posts: List<CommunityPost> = emptyList(),
    private val onLikeClick: (CommunityPost) -> Unit
) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    inner class CommunityViewHolder(val binding: ItemCommunityPostBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val binding = ItemCommunityPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommunityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val post = posts[position]
        with(holder.binding) {
            tvAuthorName.text = post.authorName.ifEmpty { "Stoic Practitioner" }
            tvAvatarInitial.text = (post.authorName.firstOrNull() ?: 'S').uppercaseChar().toString()
            
            val timeAgo = DateUtils.getRelativeTimeSpanString(
                post.date,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
            tvPostDate.text = timeAgo

            tvCategoryTag.text = post.category
            tvPostTitle.text = post.title
            tvPostContent.text = post.content
            
            tvLikesCount.text = "${post.likesCount} Likes"

            if (post.isLikedByMe) {
                ivLikeIcon.setColorFilter(Color.parseColor("#E53935"))
                tvLikesCount.setTextColor(Color.parseColor("#E53935"))
            } else {
                ivLikeIcon.setColorFilter(Color.parseColor("#757575"))
                tvLikesCount.setTextColor(Color.parseColor("#757575"))
            }

            btnLike.setOnClickListener {
                onLikeClick(post)
            }
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<CommunityPost>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
