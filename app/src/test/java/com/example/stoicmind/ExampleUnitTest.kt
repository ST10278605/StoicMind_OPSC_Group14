package com.example.stoicmind

import com.example.stoicmind.models.JournalEntry
import com.example.stoicmind.models.Quote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun quoteModelStoresQuoteDetailsCorrectly() {
        val quote = Quote(
            id = "1",
            text = "The obstacle is the way.",
            author = "Marcus Aurelius",
            category = "Stoicism",
        )

        assertEquals("The obstacle is the way.", quote.text)
        assertEquals("Marcus Aurelius", quote.author)
        assertEquals("Stoicism", quote.category)
    }

    @Test
    fun newQuoteIsNotFavoriteByDefault() {
        val quote = Quote(
            text = "Focus on what you can control.",
            author = "Epictetus"
        )

        assertFalse(quote.isFavorite)
    }

    @Test
    fun journalContentCannotBeEmpty() {
        val content = ""

        val isValid = content.trim().isNotEmpty()

        assertFalse(isValid)
    }

    @Test
    fun journalContentIsValidWhenTextIsEntered() {
        val content = "Today I focused on what I can control."

        val isValid = content.trim().isNotEmpty()

        assertTrue(isValid)
    }

    @Test
    fun journalEntryModelCreation() {
        val entry = JournalEntry(
            id = "entry_1",
            userId = "user_123",
            title = "Morning Reflection",
            content = "Today is a peaceful day.",
            mood = "😊 Happy"
        )

        assertEquals("entry_1", entry.id)
        assertEquals("user_123", entry.userId)
        assertEquals("Morning Reflection", entry.title)
        assertEquals("Today is a peaceful day.", entry.content)
        assertEquals("😊 Happy", entry.mood)
        assertTrue(entry.date > 0)
    }

    @Test
    fun userSettingsCanBeUpdatedCorrectly() {
        val user = com.example.stoicmind.models.User(
            id = "user_1",
            name = "John Doe",
            email = "john@example.com",
            primaryVirtue = "Wisdom",
            theme = "classic"
        )

        val updatedUser = user.copy(
            name = "Jane Doe",
            primaryVirtue = "Courage",
            theme = "serene"
        )

        assertEquals("Jane Doe", updatedUser.name)
        assertEquals("Courage", updatedUser.primaryVirtue)
        assertEquals("serene", updatedUser.theme)
    }

    @Test
    fun userModelCreationWithEmail() {
        val user = com.example.stoicmind.models.User(
            id = "user_456",
            name = "Sipho Sithole",
            email = "sipho@example.com"
        )

        assertEquals("user_456", user.id)
        assertEquals("Sipho Sithole", user.name)
        assertEquals("sipho@example.com", user.email)
    }
}
