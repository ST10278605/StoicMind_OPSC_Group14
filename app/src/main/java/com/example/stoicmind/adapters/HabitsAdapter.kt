package com.example.stoicmind.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stoicmind.databinding.ItemHabitBinding
import com.example.stoicmind.models.Habit

class HabitsAdapter(
    private var habits: List<Habit> = emptyList(),
    private val onHabitCheckedChange: (Habit, Boolean) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(val binding: ItemHabitBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        with(holder.binding) {
            tvHabitName.text = habit.name

            try {
                val color = if (habit.colorHex.isNotBlank()) {
                    Color.parseColor(habit.colorHex)
                } else {
                    Color.parseColor("#D1E8E2")
                }
                cvIconBackground.setCardBackgroundColor(color)
            } catch (_: Exception) {
                cvIconBackground.setCardBackgroundColor(Color.parseColor("#D1E8E2"))
            }

            cbHabit.setOnCheckedChangeListener(null)
            cbHabit.isChecked = habit.isCompleted

            cbHabit.setOnCheckedChangeListener { _, isChecked ->
                onHabitCheckedChange(habit, isChecked)
            }

            root.setOnClickListener {
                cbHabit.isChecked = !cbHabit.isChecked
            }
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateHabits(newHabits: List<Habit>) {
        habits = newHabits
        notifyDataSetChanged()
    }
}
