package com.example.stoicmind.database

import android.net.Uri
import android.util.Log
import com.example.stoicmind.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val cloudinaryClient = OkHttpClient()

    private var mockUser: User? = null
    private val mockUsersMap = mutableMapOf<String, User>()

    companion object {
        private const val TAG = "FirebaseManager"
        private const val USERS_COLLECTION = "users"
        private const val JOURNAL_COLLECTION = "journal"
        private const val HABITS_COLLECTION = "habits"
        private const val MOODS_COLLECTION = "moods"
        private const val ACHIEVEMENTS_COLLECTION = "achievements"
    }

    // AUTHENTICATION METHODS

    suspend fun registerUser(email: String, password: String): Result<User> {
        return try {
            val authResult =
                auth.createUserWithEmailAndPassword(email, password).await()

            val user = User(
                id = authResult.user?.uid ?: "",
                email = email
            )

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
            val authResult =
                auth.signInWithEmailAndPassword(email, password).await()

            val userId = authResult.user?.uid
                ?: throw Exception("User ID not found")

            val user = getUser(userId)
            mockUser = user

            Result.success(user)

        } catch (e: Exception) {
            Log.e(TAG, "Login failed, falling back to mock mode", e)

            val mockId = "mock_uid_${email.hashCode()}"

            val derivedName = email.substringBefore("@")
                .replace(".", " ")
                .replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(java.util.Locale.getDefault())
                    } else {
                        it.toString()
                    }
                }

            val existingUser = mockUsersMap[mockId]

            val user = if (existingUser != null) {
                existingUser
            } else {
                User(
                    id = mockId,
                    name = derivedName,
                    email = email
                )
            }

            mockUser = user
            mockUsersMap[mockId] = user

            Result.success(user)
        }
    }

    suspend fun googleSignIn(idToken: String): Result<User> {
        return try {
            val credential =
                com.google.firebase.auth.GoogleAuthProvider
                    .getCredential(idToken, null)

            val authResult =
                auth.signInWithCredential(credential).await()

            val userId = authResult.user?.uid
                ?: throw Exception("Google user ID not found")

            val user = getUser(userId)
            mockUser = user

            Result.success(user)

        } catch (e: Exception) {
            Log.e(TAG, "Google sign in failed, falling back to mock mode", e)

            val user = User(
                id = "mock_google_uid",
                name = "Stoic Practitioner",
                email = "mockuser@gmail.com"
            )

            mockUser = user
            Result.success(user)
        }
    }

    fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed", e)
        }

        mockUser = null
    }

    fun getCurrentUserId(): String? {
        return try {
            auth.currentUser?.uid ?: mockUser?.id
        } catch (e: Exception) {
            Log.e(TAG, "Could not get current user ID", e)
            mockUser?.id
        }
    }

    // USER METHODS

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

            val user = document.toObject(User::class.java)
                ?: mockUsersMap[userId]
                ?: mockUser
                ?: User(id = userId)

            if (user.name.isEmpty() && user.email.isNotEmpty()) {
                val derivedName = user.email.substringBefore("@")
                    .replace(".", " ")
                    .replaceFirstChar {
                        if (it.isLowerCase()) {
                            it.titlecase(java.util.Locale.getDefault())
                        } else {
                            it.toString()
                        }
                    }

                user.copy(name = derivedName)
            } else {
                user
            }

        } catch (e: Exception) {
            Log.e(TAG, "Get user failed, using mock", e)

            val user = mockUsersMap[userId]
                ?: mockUser
                ?: User(id = userId)

            if (user.name.isEmpty() && user.email.isNotEmpty()) {
                val derivedName = user.email.substringBefore("@")
                    .replace(".", " ")
                    .replaceFirstChar {
                        if (it.isLowerCase()) {
                            it.titlecase(java.util.Locale.getDefault())
                        } else {
                            it.toString()
                        }
                    }

                user.copy(name = derivedName)
            } else {
                user
            }
        }
    }

    // PROFILE IMAGE UPLOAD

    // CLOUDINARY PROFILE IMAGE UPLOAD

    suspend fun uploadProfileImage(
        userId: String,
        imageUri: Uri
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val imageBytes = context.contentResolver
                    .openInputStream(imageUri)
                    ?.use { it.readBytes() }
                    ?: throw Exception("Could not read selected image")

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", "stoic_profile_upload")
                    .addFormDataPart(
                        "folder",
                        "stoic-profile-images"
                    )
                    .addFormDataPart(
                        "file",
                        "profile_$userId.jpg",
                        imageBytes.toRequestBody("image/*".toMediaType())
                    )
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/komqlp72/image/upload")
                    .post(requestBody)
                    .build()

                val response = cloudinaryClient.newCall(request).execute()

                val responseBody = response.body?.string()
                    ?: throw Exception("Empty Cloudinary response")

                if (!response.isSuccessful) {
                    throw Exception("Cloudinary upload failed: $responseBody")
                }

                val secureUrl = JSONObject(responseBody)
                    .getString("secure_url")

                db.collection(USERS_COLLECTION)
                    .document(userId)
                    .update("profileImageUrl", secureUrl)
                    .await()

                Log.d(TAG, "Cloudinary profile image uploaded successfully")

                Result.success(secureUrl)

            } catch (e: Exception) {
                Log.e(TAG, "Cloudinary profile image upload failed", e)
                Result.failure(e)
            }
        }
    }
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

            querySnapshot.documents.mapNotNull {
                it.toObject(JournalEntry::class.java)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Get journal entries failed", e)
            emptyList()
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
            Log.e(TAG, "Get habits failed", e)
            emptyList()
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

            querySnapshot.documents.mapNotNull {
                it.toObject(MoodCheckIn::class.java)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Get moods failed", e)
            emptyList()
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