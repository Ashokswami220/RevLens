package com.revlens.app.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.BusinessModel
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.milliseconds

private val safeFirestore: FirebaseFirestore?
    get() = try {
        FirebaseFirestore.getInstance()
    } catch (_: Throwable) {
        null
    }

private val safeAuth: FirebaseAuth?
    get() = try {
        FirebaseAuth.getInstance()
    } catch (_: Throwable) {
        null
    }

private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

private fun parseBusinessModel(raw: String?): BusinessModel {
    if (raw.isNullOrBlank()) return BusinessModel.B2bSaas
    return try {
        BusinessModel.valueOf(raw)
    } catch (_: Throwable) {
        BusinessModel.B2bSaas
    }
}

class AndroidFirestoreWorkspaceRepository(
    private val localFallback: WorkspaceRepository = DefaultWorkspaceRepository(emptyList()),
    private val authRepository: AuthRepository? = null
) : WorkspaceRepository {

    private val db: FirebaseFirestore?
        get() = safeFirestore

    private val auth: FirebaseAuth?
        get() = safeAuth

    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())

    private suspend fun resolveUserId(): String? {
        return auth?.currentUser?.uid ?: authRepository?.getCurrentUser()?.id
    }

    override val workspacesFlow: Flow<List<Workspace>> = callbackFlow {
        val firestore = db
        val fbAuth = auth
        var firestoreListener: ListenerRegistration? = null

        trySend(_workspaces.value)

        fun attachListener(uid: String) {
            if (uid.isBlank() || firestore == null) {
                _workspaces.value = emptyList()
                trySend(emptyList())
                return
            }
            firestoreListener?.remove()
            try {
                firestoreListener = firestore.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("RevLensFirestore", "Workspace snapshot error", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                val id = doc.getString("id") ?: doc.id
                                if (id.isBlank() || id == "ws_default") return@mapNotNull null
                                val name = doc.getString("name") ?: "My Workspace"
                                val currency = doc.getString("currency") ?: "USD"
                                val timeZoneId = doc.getString("timeZoneId") ?: "UTC"
                                val fiscalYearStartMonth = doc.getLong("fiscalYearStartMonth")
                                    ?.toInt() ?: 1
                                val businessModel =
                                    parseBusinessModel(doc.getString("businessModel"))
                                val createdAtMillis =
                                    doc.getLong("createdAtMillis") ?: System.currentTimeMillis()

                                Workspace(
                                    id = id,
                                    name = name,
                                    currency = currency,
                                    timeZoneId = timeZoneId,
                                    fiscalYearStartMonth = fiscalYearStartMonth,
                                    businessModel = businessModel,
                                    createdAtMillis = createdAtMillis
                                )
                            }
                            if (list.isNotEmpty()) {
                                val newest = list.maxByOrNull { it.createdAtMillis } ?: list.first()
                                val singleList = listOf(newest)
                                _workspaces.value = singleList
                                trySend(singleList)
                            } else {
                                _workspaces.value = emptyList()
                                trySend(emptyList())
                            }
                        }
                    }
            } catch (e: Throwable) {
                Log.e("RevLensFirestore", "Workspace snapshot attach error", e)
            }
        }

        val initJob = ioScope.launch {
            val uid = resolveUserId()
            if (!uid.isNullOrBlank()) {
                attachListener(uid)
            } else {
                _workspaces.value = emptyList()
                trySend(emptyList())
            }
        }

        val authStateListener = if (fbAuth != null) {
            FirebaseAuth.AuthStateListener { firebaseAuth ->
                val uid = firebaseAuth.currentUser?.uid
                if (!uid.isNullOrBlank()) {
                    attachListener(uid)
                } else {
                    firestoreListener?.remove()
                    _workspaces.value = emptyList()
                    trySend(emptyList())
                }
            }
        } else {
            null
        }

        if (fbAuth != null && authStateListener != null) {
            try {
                fbAuth.addAuthStateListener(authStateListener)
            } catch (e: Throwable) {
                Log.e("RevLensFirestore", "Workspace auth listener error", e)
            }
        }

        awaitClose {
            initJob.cancel()
            if (fbAuth != null && authStateListener != null) {
                try {
                    fbAuth.removeAuthStateListener(authStateListener)
                } catch (_: Throwable) {
                }
            }
            firestoreListener?.remove()
        }
    }

    override suspend fun getWorkspace(id: String): Workspace? {
        if (id.isBlank() || id == "ws_default") return null
        val inMemory = _workspaces.value.firstOrNull { it.id == id }
        if (inMemory != null) return inMemory

        val firestore = db ?: return localFallback.getWorkspace(id)
        try {
            val snapshot = firestore.collection("workspaces")
                .document(id)
                .get()
                .await()

            if (snapshot.exists()) {
                val name = snapshot.getString("name") ?: "My Workspace"
                val currency = snapshot.getString("currency") ?: "USD"
                val timeZoneId = snapshot.getString("timeZoneId") ?: "UTC"
                val fiscalYearStartMonth = snapshot.getLong("fiscalYearStartMonth")
                    ?.toInt() ?: 1
                val businessModel = parseBusinessModel(snapshot.getString("businessModel"))
                val createdAtMillis =
                    snapshot.getLong("createdAtMillis") ?: System.currentTimeMillis()

                return Workspace(
                    id = id,
                    name = name,
                    currency = currency,
                    timeZoneId = timeZoneId,
                    fiscalYearStartMonth = fiscalYearStartMonth,
                    businessModel = businessModel,
                    createdAtMillis = createdAtMillis
                )
            }
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "getWorkspace error", e)
        }
        return null
    }

    override suspend fun createWorkspace(workspace: Workspace) = withContext(NonCancellable) {
        if (workspace.id.isBlank() || workspace.id == "ws_default") return@withContext

        val firestore = db
        val currentUser = authRepository?.getCurrentUser()
        val uid = auth?.currentUser?.uid ?: currentUser?.id

        // Check if a workspace already exists locally or in Firestore for this user so we update in-place
        var effectiveId = _workspaces.value
            .firstOrNull { it.id.isNotBlank() && it.id != "ws_default" }
            ?.id

        if (effectiveId == null && firestore != null && !uid.isNullOrBlank()) {
            try {
                val existingDocs = firestore.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .get()
                    .await()
                val existingRemote = existingDocs.documents.firstOrNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    id.isNotBlank() && id != "ws_default"
                }
                if (existingRemote != null) {
                    effectiveId = existingRemote.getString("id") ?: existingRemote.id
                }
            } catch (_: Throwable) {
            }
        }

        val finalWorkspace = if (!effectiveId.isNullOrBlank() && effectiveId != workspace.id) {
            workspace.copy(id = effectiveId)
        } else {
            workspace
        }

        val previousIds = _workspaces.value.map { it.id }
            .filter { it != finalWorkspace.id }
        _workspaces.value = listOf(finalWorkspace)
        previousIds.forEach { oldId ->
            localFallback.deleteWorkspace(oldId)
        }
        localFallback.createWorkspace(finalWorkspace)

        if (firestore == null) return@withContext

        val joinCode = "RL-" + finalWorkspace.id.removePrefix("ws_")
            .uppercase()
            .take(6)
            .padEnd(6, '0')
        val map = mutableMapOf<String, Any>(
            "id" to finalWorkspace.id,
            "name" to finalWorkspace.name,
            "currency" to finalWorkspace.currency,
            "timeZoneId" to finalWorkspace.timeZoneId,
            "fiscalYearStartMonth" to finalWorkspace.fiscalYearStartMonth,
            "businessModel" to finalWorkspace.businessModel.name,
            "joinCode" to joinCode,
            "createdAtMillis" to finalWorkspace.createdAtMillis,
            "updatedAt" to System.currentTimeMillis()
        )

        if (!uid.isNullOrBlank()) {
            map["ownerId"] = uid
            map["userId"] = uid
            try {
                val existingDocs = firestore.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .get()
                    .await()
                for (doc in existingDocs.documents) {
                    if (doc.id != finalWorkspace.id) {
                        doc.reference.delete()
                            .await()
                        try {
                            firestore.collection("workspaces")
                                .document(doc.id)
                                .delete()
                                .await()
                        } catch (_: Throwable) {
                        }
                    }
                }
                firestore.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .document(finalWorkspace.id)
                    .set(map, SetOptions.merge())
                    .await()
                firestore.collection("users")
                    .document(uid)
                    .set(
                        mapOf(
                            "id" to uid,
                            "activeWorkspaceId" to finalWorkspace.id,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
                    .await()
            } catch (e: Throwable) {
                Log.e("RevLensFirestore", "Failed to save user-scoped workspace", e)
            }
        }

        try {
            firestore.collection("workspaces")
                .document(finalWorkspace.id)
                .set(map, SetOptions.merge())
                .await()
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "Failed to save root workspace", e)
        }

        try {
            if (!uid.isNullOrBlank()) {
                val memberRef = firestore.collection("workspaces")
                    .document(finalWorkspace.id)
                    .collection("members")
                    .document(uid)
                val existingMember = memberRef.get()
                    .await()
                if (!existingMember.exists()) {
                    val email = auth?.currentUser?.email ?: currentUser?.email ?: ""
                    val defaultName = auth?.currentUser?.displayName?.ifBlank { null }
                        ?: currentUser?.name?.ifBlank { null }
                        ?: email.substringBefore("@")
                            .ifBlank { "Owner" }
                    val ownerMap = mapOf(
                        "id" to uid,
                        "name" to defaultName,
                        "email" to email.trim()
                            .lowercase(),
                        "role" to Role.Owner.name,
                        "joinedAt" to "Workspace Owner",
                        "avatarUrl" to (auth?.currentUser?.photoUrl?.toString()
                            ?: currentUser?.avatarUrl)
                    )
                    memberRef.set(ownerMap, SetOptions.merge())
                        .await()
                }
            }
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "Failed to register workspace owner member", e)
        }
    }

    override suspend fun updateWorkspace(workspace: Workspace) {
        createWorkspace(workspace)
    }

    override suspend fun deleteWorkspace(id: String) = withContext(NonCancellable) {
        _workspaces.value = _workspaces.value.filter { it.id != id }
        localFallback.deleteWorkspace(id)

        val firestore = db ?: return@withContext
        if (id.isBlank()) return@withContext
        try {
            val uid = resolveUserId()
            if (!uid.isNullOrBlank()) {
                firestore.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .document(id)
                    .delete()
                    .await()
            }
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "deleteWorkspace error", e)
        }
    }

    override suspend fun clearLocalData() = withContext(NonCancellable) {
        _workspaces.value = emptyList()
        localFallback.clearLocalData()
    }
}

