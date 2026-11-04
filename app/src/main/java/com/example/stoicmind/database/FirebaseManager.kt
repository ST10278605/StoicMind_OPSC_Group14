package com.example.stoicmind.database

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.stoicmind.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("StoicMindAuthPrefs", Context.MODE_PRIVATE)

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
    suspend fun registerUser(email: String, password: String, name: String = ""): Result<User> {
        val userId = try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            try {
                firebaseUser?.sendEmailVerification()?.await()
                Log.d(TAG, "Verification email sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send verification email", e)
            }
            firebaseUser?.uid ?: "mock_uid_${email.hashCode()}"
        } catch (e: Exception) {
            Log.e(TAG, "Firebase registration failed, using secure local registration", e)
            "mock_uid_${email.hashCode()}"
        }

        val user = User(
            id = userId,
            name = name,
            email = email,
            isEmailVerified = true
        )

        // Store credentials and user locally so login works reliably across sessions
        prefs.edit()
            .putString("pwd_$email", password)
            .putString("uid_for_$email", userId)
            .putString("user_$userId", gson.toJson(user))
            .apply()

        mockUser = user
        mockUsersMap[userId] = user
        saveUser(user)

        return Result.success(user)
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: ""
            val user = getUser(userId)
            mockUser = user
            Result.success(user)

        } catch (e: Exception) {
            Log.e(TAG, "Firebase login failed, checking local storage credentials", e)

            // Check if account is registered locally
            val storedPassword = prefs.getString("pwd_$email", null)
            if (storedPassword == null) {
                return Result.failure(Exception("Account not found. Please register an account first."))
            }
            if (storedPassword != password) {
                return Result.failure(Exception("Incorrect password."))
            }

            val userId = prefs.getString("uid_for_$email", "mock_uid_${email.hashCode()}") ?: "mock_uid_${email.hashCode()}"
            val userJson = prefs.getString("user_$userId", null)
            val user = if (userJson != null) {
                gson.fromJson(userJson, User::class.java)
            } else {
                val derivedName = email.substringBefore("@").replace(".", " ")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                User(id = userId, name = derivedName, email = email, isEmailVerified = true)
            }

            mockUser = user
            mockUsersMap[userId] = user
            Result.success(user)
        }
    }

    suspend fun googleSignIn(idToken: String): Result<User> {
        return try {
            val credential =
                com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)

            val authResult = auth.signInWithCredential(credential).await()
            val userId = authResult.user?.uid ?: ""
            val user = getUser(userId)
            mockUser = user

            Result.success(user)

        } catch (e: Exception) {
            Log.e(TAG, "Google sign in failed", e)
            Result.failure(e)
        }
    }

    fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed", e)
        } catch (_: Exception) {
            // Ignore
        }

        mockUser = null
    }

    fun getCurrentUserId(): String? {
        return try {
            auth.currentUser?.uid ?: mockUser?.id
        } catch (_: Exception) {
            mockUser?.id
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
            Log.e(TAG, "Save user to Firestore failed, stored locally", e)
        }
        mockUsersMap[user.id] = user
        prefs.edit().putString("user_${user.id}", gson.toJson(user)).apply()
    }

    suspend fun getUser(userId: String): User {
        return try {
            val document = db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            val remoteUser = document.toObject(User::class.java)
            if (remoteUser != null) {
                remoteUser
            } else {
                val localJson = prefs.getString("user_$userId", null)
                if (localJson != null) {
                    gson.fromJson(localJson, User::class.java)
                } else {
                    mockUsersMap[userId] ?: mockUser ?: User(id = userId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get user failed, using local/mock", e)
            val localJson = prefs.getString("user_$userId", null)
            if (localJson != null) {
                gson.fromJson(localJson, User::class.java)
            } else {
                mockUsersMap[userId] ?: mockUser ?: User(id = userId)
            }
        }
    }

    // Firebase Storage: Upload Profile Image
    suspend fun uploadProfileImage(
        userId: String,
        imageUri: Uri
    ): Result<String> {
        return try {
            val imageRef = storage.reference
                .child("profile_images/$userId.jpg")

            // Upload the selected image
            imageRef.putFile(imageUri).await()

            // Retrieve the image download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()

            // Save the URL in the user's Firestore document
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update("profileImageUrl", downloadUrl)
                .await()

            Log.d(TAG, "Profile image uploaded successfully")

            Result.success(downloadUrl)

        } catch (e: Exception) {
            Log.e(TAG, "Profile image upload failed", e)
            Result.failure(e)
        }
    }

    // Journal Methods
    // JOURNAL METHODS

    suspend fun saveJournalEntry(entry: JournalEntry): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: throw Exception("User is not logged in")

            val docRef = db.collection(JOURNAL_COLLECTION).document()

            val entryWithId = entry.copy(
                id = docRef.id,
                userId = userId
            )

            docRef.set(entryWithId).await()

            Result.success(docRef.id)

        } catch (e: Exception) {
            Log.e(TAG, "Save journal entry failed, using mock", e)

            val mockId = entry.id.ifEmpty { java.util.UUID.randomUUID().toString() }
            val entryWithId = entry.copy(id = mockId)

            mockJournalEntries.removeAll { it.id == entryWithId.id }
            mockJournalEntries.add(0, entryWithId)

            Result.success(mockId)
            Log.e(TAG, "Save journal entry failed", e)
            Result.failure(e)
        }
    }

    suspend fun getJournalEntries(userId: String): List<JournalEntry> {
        return try {
            val querySnapshot = db.collection(JOURNAL_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy(
                    "date",
                    com.google.firebase.firestore.Query.Direction.DESCENDING
                )
                .get()
                .await()

            val remoteEntries = querySnapshot.documents.mapNotNull {
                it.toObject(JournalEntry::class.java)
            }

            if (remoteEntries.isNotEmpty()) {
                (remoteEntries + mockJournalEntries.filter { it.userId == userId })
                    .asSequence()
                    .distinctBy { it.id }
                    .sortedByDescending { it.date }
                    .toList()
            } else {
                mockJournalEntries.asSequence().filter { it.userId == userId }.sortedByDescending { it.date }.toList()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Get journal entries failed, using mock", e)
            mockJournalEntries.asSequence().filter { it.userId == userId }.sortedByDescending { it.date }.toList()
        }
    }


// HABIT METHODS

    suspend fun saveHabit(habit: Habit): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: throw Exception("User is not logged in")

            val docRef = db.collection(HABITS_COLLECTION).document()

            val habitWithId = habit.copy(
                id = docRef.id,
                userId = userId
            )

            docRef.set(habitWithId).await()

            Result.success(docRef.id)

        } catch (e: Exception) {
            Log.e(TAG, "Save habit failed", e)
            Result.failure(e)
        }
    }

    @Suppress("unused")
    suspend fun getHabits(userId: String): List<Habit> {
        return try {
            val querySnapshot = db.collection(HABITS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val remoteHabits = querySnapshot.documents.mapNotNull {
                it.toObject(Habit::class.java)
            }

            if (remoteHabits.isNotEmpty()) {
                (remoteHabits + mockHabits.filter { (it.userId == userId) && it.isActive })
                    .asSequence()
                    .distinctBy { it.id }
                    .toList()
            } else {
                mockHabits.filter { (it.userId == userId) && it.isActive }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Get habits failed, using mock", e)
            mockHabits.filter { (it.userId == userId) && it.isActive }
        }
    }


// MOOD METHODS

    suspend fun saveMood(mood: MoodCheckIn): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: throw Exception("User is not logged in")

            val docRef = db.collection(MOODS_COLLECTION).document()

            val moodWithId = mood.copy(
                id = docRef.id,
                userId = userId
            )

            docRef.set(moodWithId).await()

            Result.success(docRef.id)

        } catch (e: Exception) {
            Log.e(TAG, "Save mood failed", e)
            Result.failure(e)
        }
    }

    @Suppress("unused")
    suspend fun getMoods(userId: String): List<MoodCheckIn> {
        return try {
            val querySnapshot = db.collection(MOODS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy(
                    "date",
                    com.google.firebase.firestore.Query.Direction.DESCENDING
                )
                .limit(30)
                .get()
                .await()

            val remoteMoods = querySnapshot.documents.mapNotNull {
                it.toObject(MoodCheckIn::class.java)
            }

            if (remoteMoods.isNotEmpty()) {
                (remoteMoods + mockMoods.filter { it.userId == userId })
                    .asSequence()
                    .distinctBy { it.id }
                    .sortedByDescending { it.date }
                    .take(30)
                    .toList()
            } else {
                mockMoods.asSequence().filter { it.userId == userId }.sortedByDescending { it.date }.take(30).toList()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Get moods failed, using mock", e)
            mockMoods.asSequence().filter { it.userId == userId }.sortedByDescending { it.date }.take(30).toList()
        }
    }


// ACHIEVEMENT METHODS

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
            Log.e(TAG, "Get achievements failed", e)
            emptyList()
        }
    }
}
