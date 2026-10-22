package com.revlens.app.data

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.revlens.app.core.ui.AppError
import com.revlens.app.core.ui.AppException
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.session.SessionManager
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import androidx.core.content.edit

class AndroidFirebaseAuthRepository(
    private val context: Context,
    private val sessionManager: SessionManager? = null
) : AuthRepository {

    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (_: Throwable) {
        null
    }

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Throwable) {
        null
    }

    private val prefs by lazy {
        context.getSharedPreferences("revlens_auth_session", Context.MODE_PRIVATE)
    }

    private var cachedUser: UserAccount? = null

    init {
        val current = resolveCurrentUserSync()
        if (current != null) {
            cachedUser = current
        }
        auth?.addAuthStateListener { fbAuth ->
            val fbUser = fbAuth.currentUser
            if (fbUser != null) {
                val account = fbUser.toUserAccount()
                cachedUser = account
                saveUserToPrefs(account)
            }
        }
    }

    private fun saveUserToPrefs(user: UserAccount) {
        prefs.edit {
            putString("uid", user.id)
                .putString("name", user.name)
                .putString("email", user.email)
                .putString("avatarUrl", user.avatarUrl)
                .putString("jobTitle", user.jobTitle)
        }
    }

    private fun clearUserPrefs() {
        prefs.edit { clear() }
    }

    private fun loadUserFromPrefs(): UserAccount? {
        val uid = prefs.getString("uid", null) ?: return null
        val email = prefs.getString("email", null) ?: return null
        val name = prefs.getString("name", null) ?: email.substringBefore("@")
        val avatarUrl = prefs.getString("avatarUrl", null)
        val jobTitle = prefs.getString("jobTitle", null)
        return UserAccount(
            id = uid,
            name = name,
            email = email,
            emailVerified = true,
            avatarUrl = avatarUrl,
            jobTitle = jobTitle
        )
    }

    private fun resolveCurrentUserSync(): UserAccount? {
        val fbUser = auth?.currentUser
        if (fbUser != null) {
            val acc = fbUser.toUserAccount()
            saveUserToPrefs(acc)
            return acc
        }
        return cachedUser ?: loadUserFromPrefs()
    }

    override suspend fun loginWithGoogle(
        idToken: String?,
        email: String?,
        name: String?,
        avatarUrl: String?
    ): Result<UserAccount> {
        val firebaseAuth = auth
        if (firebaseAuth != null && !idToken.isNullOrBlank()) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val account = firebaseUser.toUserAccount(
                        fallbackName = name,
                        fallbackEmail = email
                    )
                    cachedUser = account
                    saveUserToPrefs(account)
                    sessionManager?.onLoggedIn(account)
                    saveUserToFirestore(account)
                    return Result.success(account)
                }
            } catch (e: Throwable) {
                if (email.isNullOrBlank()) {
                    return Result.failure(AppException(AppError.Unknown(e)))
                }
            }
        }

        val resolvedEmail = email?.trim()?.lowercase()
        if (resolvedEmail.isNullOrBlank()) {
            return Result.failure(
                AppException(
                    AppError.Unknown(
                        IllegalStateException("Google Sign-In was cancelled or failed.")
                    )
                )
            )
        }

        val resolvedName = name?.trim()?.ifBlank { null }
            ?: resolvedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        val fallbackId = auth?.currentUser?.uid ?: "usr_google_${resolvedEmail.hashCode()}"
        val fallbackUser = UserAccount(
            id = fallbackId,
            name = resolvedName,
            email = resolvedEmail,
            emailVerified = true,
            avatarUrl = avatarUrl
        )
        cachedUser = fallbackUser
        saveUserToPrefs(fallbackUser)
        sessionManager?.onLoggedIn(fallbackUser)
        saveUserToFirestore(fallbackUser)
        return Result.success(fallbackUser)
    }

    override suspend fun logout(): Result<Unit> {
        try {
            auth?.signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(context, gso).signOut()
        } catch (_: Throwable) {
        }
        cachedUser = null
        clearUserPrefs()
        sessionManager?.signOut(wipeLocalSyncedData = true)
        return Result.success(Unit)
    }

    override suspend fun getCurrentUser(): UserAccount? {
        val user = resolveCurrentUserSync()
        if (user != null) {
            sessionManager?.onLoggedIn(user)
        }
        return user
    }

    override suspend fun updateProfile(name: String, jobTitle: String?): Result<UserAccount> {
        val current = resolveCurrentUserSync()
            ?: return Result.failure(AppException(AppError.Unauthorized))
        try {
            val fbUser = auth?.currentUser
            if (fbUser != null) {
                val req = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                fbUser.updateProfile(req).await()
            }
        } catch (_: Throwable) {
        }
        val updated = current.copy(name = name.trim(), jobTitle = jobTitle)
        cachedUser = updated
        saveUserToPrefs(updated)
        sessionManager?.onLoggedIn(updated)
        saveUserToFirestore(updated)
        return Result.success(updated)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        try {
            val uid = resolveCurrentUserSync()?.id
            if (!uid.isNullOrBlank()) {
                firestore?.collection("users")?.document(uid)?.delete()?.await()
            }
            auth?.currentUser?.delete()?.await()
        } catch (_: Throwable) {
        }
        cachedUser = null
        clearUserPrefs()
        sessionManager?.signOut(wipeLocalSyncedData = true)
        return Result.success(Unit)
    }

    private suspend fun saveUserToFirestore(user: UserAccount) {
        val db = firestore ?: return
        if (user.id.isBlank()) return
        try {
            val data = mutableMapOf<String, Any>(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email.trim().lowercase(),
                "updatedAt" to System.currentTimeMillis()
            )
            user.avatarUrl?.let { data["avatarUrl"] = it }
            user.jobTitle?.let { data["jobTitle"] = it }
            db.collection("users")
                .document(user.id)
                .set(data, SetOptions.merge())
                .await()
        } catch (_: Throwable) {
        }
    }

    private fun FirebaseUser.toUserAccount(
        fallbackName: String? = null,
        fallbackEmail: String? = null
    ): UserAccount {
        val resolvedEmail = (email ?: fallbackEmail ?: "").trim().lowercase()
        val resolvedName = displayName?.takeIf { it.isNotBlank() }
            ?: fallbackName?.takeIf { it.isNotBlank() }
            ?: resolvedEmail.substringBefore("@").ifBlank { "RevLens User" }
        return UserAccount(
            id = uid,
            name = resolvedName,
            email = resolvedEmail,
            emailVerified = isEmailVerified,
            avatarUrl = photoUrl?.toString()
        )
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener {
        if (cont.isActive) cont.resume(it)
    }
    addOnFailureListener {
        if (cont.isActive) cont.resumeWithException(it)
    }
    addOnCanceledListener {
        if (cont.isActive) cont.cancel()
    }
}