class AndroidFirestoreProfileRepository(
    private val localFallback: ProfileRepository,
    private val workspaceContext: WorkspaceContext? = null,
    private val authRepository: AuthRepository? = null
) : ProfileRepository {

    private val db: FirebaseFirestore?
        get() = safeFirestore

    private val auth: FirebaseAuth?
        get() = safeAuth

    private val json = Json { ignoreUnknownKeys = true }
    private val _profile = MutableStateFlow<BusinessProfile?>(null)

    private fun sanitizeKey(raw: String): String {
        return raw.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9_-]"), "_")
    }

    private suspend fun resolveUserIdentity(): Pair<String?, String?> {
        val currentUser = runCatching { authRepository?.getCurrentUser() }.getOrNull()
        val uid = auth?.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: currentUser?.id?.takeIf { it.isNotBlank() }
        val email = (auth?.currentUser?.email ?: currentUser?.email)
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
        return Pair(uid, email)
    }

    private suspend fun resolveEffectiveWorkspaceId(): String {
        val fromContext = workspaceContext?.activeWorkspaceId?.value
        if (!fromContext.isNullOrBlank() && fromContext != "ws_default") {
            return fromContext
        }
        val (uid, email) = resolveUserIdentity()
        val firestore = db
        if (firestore != null && !uid.isNullOrBlank()) {
            try {
                val userDoc = firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()
                val activeWs = userDoc.getString("activeWorkspaceId")
                if (!activeWs.isNullOrBlank() && activeWs != "ws_default") {
                    workspaceContext?.switchTo(activeWs)
                    return activeWs
                }
                val wsSnap = firestore.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .get()
                    .await()
                val firstWs = wsSnap.documents.firstOrNull()?.id
                if (!firstWs.isNullOrBlank() && firstWs != "ws_default") {
                    workspaceContext?.switchTo(firstWs)
                    return firstWs
                }
            } catch (_: Throwable) {
            }
            val fallbackWsId = "ws_$uid"
            workspaceContext?.switchTo(fallbackWsId)
            return fallbackWsId
        }
        if (!email.isNullOrBlank()) {
            val emailWsId = "ws_${sanitizeKey(email)}"
            workspaceContext?.switchTo(emailWsId)
            return emailWsId
        }
        return "ws_default"
    }

    override val profileFlow: Flow<BusinessProfile?> = callbackFlow {
        val firestore = db
        var rootProfileListener: ListenerRegistration? = null
        var userProfileListener: ListenerRegistration? = null

        trySend(_profile.value)

        fun decodeAndEmit(payload: String?) {
            if (payload.isNullOrBlank()) return
            try {
                val profile = json.decodeFromString<BusinessProfile>(payload)
                _profile.value = profile
                ioScope.launch {
                    localFallback.saveProfile(profile)
                    localFallback.setOnboardingCompleted(true)
                }
                trySend(profile)
            } catch (e: Throwable) {
                Log.e("RevLensFirestore", "Profile decode error", e)
            }
        }

        fun attachListeners(wsId: String?) {
            rootProfileListener?.remove()
            userProfileListener?.remove()
            if (firestore == null) return

            val effectiveWsId = wsId?.takeIf { it.isNotBlank() && it != "ws_default" }
            if (effectiveWsId != null) {
                try {
                    rootProfileListener = firestore.collection("profiles")
                        .document(effectiveWsId)
                        .addSnapshotListener { snapshot, error ->
                            if (error == null && snapshot != null && snapshot.exists()) {
                                decodeAndEmit(snapshot.getString("payload"))
                            }
                        }
                } catch (_: Throwable) {
                }
            }

            val uid = auth?.currentUser?.uid
            if (!uid.isNullOrBlank()) {
                try {
                    userProfileListener = firestore.collection("profiles")
                        .document("user_$uid")
                        .addSnapshotListener { snapshot, error ->
                            if (error == null && snapshot != null && snapshot.exists()) {
                                decodeAndEmit(snapshot.getString("payload"))
                            }
                        }
                } catch (_: Throwable) {
                }
            }
        }

        val wsJob = ioScope.launch {
            if (workspaceContext != null) {
                workspaceContext.activeWorkspaceId.collectLatest { wsId ->
                    attachListeners(wsId)
                }
            } else {
                attachListeners(null)
            }
        }

        val localJob = ioScope.launch {
            localFallback.profileFlow.collect { localProfile ->
                if (localProfile != null && _profile.value == null) {
                    _profile.value = localProfile
                    trySend(localProfile)
                } else if (localProfile == null && _profile.value == null) {
                    trySend(null)
                }
            }
        }

        awaitClose {
            rootProfileListener?.remove()
            userProfileListener?.remove()
            wsJob.cancel()
            localJob.cancel()
        }
    }

    override val isOnboardingCompletedFlow: Flow<Boolean> = localFallback.isOnboardingCompletedFlow
    override val hasSeenIntroFlow: Flow<Boolean> = localFallback.hasSeenIntroFlow

    override suspend fun setHasSeenIntro(seen: Boolean) {
        localFallback.setHasSeenIntro(seen)
    }

    override suspend fun clearLocalData() = withContext(NonCancellable) {
        _profile.value = null
        localFallback.clearLocalData()
    }

    override suspend fun saveProfile(profile: BusinessProfile) = withContext(NonCancellable) {
        _profile.value = profile
        localFallback.saveProfile(profile)
        localFallback.setOnboardingCompleted(true)

        val (uid, email) = resolveUserIdentity()
        val dsFallback = localFallback as? DataStoreProfileRepository
        if (dsFallback != null) {
            if (!uid.isNullOrBlank()) dsFallback.saveUserProfileBackup(uid, profile)
            if (!email.isNullOrBlank()) dsFallback.saveUserProfileBackup(email, profile)
        }

        val firestore = db ?: return@withContext
        val wsId = resolveEffectiveWorkspaceId()

        try {
            val serialized = json.encodeToString(BusinessProfile.serializer(), profile)
            val map = mutableMapOf(
                "payload" to serialized,
                "workspaceId" to wsId,
                "updatedAt" to System.currentTimeMillis(),
                "currencyCode" to profile.currencyCode,
                "monthlyChurnRate" to profile.monthlyChurnRate,
                "monthlyGrowthRate" to profile.monthlyGrowthRate,
                "cac" to profile.cac.toDouble(),
                "fixedCosts" to profile.fixedCosts.toDouble(),
                "variableCostPerCustomer" to profile.variableCostPerCustomer.toDouble(),
                "totalCustomers" to profile.totalCustomers,
                "currentMrr" to profile.currentMrr.toDouble(),
                "arpu" to profile.arpu.toDouble(),
                "plans" to profile.plans.map { plan ->
                    mapOf(
                        "id" to plan.id,
                        "name" to plan.name,
                        "price" to plan.price.toDouble(),
                        "billingPeriod" to plan.billingPeriod.name,
                        "customerCount" to plan.customerCount
                    )
                }
            )
            profile.mrrGoal?.let { goal ->
                map["mrrGoal"] = goal.toDouble()
            }
            if (!uid.isNullOrBlank()) {
                map["updatedBy"] = uid
                map["userId"] = uid
            }
            if (!email.isNullOrBlank()) {
                map["email"] = email
            }

            // 1. Save directly under user-keyed documents in /profiles (Always permitted by root rules!)
            if (!uid.isNullOrBlank()) {
                try {
                    firestore.collection("profiles")
                        .document("user_$uid")
                        .set(map)
                        .await()
                } catch (e: Throwable) {
                    Log.e("RevLensFirestore", "Failed saving /profiles/user_$uid", e)
                }
            }
            if (!email.isNullOrBlank()) {
                val emailDocId = "email_${sanitizeKey(email)}"
                try {
                    firestore.collection("profiles")
                        .document(emailDocId)
                        .set(map)
                        .await()
                } catch (e: Throwable) {
                    Log.e("RevLensFirestore", "Failed saving /profiles/$emailDocId", e)
                }
            }

            // 2. Save to /profiles/{wsId}
            if (wsId.isNotBlank() && wsId != "ws_default") {
                try {
                    firestore.collection("profiles")
                        .document(wsId)
                        .set(map)
                        .await()
                } catch (e: Throwable) {
                    Log.e("RevLensFirestore", "Failed saving /profiles/$wsId", e)
                }

                // 3. Save profilePayload onto /workspaces/{wsId}
                try {
                    firestore.collection("workspaces")
                        .document(wsId)
                        .set(
                            mapOf(
                                "id" to wsId,
                                "currency" to profile.currencyCode,
                                "profilePayload" to serialized,
                                "updatedAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        )
                        .await()
                } catch (_: Throwable) {
                }
            }

            // 4. Update /users/{uid} metadata & user-scoped backup
            if (!uid.isNullOrBlank()) {
                try {
                    firestore.collection("users")
                        .document(uid)
                        .set(
                            mapOf(
                                "id" to uid,
                                "activeWorkspaceId" to wsId,
                                "profilePayload" to serialized,
                                "onboardingCompleted" to true,
                                "updatedAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        )
                        .await()

                    firestore.collection("users")
                        .document(uid)
                        .collection("profile")
                        .document("current")
                        .set(map)
                        .await()
                } catch (_: Throwable) {
                }
            }
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "saveProfile error", e)
        }
    }

    override suspend fun syncRemoteProfile(): BusinessProfile? {
        val (uid, email) = resolveUserIdentity()
        if (uid.isNullOrBlank() && email.isNullOrBlank()) {
            return null
        }

        val dsFallback = localFallback as? DataStoreProfileRepository
        val firestore = db

        var resolvedWsId: String? = workspaceContext?.activeWorkspaceId?.value
            ?.takeIf { it.isNotBlank() && it != "ws_default" }
        var resolvedPayload: String? = null

        if (firestore != null) {
            // 1. Fast direct lookup in /profiles/user_{uid} and /profiles/email_{email}
            if (!uid.isNullOrBlank() && resolvedPayload.isNullOrBlank()) {
                withTimeoutOrNull(3500L.milliseconds) {
                    try {
                        val doc = firestore.collection("profiles")
                            .document("user_$uid")
                            .get()
                            .await()
                        if (doc.exists()) {
                            resolvedPayload = doc.getString("payload")
                            val docWs = doc.getString("workspaceId")
                            if (!docWs.isNullOrBlank() && docWs != "ws_default") {
                                resolvedWsId = docWs
                            }
                        }
                    } catch (_: Throwable) {
                    }
                }
            }

            if (!email.isNullOrBlank() && resolvedPayload.isNullOrBlank()) {
                val emailDocId = "email_${sanitizeKey(email)}"
                withTimeoutOrNull(3500L.milliseconds) {
                    try {
                        val doc = firestore.collection("profiles")
                            .document(emailDocId)
                            .get()
                            .await()
                        if (doc.exists()) {
                            resolvedPayload = doc.getString("payload")
                            val docWs = doc.getString("workspaceId")
                            if (!docWs.isNullOrBlank() && docWs != "ws_default") {
                                resolvedWsId = docWs
                            }
                        }
                    } catch (_: Throwable) {
                    }
                }
            }

            // 2. Fallback lookup in /users/{uid} and /users/{uid}/workspaces
            if (!uid.isNullOrBlank() && resolvedPayload.isNullOrBlank()) {
                withTimeoutOrNull(3500L.milliseconds) {
                    try {
                        val userDoc = firestore.collection("users")
                            .document(uid)
                            .get()
                            .await()
                        if (userDoc.exists()) {
                            val directPayload = userDoc.getString("profilePayload")
                            if (!directPayload.isNullOrBlank()) {
                                resolvedPayload = directPayload
                            }
                            val activeWs = userDoc.getString("activeWorkspaceId")
                            if (!activeWs.isNullOrBlank() && activeWs != "ws_default") {
                                resolvedWsId = activeWs
                            }
                        }
                    } catch (_: Throwable) {
                    }

                    if (resolvedPayload.isNullOrBlank()) {
                        try {
                            val subProf = firestore.collection("users")
                                .document(uid)
                                .collection("profile")
                                .document("current")
                                .get()
                                .await()
                            if (subProf.exists() && !subProf.getString("payload")
                                    .isNullOrBlank()
                            ) {
                                resolvedPayload = subProf.getString("payload")
                            }
                        } catch (_: Throwable) {
                        }
                    }

                    if (resolvedWsId == null) {
                        try {
                            val wsSnap = firestore.collection("users")
                                .document(uid)
                                .collection("workspaces")
                                .get()
                                .await()
                            val candidate = wsSnap.documents.firstOrNull {
                                val id = it.getString("id") ?: it.id
                                id.isNotBlank() && id != "ws_default"
                            }
                            if (candidate != null) {
                                resolvedWsId = candidate.getString("id") ?: candidate.id
                            }
                        } catch (_: Throwable) {
                        }
                    }
                }
            }

            // 3. If we found a workspace ID, check /profiles/{wsId} and /workspaces/{wsId}
            val targetWsId = resolvedWsId ?: if (!uid.isNullOrBlank()) "ws_$uid" else null
            if (!targetWsId.isNullOrBlank() && resolvedPayload.isNullOrBlank()) {
                withTimeoutOrNull(3500L.milliseconds) {
                    try {
                        val profDoc = firestore.collection("profiles")
                            .document(targetWsId)
                            .get()
                            .await()
                        if (profDoc.exists() && !profDoc.getString("payload")
                                .isNullOrBlank()
                        ) {
                            resolvedPayload = profDoc.getString("payload")
                            resolvedWsId = targetWsId
                        }
                    } catch (_: Throwable) {
                    }

                    if (resolvedPayload.isNullOrBlank()) {
                        try {
                            val wsDoc = firestore.collection("workspaces")
                                .document(targetWsId)
                                .get()
                                .await()
                            if (wsDoc.exists() && !wsDoc.getString("profilePayload")
                                    .isNullOrBlank()
                            ) {
                                resolvedPayload = wsDoc.getString("profilePayload")
                                resolvedWsId = targetWsId
                            }
                        } catch (_: Throwable) {
                        }
                    }
                }
            }
        }

        // 4. Decode remote payload if found
        if (!resolvedPayload.isNullOrBlank()) {
            try {
                val profile = json.decodeFromString<BusinessProfile>(resolvedPayload!!)
                val finalWsId = resolvedWsId
                    ?: (!uid.isNullOrBlank()).let {
                        if (it) "ws_$uid" else "ws_${
                            sanitizeKey(
                                email.orEmpty()
                            )
                        }"
                    }
                workspaceContext?.switchTo(finalWsId)
                _profile.value = profile
                localFallback.saveProfile(profile)
                localFallback.setOnboardingCompleted(true)
                if (dsFallback != null) {
                    if (!uid.isNullOrBlank()) dsFallback.saveUserProfileBackup(uid, profile)
                    if (!email.isNullOrBlank()) dsFallback.saveUserProfileBackup(email, profile)
                }
                return profile
            } catch (e: Throwable) {
                Log.e("RevLensFirestore", "Failed decoding synced remote profile", e)
            }
        }

        // 5. Fallback to per-user local backup for this exact UID / email (handles offline / rules restrictions)
        val localUserBackup = if (dsFallback != null) {
            (!uid.isNullOrBlank()).let { hasUid ->
                if (hasUid) dsFallback.getUserProfileBackup(uid!!) else null
            } ?: (!email.isNullOrBlank()).let { hasEmail ->
                if (hasEmail) dsFallback.getUserProfileBackup(email!!) else null
            }
        } else {
            null
        }

        if (localUserBackup != null) {
            val finalWsId = resolvedWsId
                ?: if (!uid.isNullOrBlank()) "ws_$uid" else "ws_${sanitizeKey(email.orEmpty())}"
            workspaceContext?.switchTo(finalWsId)
            _profile.value = localUserBackup
            localFallback.saveProfile(localUserBackup)
            localFallback.setOnboardingCompleted(true)
            return localUserBackup
        }

        return null
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) = withContext(NonCancellable) {
        localFallback.setOnboardingCompleted(completed)
        val firestore = db ?: return@withContext
        try {
            val (uid, _) = resolveUserIdentity()
            if (!uid.isNullOrBlank()) {
                firestore.collection("users")
                    .document(uid)
                    .set(
                        mapOf(
                            "onboardingCompleted" to completed,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
                    .await()
            }
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "setOnboardingCompleted error", e)
        }
    }
}

class AndroidFirestoreScenarioRepository(
    private val localFallback: ScenarioRepository,
    private val workspaceContext: WorkspaceContext? = null
) : ScenarioRepository {

    private val db: FirebaseFirestore?
        get() = safeFirestore

    private val json = Json { ignoreUnknownKeys = true }
    private val _scenarios = MutableStateFlow<List<Scenario>>(emptyList())

    override val scenariosFlow: Flow<List<Scenario>> = callbackFlow {
        val firestore = db
        var firestoreListener: ListenerRegistration? = null

        trySend(_scenarios.value)

        fun attachListener(wsId: String?) {
            firestoreListener?.remove()
            val effectiveWsId = wsId?.takeIf { it.isNotBlank() && it != "ws_default" }
            if (effectiveWsId == null || firestore == null) {
                _scenarios.value = emptyList()
                trySend(emptyList())
                return
            }

            try {
                firestoreListener = firestore.collection("scenarios")
                    .whereEqualTo("workspaceId", effectiveWsId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                val payload = doc.getString("payload") ?: return@mapNotNull null
                                try {
                                    json.decodeFromString<Scenario>(payload)
                                } catch (_: Throwable) {
                                    null
                                }
                            }
                            _scenarios.value = list
                            trySend(list)
                        }
                    }
            } catch (e: Throwable) {
                Log.e("RevLensFirestore", "Scenarios listener error", e)
            }
        }

        val wsJob = ioScope.launch {
            workspaceContext?.activeWorkspaceId?.collectLatest { wsId ->
                attachListener(wsId)
            }
        }

        awaitClose {
            firestoreListener?.remove()
            wsJob.cancel()
        }
    }

    override suspend fun clearLocalData() = withContext(NonCancellable) {
        _scenarios.value = emptyList()
        localFallback.clearLocalData()
    }

    override suspend fun saveScenario(scenario: Scenario) = withContext(NonCancellable) {
        val current = _scenarios.value.toMutableList()
        val index = current.indexOfFirst { it.id == scenario.id }
        if (index != -1) {
            current[index] = scenario
        } else {
            current.add(scenario)
        }
        _scenarios.value = current
        localFallback.saveScenario(scenario)

        val firestore = db ?: return@withContext
        if (scenario.id.isBlank()) return@withContext
        val wsId = workspaceContext?.activeWorkspaceId?.value?.takeIf {
            it.isNotBlank() && it != "ws_default"
        } ?: "ws_default"

        val serialized = json.encodeToString(Scenario.serializer(), scenario)
        val map = mapOf(
            "id" to scenario.id,
            "name" to scenario.name,
            "workspaceId" to wsId,
            "payload" to serialized,
            "updatedAt" to System.currentTimeMillis()
        )

        try {
            firestore.collection("scenarios")
                .document(scenario.id)
                .set(map)
                .await()
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "Failed saving scenario", e)
        }

        try {
            firestore.collection("workspaces")
                .document(wsId)
                .collection("scenarios")
                .document(scenario.id)
                .set(map)
                .await()
        } catch (_: Throwable) {
        }
    }

    override suspend fun deleteScenario(scenarioId: String) = withContext(NonCancellable) {
        _scenarios.value = _scenarios.value.filter { it.id != scenarioId }
        localFallback.deleteScenario(scenarioId)

        val firestore = db ?: return@withContext
        if (scenarioId.isBlank()) return@withContext
        val wsId = workspaceContext?.activeWorkspaceId?.value?.takeIf {
            it.isNotBlank() && it != "ws_default"
        } ?: "ws_default"

        try {
            firestore.collection("scenarios")
                .document(scenarioId)
                .delete()
                .await()
        } catch (_: Throwable) {
        }

        try {
            firestore.collection("workspaces")
                .document(wsId)
                .collection("scenarios")
                .document(scenarioId)
                .delete()
                .await()
        } catch (_: Throwable) {
        }
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
