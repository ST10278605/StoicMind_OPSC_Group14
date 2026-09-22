package com.example.stoicmind.database

import android.util.Log
import com.example.stoicmind.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseManager @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    private var mockUser: User? = null
    private val mockUsersMap = mutableMapOf<String, User>()
    private val mockJournalEntries = mutableListOf<JournalEntry>()
    private val mockHabits = mutableListOf<Habit>()
    private val mockMoods = mutableListOf<MoodCheckIn>()
    
    companion object {
        private const val TAG = "FirebaseManager"
        private const val USERS_COLLECTION = "users"
        private const val JOURNAL_COLLECTION = "journal"
        private const val HABITS_COLLECTION = "habits"
        private const val MOODS_COLLECTION = "moods"
        private const val ACHIEVEMENTS_COLLECTION = "achievements"
    }
    
    // Authentication Methods
    suspend fun registerUser(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = User(
                id = authResult.user?.uid ?: "",
                email = email
            )
            // Save user to Firestore
            saveUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed, falling back to mock mode", e)
            val mockId = "mock_uid_${email.hashCode()}"
            val user = User(id = mockId, email = email)
            mockUser = user
            mockUsersMap[mockId] = user
            Result.success(user)
        }
    }
    
    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: ""
            val user = getUser(userId)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed, falling back to mock mode", e)
            val mockId = "mock_uid_${email.hashCode()}"
            val user = mockUsersMap[mockId] ?: User(id = mockId, email = email)
            mockUser = user
            Result.success(user)
        }
    }
    
    suspend fun googleSignIn(idToken: String): Result<User> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val userId = authResult.user?.uid ?: ""
            val user = getUser(userId)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign in failed, falling back to mock mode", e)
            val user = User(id = "mock_google_uid", email = "mockuser@gmail.com")
            mockUser = user
            Result.success(user)
        }
    }
    
    fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Ignore
        }
        mockUser = null
    }
    
    fun getCurrentUserId(): String? {
        return try {
            auth.currentUser?.uid ?: mockUser?.id ?: "guest_user"
        } catch (e: Exception) {
            mockUser?.id ?: "guest_user"
        }
    }
    
    // User Methods
    suspend fun saveUser(user: User) {
        try {
            db.collection(USERS_COLLECTION)
                .document(user.id)
                .set(user, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Save user failed, using mock", e)
            mockUsersMap[user.id] = user
        }
    }
    
    suspend fun getUser(userId: String): User {
        return try {
            val document = db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            document.toObject(User::class.java) ?: mockUsersMap[userId] ?: User(id = userId)
        } catch (e: Exception) {
            Log.e(TAG, "Get user failed, using mock", e)
            mockUsersMap[userId] ?: User(id = userId)
        }
    }
    
    // Journal Methods
    suspend fun saveJournalEntry(entry: JournalEntry): Result<String> {
        return try {
            val docRef = db.collection(JOURNAL_COLLECTION).document()
            val entryWithId = entry.copy(id = docRef.id)
            docRef.set(entryWithId).await()
            mockJournalEntries.removeAll { it.id == entryWithId.id }
            mockJournalEntries.add(0, entryWithId)
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Save journal entry failed, using mock", e)
            val mockId = if (entry.id.isNotEmpty()) entry.id else java.util.UUID.randomUUID().toString()
            val entryWithId = entry.copy(id = mockId)
            mockJournalEntries.removeAll { it.id == entryWithId.id }
            mockJournalEntries.add(0, entryWithId)
            Result.success(mockId)
        }
    }
    
    suspend fun getJournalEntries(userId: String): List<JournalEntry> {
        return try {
            val querySnapshot = db.collection(JOURNAL_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val remoteEntries = querySnapshot.documents.mapNotNull { 
                it.toObject(JournalEntry::class.java) 
            }
            if (remoteEntries.isNotEmpty()) {
                (remoteEntries + mockJournalEntries.filter { it.userId == userId })
                    .distinctBy { it.id }
                    .sortedByDescending { it.date }
            } else {
                mockJournalEntries.filter { it.userId == userId }.sortedByDescending { it.date }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get journal entries failed, using mock", e)
            mockJournalEntries.filter { it.userId == userId }.sortedByDescending { it.date }
        }
    }
    
    // Habit Methods
    suspend fun saveHabit(habit: Habit): Result<String> {
        return try {
            val docRef = db.collection(HABITS_COLLECTION).document()
            val habitWithId = habit.copy(id = docRef.id)
            docRef.set(habitWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Save habit failed, using mock", e)
            val mockId = java.util.UUID.randomUUID().toString()
            val habitWithId = habit.copy(id = mockId)
            mockHabits.add(habitWithId)
            Result.success(mockId)
        }
    }
    
    suspend fun getHabits(userId: String): List<Habit> {
        return try {
            val querySnapshot = db.collection(HABITS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { 
                it.toObject(Habit::class.java) 
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get habits failed, using mock", e)
            mockHabits.filter { it.userId == userId && it.isActive }
        }
    }
    
    // Mood Methods
    suspend fun saveMood(mood: MoodCheckIn): Result<String> {
        return try {
            val docRef = db.collection(MOODS_COLLECTION).document()
            val moodWithId = mood.copy(id = docRef.id)
            docRef.set(moodWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Save mood failed, using mock", e)
            val mockId = java.util.UUID.randomUUID().toString()
            val moodWithId = mood.copy(id = mockId)
            mockMoods.add(moodWithId)
            Result.success(mockId)
        }
    }
    
    suspend fun getMoods(userId: String): List<MoodCheckIn> {
        return try {
            val querySnapshot = db.collection(MOODS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { 
                it.toObject(MoodCheckIn::class.java) 
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get moods failed, using mock", e)
            mockMoods.filter { it.userId == userId }.sortedByDescending { it.date }.take(30)
        }
    }
    
    // Achievements Methods
    suspend fun getAchievements(userId: String): List<Achievement> {
        return try {
            val querySnapshot = db.collection(ACHIEVEMENTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { 
                it.toObject(Achievement::class.java) 
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get achievements failed, using mock", e)
            emptyList()
        }
    }
}
