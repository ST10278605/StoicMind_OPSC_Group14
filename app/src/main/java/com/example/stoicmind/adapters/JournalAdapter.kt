package com.example.stoicmind.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stoicmind.databinding.ItemJournalEntryBinding
import com.example.stoicmind.models.JournalEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JournalAdapter(
    private var entries: List<JournalEntry> = emptyList(),
    private val onItemClick: ((JournalEntry) -> Unit)? = null
) : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    inner class JournalViewHolder(val binding: ItemJournalEntryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val binding = ItemJournalEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JournalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val entry = entries[position]
        with(holder.binding) {
            tvEntryTitle.text = if (entry.title.isNotBlank()) entry.title else "Daily Reflection"

            val sdf = SimpleDateFormat("MMMM d, yyyy • hh:mm a", Locale.getDefault())
            tvEntryDate.text = sdf.format(Date(entry.date))

            tvEntryPreview.text = entry.content

            if (entry.mood.isNotBlank()) {
                tvEntryMood.text = entry.mood
                tvEntryMood.visibility = View.VISIBLE
                ivMoodIndicator.visibility = View.GONE
            } else {
                tvEntryMood.visibility = View.GONE
                ivMoodIndicator.visibility = View.VISIBLE
            }

            root.setOnClickListener {
                onItemClick?.invoke(entry)
            }
        }
    }

    override fun getItemCount(): Int = entries.size

    fun updateEntries(newEntries: List<JournalEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}
