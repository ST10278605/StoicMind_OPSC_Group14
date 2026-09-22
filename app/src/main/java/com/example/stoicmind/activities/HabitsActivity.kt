package com.example.stoicmind.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.stoicmind.databinding.ActivityHabitsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HabitsActivity : BaseActivity() {

    private lateinit var binding: ActivityHabitsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupHabits()
        
        binding.btnAddNewHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Habits"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupHabits() {
        val habits = listOf(
            binding.habit1 to ("Read a book" to "#D1E8E2"),
            binding.habit2 to ("Meditate" to "#E0D7FF"),
            binding.habit3 to ("Exercise" to "#FFF0C1"),
            binding.habit4 to ("Journal" to "#C1F0D1"),
            binding.habit5 to ("Study" to "#FFC1C1")
        )

        habits.forEachIndexed { index, (itemBinding, data) ->
            itemBinding.tvHabitName.text = data.first
            itemBinding.cvIconBackground.setCardBackgroundColor(Color.parseColor(data.second))
            itemBinding.cbHabit.isChecked = (index < 2)

            itemBinding.cbHabit.setOnCheckedChangeListener { _, isChecked ->
                val status = if (isChecked) "completed!" else "uncompleted."
                Toast.makeText(this, "${data.first} marked as $status", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddHabitDialog() {
        val input = EditText(this)
        input.hint = "Enter habit name"

        AlertDialog.Builder(this)
            .setTitle("Add New Habit")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val newHabitName = input.text.toString().trim()
                if (newHabitName.isNotEmpty()) {
                    binding.habit5.tvHabitName.text = newHabitName
                    binding.habit5.cbHabit.isChecked = false
                    Toast.makeText(this, "Habit '$newHabitName' added successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Habit name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
