package com.example.stoicmind.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.stoicmind.R
import com.example.stoicmind.databinding.ActivityExploreCategoryContentBinding
import dagger.hilt.android.AndroidEntryPoint

data class ExploreContent(
    val title: String,
    val author: String,
    val description: String,
    val fullContent: String
)

@AndroidEntryPoint
class ExploreCategoryContentActivity : BaseActivity() {

    private lateinit var binding: ActivityExploreCategoryContentBinding

    private val favoriteTitles = mutableSetOf<String>()

    private val categoryContent = mapOf(

        "Resilience" to listOf(
            ExploreContent(
                "The Obstacle Is the Way",
                "Marcus Aurelius",
                "Challenges can become opportunities for growth.",
                "Difficulties are not always barriers. They can teach patience, courage and perseverance. Instead of avoiding challenges, focus on responding to them wisely."
            ),
            ExploreContent(
                "Accepting Difficult Times",
                "Seneca",
                "Hard times can develop strength and character.",
                "You cannot control every event in life, but you can control how you respond. Practise patience and use difficult experiences as opportunities to build resilience."
            )
        ),

        "Self-Discipline" to listOf(
            ExploreContent(
                "Master Your Actions",
                "Epictetus",
                "Self-discipline begins with controlling your choices.",
                "Build discipline through small daily decisions. Focus on your responsibilities, avoid unnecessary distractions and remain consistent with your goals."
            ),
            ExploreContent(
                "Consistency Over Motivation",
                "Stoic Practice",
                "Progress comes from repeated effort.",
                "Motivation can change from day to day. Develop routines that help you continue working even when you do not feel motivated."
            )
        ),

        "Relationships" to listOf(
            ExploreContent(
                "Practise Understanding",
                "Marcus Aurelius",
                "Treat others with patience and understanding.",
                "People have different experiences and perspectives. Listen carefully, avoid unnecessary judgement and respond with kindness while maintaining healthy boundaries."
            ),
            ExploreContent(
                "Respect and Boundaries",
                "Stoic Practice",
                "Healthy relationships require mutual respect.",
                "You cannot control another person's behaviour. You can communicate honestly, maintain your values and choose how you respond to others."
            )
        ),

        "Mindfulness" to listOf(
            ExploreContent(
                "Focus on the Present",
                "Marcus Aurelius",
                "Give your attention to the moment you are living.",
                "Avoid becoming overwhelmed by past mistakes or future uncertainty. Pay attention to your present actions and make thoughtful decisions."
            ),
            ExploreContent(
                "Observe Your Thoughts",
                "Epictetus",
                "Not every thought needs an immediate reaction.",
                "Pause before reacting to difficult situations. Observe your thoughts, identify your emotions and choose a response that reflects your values."
            )
        ),

        "Gratitude" to listOf(
            ExploreContent(
                "Appreciate What You Have",
                "Stoic Practice",
                "Recognise the value of the things already present in your life.",
                "Take time to appreciate supportive relationships, opportunities and ordinary moments. Gratitude can help you develop a more balanced perspective."
            ),
            ExploreContent(
                "Find Value in Simple Things",
                "Seneca",
                "A meaningful life does not depend entirely on possessions.",
                "Practise appreciating simple experiences instead of constantly comparing your life with others. Focus on what genuinely contributes to your well-being."
            )
        ),

        "Personal Development" to listOf(
            ExploreContent(
                "Know Yourself",
                "Socrates",
                "Self-awareness is an important part of personal growth.",
                "Reflect on your strengths, weaknesses, decisions and habits. Honest self-reflection helps you identify areas where you can improve."
            ),
            ExploreContent(
                "Learn from Experience",
                "Stoic Practice",
                "Every experience can offer an opportunity to learn.",
                "Review your decisions without being unnecessarily harsh on yourself. Identify what worked, what did not and what you can do differently next time."
            )
        ),

        "Stress Management" to listOf(
            ExploreContent(
                "Separate What You Control",
                "Epictetus",
                "Focus your energy on what is within your control.",
                "You cannot control every situation or outcome. Focus on your decisions, preparation and actions instead of worrying about things beyond your influence."
            ),
            ExploreContent(
                "Pause Before Reacting",
                "Stoic Practice",
                "A deliberate pause can prevent an impulsive response.",
                "When you feel overwhelmed, take a moment to breathe and identify what is causing your stress. Break the situation into manageable steps."
            )
        ),

        "Leadership" to listOf(
            ExploreContent(
                "Lead Through Example",
                "Marcus Aurelius",
                "Leadership begins with your own conduct.",
                "Demonstrate honesty, accountability and fairness through your actions. Good leadership involves serving others and taking responsibility for decisions."
            ),
            ExploreContent(
                "Listen Before Deciding",
                "Stoic Practice",
                "Thoughtful leaders consider different perspectives.",
                "Listen to people before making decisions. Consider the available information, remain fair and be willing to learn from feedback."
            )
        ),

        "Success" to listOf(
            ExploreContent(
                "Define Meaningful Success",
                "Stoic Practice",
                "Success should reflect your values and priorities.",
                "Set goals that have personal meaning. Measure progress through your effort, character and improvement rather than comparing yourself constantly with others."
            ),
            ExploreContent(
                "Focus on the Process",
                "Epictetus",
                "Give your best effort without demanding complete control over the outcome.",
                "Prepare carefully, take consistent action and learn from setbacks. Outcomes are not always guaranteed, but your effort and choices remain yours."
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExploreCategoryContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryName = intent.getStringExtra("category_name") ?: "Wisdom"

        setupToolbar(categoryName)
        displayCategoryContent(categoryName)
    }

    private fun setupToolbar(title: String) {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title

        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun displayCategoryContent(categoryName: String) {

        val container = binding.contentContainer
        container.removeAllViews()

        val contentList = categoryContent[categoryName].orEmpty()

        if (contentList.isEmpty()) {
            Toast.makeText(
                this,
                "No content available for this category.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        contentList.forEach { content ->

            val card = LayoutInflater.from(this)
                .inflate(R.layout.item_explore_content, container, false)

            val title = card.findViewById<TextView>(R.id.tvContentTitle)
            val author = card.findViewById<TextView>(R.id.tvContentAuthor)
            val description =
                card.findViewById<TextView>(R.id.tvContentDescription)
            val favorite =
                card.findViewById<ImageButton>(R.id.ibFavorite)
            val readMore = card.findViewById<Button>(R.id.btnReadMore)

            title.text = content.title
            author.text = content.author
            description.text = content.description

            favorite.setOnClickListener {
                if (favoriteTitles.add(content.title)) {
                    Toast.makeText(
                        this,
                        "Added to favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    favoriteTitles.remove(content.title)
                    Toast.makeText(
                        this,
                        "Removed from favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            readMore.setOnClickListener {
                android.app.AlertDialog.Builder(this)
                    .setTitle(content.title)
                    .setMessage(
                        "${content.author}\n\n${content.fullContent}"
                    )
                    .setPositiveButton("Close", null)
                    .show()
            }

            container.addView(card)
        }
    }
}