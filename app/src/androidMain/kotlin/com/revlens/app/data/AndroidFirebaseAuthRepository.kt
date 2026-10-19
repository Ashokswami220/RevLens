package com.revlens.app.data

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.revlens.app.core.platform.SecureStorage
import com.revlens.app.core.ui.AppError
import com.revlens.app.core.ui.AppException
import com.revlens.app.domain.model.AuthTokens
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.repository.AuthRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidFirebaseAuthRepository(
    private val context: Context,
    private val secureStorage: SecureStorage? = null
) : AuthRepository {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val json = Json { ignoreUnknownKeys = true }
    private var cachedUser: UserAccount? = null

    override suspend fun loginWithGoogle(
        idToken: String?,
        email: String?,
        name: String?,
        avatarUrl: String?
    ): Result<UserAccount> {
        try {
            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential)
                    .await()
                val fbUser = authResult.user ?: throw AppException(AppError.Unauthorized)

                val user = UserAccount(
                    id = fbUser.uid,
                    name = fbUser.displayName ?: name ?: "User",
                    email = fbUser.email ?: email ?: "",
                    emailVerified = fbUser.isEmailVerified,
                    avatarUrl = fbUser.photoUrl?.toString() ?: avatarUrl,
                    jobTitle = "Founder"
                )
                cachedUser = user
                saveTokens(user.id)

                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val anotherId =
                    fbUser.providerData.firstOrNull { it.providerId == "google.com" }?.uid
                        ?: fbUser.providerData.firstOrNull()?.uid
                        ?: fbUser.uid
                val userDoc = mapOf(
                    "id" to fbUser.uid,
                    "email" to (fbUser.email ?: email ?: ""),
                    "name" to (fbUser.displayName ?: name ?: "User"),
                    "jobTitle" to "Founder",
                    "anotherId" to anotherId,
                    "createdAt" to (fbUser.metadata?.creationTimestamp
                        ?: System.currentTimeMillis()),
                    "updatedAt" to System.currentTimeMillis()
                )
                try {
                    db.collection("users")
                        .document(fbUser.uid)
                        .set(userDoc, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            android.util.Log.d(
                                "RevLensFirestore",
                                "Successfully saved user document to users/${fbUser.uid}"
                            )
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e(
                                "RevLensFirestore", "Failed to save user document: ${e.message}", e
                            )
                            if (e.message?.contains(
                                    "PERMISSION_DENIED", ignoreCase = true
                                ) == true
                            ) {
                                android.util.Log.e(
                                    "RevLensFirestore",
                                    "PERMISSION_DENIED: Firestore security rules in Firebase Console are blocking writes! Update rules in Firebase Console."
                                )
                            }
                        }
                } catch (e: Exception) {
                    android.util.Log.e(
                        "RevLensFirestore", "Failed to trigger save user profile in Firestore", e
                    )
                }

                return Result.success(user)
            }

            val current = getCurrentUser()
            if (current != null) {
                return Result.success(current)
            }

            return Result.failure(AppException(AppError.Unauthorized))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<UserAccount> {
        return Result.failure(AppException(AppError.InvalidCredentials))
    }

    override suspend fun signUp(
        name: String, email: String, password: String
    ): Result<UserAccount> {
        return Result.failure(
            AppException(AppError.Conflict("Sign up is handled via Google Sign-In"))
        )
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun validateResetToken(token: String): Result<Boolean> {
        return Result.success(false)
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> {
        return Result.failure(AppException(AppError.Unauthorized))
    }

    override suspend fun logout(): Result<Unit> {
        try {
            auth.signOut()
            cachedUser = null
            secureStorage?.remove("auth_tokens")
            try {
                val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                )
                    .build()
                com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                    .signOut()
            } catch (_: Exception) {
            }
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun refreshToken(): Result<AuthTokens> {
        val user = cachedUser ?: getCurrentUser() ?: return Result.failure(
            AppException(AppError.Unauthorized)
        )
        val tokens = AuthTokens(
            accessToken = "android_fb_acc_${user.id}",
            refreshToken = "android_fb_ref_${user.id}"
        )
        return Result.success(tokens)
    }

    override suspend fun getCurrentUser(): UserAccount? {
        val fbUser = auth.currentUser ?: return null
        var jobTitle = cachedUser?.jobTitle
        if (jobTitle == null) {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val doc = db.collection("users")
                    .document(fbUser.uid)
                    .get()
                    .await()
                if (doc.exists()) {
                    jobTitle = doc.getString("jobTitle")
                }
            } catch (_: Exception) {
            }
        }
        val user = UserAccount(
            id = fbUser.uid,
            name = fbUser.displayName ?: "User",
            email = fbUser.email ?: "",
            emailVerified = fbUser.isEmailVerified,
            avatarUrl = fbUser.photoUrl?.toString(),
            jobTitle = jobTitle ?: "Founder"
        )
        cachedUser = user
        return user
    }

    override suspend fun getTokens(): AuthTokens? {
        val user = cachedUser ?: getCurrentUser() ?: return null
        return AuthTokens(
            accessToken = "android_fb_acc_${user.id}",
            refreshToken = "android_fb_ref_${user.id}"
        )
    }

    override suspend fun updateProfile(name: String, jobTitle: String?): Result<UserAccount> {
        val fbUser = auth.currentUser ?: return Result.failure(AppException(AppError.Unauthorized))
        try {
            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName(name.trim())
                .build()
            fbUser.updateProfile(updates)
                .await()

            val updated = UserAccount(
                id = fbUser.uid,
                name = name.trim(),
                email = fbUser.email ?: "",
                emailVerified = fbUser.isEmailVerified,
                avatarUrl = fbUser.photoUrl?.toString(),
                jobTitle = jobTitle
            )
            cachedUser = updated

            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            try {
                val updateMap = mutableMapOf<String, Any>(
                    "name" to name.trim(),
                    "updatedAt" to System.currentTimeMillis()
                )
                if (jobTitle != null) {
                    updateMap["jobTitle"] = jobTitle
                }
                db.collection("users")
                    .document(fbUser.uid)
                    .set(updateMap, com.google.firebase.firestore.SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                android.util.Log.e(
                    "RevLensFirestore", "Failed to update user profile in Firestore", e
                )
            }

            return Result.success(updated)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        try {
            auth.currentUser?.delete()
                ?.await()
            cachedUser = null
            secureStorage?.remove("auth_tokens")
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun saveTokens(userId: String) {
        val tokens = AuthTokens(
            accessToken = "android_fb_acc_$userId",
            refreshToken = "android_fb_ref_$userId"
        )
        try {
            secureStorage?.put("auth_tokens", json.encodeToString(AuthTokens.serializer(), tokens))
        } catch (_: Exception) {
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resumeWithException(it) }
        addOnCanceledListener { cont.cancel() }
    }
}
