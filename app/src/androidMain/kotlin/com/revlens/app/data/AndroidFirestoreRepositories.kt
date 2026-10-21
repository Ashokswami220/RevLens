package com.revlens.app.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.revlens.app.data.fake.FakeTeamRepository
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.BusinessModel
import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.TeamInvite
import com.revlens.app.domain.model.TeamInviteDetail
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.repository.MembershipCheckResult
import com.revlens.app.domain.repository.TeamRepository
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
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

class AndroidFirestoreWorkspaceRepository(
    private val localFallback: WorkspaceRepository = DefaultWorkspaceRepository()
) : WorkspaceRepository {

    private val db: FirebaseFirestore? get() = safeFirestore
    private val auth: FirebaseAuth? get() = safeAuth
    private val _workspaces = MutableStateFlow(
        listOf(Workspace(id = "ws_default", name = "My Workspace", currency = "USD"))
    )
    private var hasRemoteWorkspaces = false

    override val workspacesFlow: Flow<List<Workspace>> = callbackFlow {
        val firestore = db
        val fbAuth = auth
        var firestoreListener: ListenerRegistration? = null

        trySend(_workspaces.value)

        fun attachListener(uid: String) {
            if (uid.isBlank() || firestore == null) return
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
                                val name = doc.getString("name") ?: "My Workspace"
                                val currency = doc.getString("currency") ?: "USD"
                                val timeZoneId = doc.getString("timeZoneId") ?: "UTC"
                                val fiscalYearStartMonth = doc.getLong("fiscalYearStartMonth")
                                    ?.toInt() ?: 1
                                val bmStr = doc.getString("businessModel") ?: "B2bSaas"
                                val bm = try {
                                    BusinessModel.valueOf(bmStr)
                                } catch (_: Throwable) {
                                    BusinessModel.B2bSaas
                                }
                                val createdAtMillis =
                                    doc.getLong("createdAtMillis") ?: System.currentTimeMillis()
                                Workspace(
                                    id = id,
                                    name = name,
                                    currency = currency,
                                    timeZoneId = timeZoneId,
                                    fiscalYearStartMonth = fiscalYearStartMonth,
                                    businessModel = bm,
                                    createdAtMillis = createdAtMillis
                                )
                            }
                            if (list.isNotEmpty()) {
                                hasRemoteWorkspaces = true
                                // Enforce single active workspace per user
                                val singleList =
                                    listOf(list.maxByOrNull { it.createdAtMillis } ?: list.first())
                                _workspaces.value = singleList
                                trySend(singleList)
                            } else {
                                trySend(_workspaces.value)
                            }
                        }
                    }
            } catch (e: Throwable) {
                Log.e("RevLensFirestore", "Workspace snapshot attach error", e)
            }
        }

        val authStateListener = if (fbAuth != null) {
            FirebaseAuth.AuthStateListener { fAuth ->
                val uid = fAuth.currentUser?.uid
                if (!uid.isNullOrBlank()) attachListener(uid) else firestoreListener?.remove()
            }
        } else null

        if (fbAuth != null && authStateListener != null) {
            try {
                fbAuth.addAuthStateListener(authStateListener)
                val initialUid = fbAuth.currentUser?.uid
                if (!initialUid.isNullOrBlank()) attachListener(initialUid)
            } catch (e: Throwable) {
                Log.e("RevLensFirestore", "Workspace auth listener attach error", e)
            }
        }

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.workspacesFlow.collect { localList ->
                if (!hasRemoteWorkspaces) {
                    if (localList.isNotEmpty()) {
                        _workspaces.value = listOf(localList.last())
                        trySend(_workspaces.value)
                    } else {
                        trySend(_workspaces.value)
                    }
                }
            }
        }

        awaitClose {
            if (fbAuth != null && authStateListener != null) {
                try {
                    fbAuth.removeAuthStateListener(authStateListener)
                } catch (_: Throwable) {
                }
            }
            firestoreListener?.remove()
            localJob.cancel()
        }
    }

    override suspend fun getWorkspace(id: String): Workspace? {
        val inMem = _workspaces.value.firstOrNull { it.id == id }
        if (inMem != null) return inMem
        val firestore = db ?: return localFallback.getWorkspace(id)
        if (id.isBlank()) return localFallback.getWorkspace(id)

        try {
            val uid = auth?.currentUser?.uid
            val snapshot = if (!uid.isNullOrBlank()) {
                val userWs = firestore.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .document(id)
                    .get()
                    .await()
                if (userWs.exists()) userWs else firestore.collection("workspaces")
                    .document(id)
                    .get()
                    .await()
            } else {
                firestore.collection("workspaces")
                    .document(id)
                    .get()
                    .await()
            }
            if (snapshot.exists()) {
                val name = snapshot.getString("name") ?: "My Workspace"
                val currency = snapshot.getString("currency") ?: "USD"
                val timeZoneId = snapshot.getString("timeZoneId") ?: "UTC"
                val fiscalYearStartMonth = snapshot.getLong("fiscalYearStartMonth")
                    ?.toInt() ?: 1
                val bmStr = snapshot.getString("businessModel") ?: "B2bSaas"
                val bm = try {
                    BusinessModel.valueOf(bmStr)
                } catch (_: Throwable) {
                    BusinessModel.B2bSaas
                }
                val createdAtMillis =
                    snapshot.getLong("createdAtMillis") ?: System.currentTimeMillis()
                return Workspace(
                    id = id,
                    name = name,
                    currency = currency,
                    timeZoneId = timeZoneId,
                    fiscalYearStartMonth = fiscalYearStartMonth,
                    businessModel = bm,
                    createdAtMillis = createdAtMillis
                )
            }
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "getWorkspace error", e)
        }
        return localFallback.getWorkspace(id)
    }

    override suspend fun createWorkspace(workspace: Workspace) {
        // Single workspace rule: replace any existing workspace locally
        val previousIds = _workspaces.value.map { it.id }
            .filter { it != workspace.id }
        _workspaces.value = listOf(workspace)
        previousIds.forEach { oldId -> localFallback.deleteWorkspace(oldId) }
        localFallback.createWorkspace(workspace)

        val firestore = db ?: return
        if (workspace.id.isBlank()) return
        val uid = auth?.currentUser?.uid
        val joinCode = "RL-" + workspace.id.removePrefix("ws_")
            .uppercase()
            .take(6)
            .padEnd(6, '0')
        val map = mutableMapOf<String, Any>(
            "id" to workspace.id,
            "name" to workspace.name,
            "currency" to workspace.currency,
            "timeZoneId" to workspace.timeZoneId,
            "fiscalYearStartMonth" to workspace.fiscalYearStartMonth,
            "businessModel" to workspace.businessModel.name,
            "joinCode" to joinCode,
            "createdAtMillis" to workspace.createdAtMillis,
            "updatedAt" to System.currentTimeMillis()
        )
        if (!uid.isNullOrBlank()) {
            map["userId"] = uid
            try {
                // Remove old user-scoped workspaces so user only has 1 workspace in Firestore
                val existingDocs = firestore.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .get()
                    .await()
                for (doc in existingDocs.documents) {
                    if (doc.id != workspace.id) {
                        doc.reference.delete()
                            .await()
                    }
                }
                firestore.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .document(workspace.id)
                    .set(map, SetOptions.merge())
                    .await()
            } catch (e: Throwable) {
                Log.e("RevLensFirestore", "Failed to save user-scoped workspace", e)
            }
        }
        try {
            firestore.collection("workspaces")
                .document(workspace.id)
                .set(map, SetOptions.merge())
                .await()

            // Ensure creator is registered as Owner in workspaces/{wsId}/members unless they already have a role
            val authUser = auth?.currentUser
            if (authUser != null) {
                val memberRef = firestore.collection("workspaces")
                    .document(workspace.id)
                    .collection("members")
                    .document(authUser.uid)
                val existingMember = memberRef.get()
                    .await()
                if (!existingMember.exists()) {
                    val ownerMap = mapOf(
                        "id" to authUser.uid,
                        "name" to (authUser.displayName?.ifBlank { null }
                            ?: authUser.email?.substringBefore("@") ?: "Owner"),
                        "email" to (authUser.email?.trim()
                            ?.lowercase() ?: ""),
                        "role" to Role.Owner.name,
                        "joinedAt" to "Workspace Owner",
                        "avatarUrl" to authUser.photoUrl?.toString()
                    )
                    memberRef.set(ownerMap, SetOptions.merge())
                        .await()
                }
            }
        } catch (_: Throwable) {
        }
    }

    override suspend fun updateWorkspace(workspace: Workspace) {
        createWorkspace(workspace)
    }

    override suspend fun deleteWorkspace(id: String) {
        _workspaces.value = _workspaces.value.filter { it.id != id }
        localFallback.deleteWorkspace(id)
        val firestore = db ?: return
        if (id.isBlank()) return
        try {
            val uid = auth?.currentUser?.uid
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
}

class AndroidFirestoreProfileRepository(
    private val localFallback: ProfileRepository,
    private val workspaceContext: WorkspaceContext? = null
) : ProfileRepository {

    private val db: FirebaseFirestore? get() = safeFirestore
    private val auth: FirebaseAuth? get() = safeAuth
    private val json = Json { ignoreUnknownKeys = true }
    private val _profile = MutableStateFlow<BusinessProfile?>(null)

    override val profileFlow: Flow<BusinessProfile?> = callbackFlow {
        val firestore = db
        val fbAuth = auth
        var wsProfileListener: ListenerRegistration? = null
        var userProfileListener: ListenerRegistration? = null

        trySend(_profile.value)

        fun attachListeners(uid: String?, wsId: String?) {
            if (firestore == null) return
            wsProfileListener?.remove()
            userProfileListener?.remove()

            val effectiveWsId = wsId?.takeIf { it.isNotBlank() && it != "ws_default" }
            if (effectiveWsId != null) {
                // Primary source of truth: Shared Workspace Profile (/workspaces/{wsId}/profile/current)
                try {
                    wsProfileListener = firestore.collection("workspaces")
                        .document(effectiveWsId)
                        .collection("profile")
                        .document("current")
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) return@addSnapshotListener
                            if (snapshot != null && snapshot.exists()) {
                                val payload = snapshot.getString("payload")
                                if (payload != null) {
                                    try {
                                        val profile =
                                            json.decodeFromString<BusinessProfile>(payload)
                                        _profile.value = profile
                                        CoroutineScope(Dispatchers.IO).launch {
                                            localFallback.saveProfile(profile)
                                        }
                                        trySend(profile)
                                    } catch (_: Throwable) {
                                    }
                                }
                            }
                        }
                } catch (_: Throwable) {
                }
            } else if (!uid.isNullOrBlank()) {
                try {
                    userProfileListener = firestore.collection("users")
                        .document(uid)
                        .collection("profile")
                        .document("current")
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) return@addSnapshotListener
                            if (snapshot != null && snapshot.exists()) {
                                val payload = snapshot.getString("payload")
                                if (payload != null) {
                                    try {
                                        val profile =
                                            json.decodeFromString<BusinessProfile>(payload)
                                        _profile.value = profile
                                        CoroutineScope(Dispatchers.IO).launch {
                                            localFallback.saveProfile(profile)
                                        }
                                        trySend(profile)
                                    } catch (_: Throwable) {
                                    }
                                }
                            }
                        }
                } catch (_: Throwable) {
                }
            }
        }

        val wsJob = CoroutineScope(Dispatchers.IO).launch {
            if (workspaceContext != null) {
                workspaceContext.activeWorkspaceId.collectLatest { wsId ->
                    attachListeners(fbAuth?.currentUser?.uid, wsId)
                }
            } else {
                attachListeners(fbAuth?.currentUser?.uid, null)
            }
        }

        val authStateListener = if (fbAuth != null) {
            FirebaseAuth.AuthStateListener { fAuth ->
                attachListeners(fAuth.currentUser?.uid, workspaceContext?.activeWorkspaceId?.value)
            }
        } else null

        if (fbAuth != null && authStateListener != null) {
            try {
                fbAuth.addAuthStateListener(authStateListener)
            } catch (_: Throwable) {
            }
        }

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.profileFlow.collect { localProf ->
                if (localProf != null || _profile.value == null) {
                    _profile.value = localProf
                    trySend(localProf)
                }
            }
        }

        awaitClose {
            if (fbAuth != null && authStateListener != null) {
                try {
                    fbAuth.removeAuthStateListener(authStateListener)
                } catch (_: Throwable) {
                }
            }
            wsProfileListener?.remove()
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

    override suspend fun saveProfile(profile: BusinessProfile) {
        _profile.value = profile
        localFallback.saveProfile(profile)
        val firestore = db ?: return
        val uid = auth?.currentUser?.uid
        val rawWsId = workspaceContext?.activeWorkspaceId?.value
        val wsId = if (rawWsId.isNullOrBlank()) "ws_default" else rawWsId
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
            profile.mrrGoal?.let {
                map["mrrGoal"] = it.toDouble()
            }

            // Always save to the shared workspace profile document so all team members see the exact same profile
            try {
                firestore.collection("workspaces")
                    .document(wsId)
                    .collection("profile")
                    .document("current")
                    .set(map)
                    .await()
                firestore.collection("profiles")
                    .document(wsId)
                    .set(map)
                    .await()
            } catch (_: Throwable) {
            }

            if (!uid.isNullOrBlank()) {
                map["userId"] = uid
                try {
                    firestore.collection("users")
                        .document(uid)
                        .collection("profile")
                        .document("current")
                        .set(map)
                        .await()
                } catch (_: Throwable) {
                }

                try {
                    firestore.collection("users")
                        .document(uid)
                        .set(
                            mapOf(
                                "id" to uid,
                                "activeWorkspaceId" to wsId,
                                "onboardingCompleted" to true,
                                "updatedAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        )
                        .await()
                } catch (_: Throwable) {
                }

                try {
                    firestore.collection("profiles")
                        .document(uid)
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
        val firestore = db ?: return null
        return try {
            val uid = auth?.currentUser?.uid
            val rawWsId = workspaceContext?.activeWorkspaceId?.value
            val wsId = if (rawWsId.isNullOrBlank()) "ws_default" else rawWsId

            // Priority 1: Check shared workspace profile FIRST so invited/joined members load the team's profile
            var snapshot = if (wsId != "ws_default") {
                withTimeoutOrNull(2500L.milliseconds) {
                    try {
                        val doc = firestore.collection("workspaces")
                            .document(wsId)
                            .collection("profile")
                            .document("current")
                            .get()
                            .await()
                        if (doc.exists()) doc else {
                            firestore.collection("profiles")
                                .document(wsId)
                                .get()
                                .await()
                        }
                    } catch (_: Throwable) {
                        null
                    }
                }
            } else null

            // Priority 2: Check user's profile doc
            if ((snapshot == null || !snapshot.exists()) && !uid.isNullOrBlank()) {
                snapshot = withTimeoutOrNull(2500L.milliseconds) {
                    try {
                        val userDoc = firestore.collection("users")
                            .document(uid)
                            .collection("profile")
                            .document("current")
                            .get()
                            .await()
                        if (userDoc.exists()) userDoc else {
                            firestore.collection("profiles")
                                .document(uid)
                                .get()
                                .await()
                        }
                    } catch (_: Throwable) {
                        null
                    }
                }
            }

            if (snapshot != null && snapshot.exists()) {
                val payload = snapshot.getString("payload")
                if (payload != null) {
                    val profile = json.decodeFromString<BusinessProfile>(payload)
                    _profile.value = profile
                    localFallback.saveProfile(profile)
                    localFallback.setOnboardingCompleted(true)
                    profile
                } else null
            } else null
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "syncRemoteProfile failed", e)
            null
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        localFallback.setOnboardingCompleted(completed)
        val firestore = db ?: return
        try {
            val uid = auth?.currentUser?.uid
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

    private val db: FirebaseFirestore? get() = safeFirestore
    private val auth: FirebaseAuth? get() = safeAuth
    private val json = Json { ignoreUnknownKeys = true }
    private val _scenarios = MutableStateFlow<List<Scenario>>(emptyList())

    override val scenariosFlow: Flow<List<Scenario>> = callbackFlow {
        val firestore = db
        val fbAuth = auth
        var firestoreListener: ListenerRegistration? = null

        trySend(_scenarios.value)

        fun attachListener(uid: String?, wsId: String?) {
            if (firestore == null) return
            firestoreListener?.remove()
            val effectiveWsId = wsId?.takeIf { it.isNotBlank() && it != "ws_default" }
            try {
                val targetCollection = if (effectiveWsId != null) {
                    firestore.collection("workspaces")
                        .document(effectiveWsId)
                        .collection("scenarios")
                } else if (!uid.isNullOrBlank()) {
                    firestore.collection("users")
                        .document(uid)
                        .collection("scenarios")
                } else null

                if (targetCollection != null) {
                    firestoreListener = targetCollection.addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                val payload = doc.getString("payload")
                                if (payload != null) {
                                    try {
                                        json.decodeFromString<Scenario>(payload)
                                    } catch (_: Throwable) {
                                        null
                                    }
                                } else null
                            }
                            _scenarios.value = list
                            trySend(list)
                        }
                    }
                }
            } catch (_: Throwable) {
            }
        }

        val wsJob = CoroutineScope(Dispatchers.IO).launch {
            if (workspaceContext != null) {
                workspaceContext.activeWorkspaceId.collectLatest { wsId ->
                    attachListener(fbAuth?.currentUser?.uid, wsId)
                }
            } else {
                attachListener(fbAuth?.currentUser?.uid, null)
            }
        }

        val authStateListener = if (fbAuth != null) {
            FirebaseAuth.AuthStateListener { fAuth ->
                attachListener(fAuth.currentUser?.uid, workspaceContext?.activeWorkspaceId?.value)
            }
        } else null

        if (fbAuth != null && authStateListener != null) {
            try {
                fbAuth.addAuthStateListener(authStateListener)
            } catch (_: Throwable) {
            }
        }

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.scenariosFlow.collect { localList ->
                if (_scenarios.value.isEmpty()) {
                    _scenarios.value = localList
                    trySend(localList)
                }
            }
        }

        awaitClose {
            if (fbAuth != null && authStateListener != null) {
                try {
                    fbAuth.removeAuthStateListener(authStateListener)
                } catch (_: Throwable) {
                }
            }
            firestoreListener?.remove()
            wsJob.cancel()
            localJob.cancel()
        }
    }

    override suspend fun saveScenario(scenario: Scenario) {
        val current = _scenarios.value.toMutableList()
        val index = current.indexOfFirst { it.id == scenario.id }
        if (index != -1) {
            current[index] = scenario
        } else {
            current.add(scenario)
        }
        _scenarios.value = current
        localFallback.saveScenario(scenario)

        val firestore = db ?: return
        if (scenario.id.isBlank()) return
        val uid = auth?.currentUser?.uid
        val wsId =
            workspaceContext?.activeWorkspaceId?.value?.takeIf { it.isNotBlank() } ?: "ws_default"
        val serialized = json.encodeToString(Scenario.serializer(), scenario)
        val map = mutableMapOf<String, Any>(
            "id" to scenario.id,
            "name" to scenario.name,
            "workspaceId" to wsId,
            "payload" to serialized,
            "updatedAt" to System.currentTimeMillis()
        )
        try {
            firestore.collection("workspaces")
                .document(wsId)
                .collection("scenarios")
                .document(scenario.id)
                .set(map)
                .await()
        } catch (_: Throwable) {
        }

        if (!uid.isNullOrBlank()) {
            map["userId"] = uid
            try {
                firestore.collection("users")
                    .document(uid)
                    .collection("scenarios")
                    .document(scenario.id)
                    .set(map)
                    .await()
            } catch (_: Throwable) {
            }
        }
    }

    override suspend fun deleteScenario(scenarioId: String) {
        _scenarios.value = _scenarios.value.filter { it.id != scenarioId }
        localFallback.deleteScenario(scenarioId)
        val firestore = db ?: return
        if (scenarioId.isBlank()) return
        val wsId =
            workspaceContext?.activeWorkspaceId?.value?.takeIf { it.isNotBlank() } ?: "ws_default"
        try {
            firestore.collection("workspaces")
                .document(wsId)
                .collection("scenarios")
                .document(scenarioId)
                .delete()
                .await()
        } catch (_: Throwable) {
        }
        try {
            val uid = auth?.currentUser?.uid
            if (!uid.isNullOrBlank()) {
                firestore.collection("users")
                    .document(uid)
                    .collection("scenarios")
                    .document(scenarioId)
                    .delete()
                    .await()
            }
        } catch (_: Throwable) {
        }
    }
}

class AndroidFirestoreTeamRepository(
    private val localFallback: FakeTeamRepository = FakeTeamRepository(),
    private val profileRepository: ProfileRepository? = null
) : TeamRepository {

    private val db: FirebaseFirestore? get() = safeFirestore
    private val auth: FirebaseAuth? get() = safeAuth
    private val json = Json { ignoreUnknownKeys = true }

    override fun getMembers(workspaceId: String): Flow<List<Member>> = callbackFlow {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        var listener: ListenerRegistration? = null
        var hasRemoteEmission = false

        fun attach(wsId: String) {
            val firestore = db ?: return
            if (wsId.isBlank()) return
            listener?.remove()
            try {
                listener = firestore.collection("workspaces")
                    .document(wsId)
                    .collection("members")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("RevLensFirestore", "Team members snapshot error", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val members = snapshot.documents.mapNotNull { doc ->
                                val id = doc.getString("id") ?: doc.id
                                val name = doc.getString("name") ?: "Member"
                                val email = doc.getString("email") ?: ""
                                val roleStr = doc.getString("role") ?: "Viewer"
                                val role = try {
                                    Role.valueOf(roleStr)
                                } catch (_: Throwable) {
                                    Role.Viewer
                                }
                                val joinedAt = doc.getString("joinedAt") ?: "Member"
                                val avatarUrl = doc.getString("avatarUrl")
                                Member(
                                    id = id, name = name, email = email, role = role,
                                    avatarUrl = avatarUrl, joinedAt = joinedAt
                                )
                            }
                            if (members.isNotEmpty() || hasRemoteEmission) {
                                hasRemoteEmission = true
                                trySend(members)
                            }
                        }
                    }
            } catch (e: Throwable) {
                Log.e("RevLensFirestore", "Team members attach error", e)
            }
        }

        attach(cleanWsId)

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.getMembers(cleanWsId)
                .collect { localList ->
                    if (!hasRemoteEmission) {
                        trySend(localList)
                    }
                }
        }

        awaitClose {
            listener?.remove()
            localJob.cancel()
        }
    }

    override fun getInvites(workspaceId: String): Flow<List<TeamInvite>> = callbackFlow {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        var listener: ListenerRegistration? = null
        var hasRemoteInvites = false

        fun attach(wsId: String) {
            val firestore = db ?: return
            if (wsId.isBlank()) return
            listener?.remove()
            try {
                listener = firestore.collection("workspaces")
                    .document(wsId)
                    .collection("invites")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            hasRemoteInvites = true
                            val invites = snapshot.documents.mapNotNull { doc ->
                                val id = doc.getString("id") ?: doc.id
                                val email = doc.getString("email") ?: return@mapNotNull null
                                val roleStr = doc.getString("role") ?: "Viewer"
                                val role = try {
                                    Role.valueOf(roleStr)
                                } catch (_: Throwable) {
                                    Role.Viewer
                                }
                                val sentAt = doc.getString("sentAt") ?: "Just now"
                                val invitedBy = doc.getString("invitedBy") ?: "Admin"
                                TeamInvite(
                                    id = id, email = email, role = role, sentAt = sentAt,
                                    invitedBy = invitedBy
                                )
                            }
                            trySend(invites)
                        }
                    }
            } catch (_: Throwable) {
            }
        }

        attach(cleanWsId)

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.getInvites(cleanWsId)
                .collect { localList ->
                    if (!hasRemoteInvites) {
                        trySend(localList)
                    }
                }
        }

        awaitClose {
            listener?.remove()
            localJob.cancel()
        }
    }

    override suspend fun inviteMember(
        workspaceId: String, email: String, role: Role, inviterName: String
    ): Result<TeamInvite> {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        val fallbackResult = localFallback.inviteMember(cleanWsId, email, role, inviterName)
        if (fallbackResult.isFailure) return fallbackResult

        val invite = fallbackResult.getOrThrow()
        val firestore = db ?: return Result.success(invite)
        try {
            val map = mapOf(
                "id" to invite.id,
                "email" to invite.email.trim()
                    .lowercase(),
                "role" to invite.role.name,
                "sentAt" to invite.sentAt,
                "invitedBy" to invite.invitedBy,
                "workspaceId" to cleanWsId,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("workspaces")
                .document(cleanWsId)
                .collection("invites")
                .document(invite.id)
                .set(map)
                .await()

            firestore.collection("team_invites")
                .document(invite.id)
                .set(map)
                .await()

            // Ensure currentworkspace joinCode is stored on workspace document
            val joinCode = "RL-" + cleanWsId.removePrefix("ws_")
                .uppercase()
                .take(6)
                .padEnd(6, '0')
            firestore.collection("workspaces")
                .document(cleanWsId)
                .set(mapOf("joinCode" to joinCode), SetOptions.merge())
                .await()

            val authUser = auth?.currentUser
            if (authUser != null) {
                val memberRef = firestore.collection("workspaces")
                    .document(cleanWsId)
                    .collection("members")
                    .document(authUser.uid)
                val snap = memberRef.get()
                    .await()
                if (!snap.exists()) {
                    val ownerMap = mapOf(
                        "id" to authUser.uid,
                        "name" to (authUser.displayName ?: inviterName),
                        "email" to (authUser.email?.trim()
                            ?.lowercase() ?: ""),
                        "role" to Role.Owner.name,
                        "joinedAt" to "Workspace Founder",
                        "avatarUrl" to authUser.photoUrl?.toString()
                    )
                    memberRef.set(ownerMap, SetOptions.merge())
                        .await()
                }
            }
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "inviteMember firestore error", e)
        }
        return Result.success(invite)
    }

    override suspend fun updateMemberRole(
        workspaceId: String, memberId: String, newRole: Role
    ): Result<Unit> {
        if (newRole == Role.Owner) {
            return Result.failure(
                IllegalArgumentException("Use Transfer Ownership to assign an Owner.")
            )
        }
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        localFallback.updateMemberRole(cleanWsId, memberId, newRole)
        val firestore = db ?: return Result.success(Unit)
        if (cleanWsId.isBlank() || memberId.isBlank()) return Result.success(Unit)
        try {
            firestore.collection("workspaces")
                .document(cleanWsId)
                .collection("members")
                .document(memberId)
                .update("role", newRole.name)
                .await()
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "updateMemberRole error", e)
        }
        return Result.success(Unit)
    }

    override suspend fun removeMember(workspaceId: String, memberId: String): Result<Unit> {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        val currentUid = auth?.currentUser?.uid.orEmpty()
        if (currentUid.isNotBlank() && memberId == currentUid) {
            return Result.failure(
                IllegalArgumentException("You cannot remove yourself from the workspace.")
            )
        }
        localFallback.removeMember(cleanWsId, memberId)
        val firestore = db ?: return Result.success(Unit)
        if (cleanWsId.isBlank() || memberId.isBlank()) return Result.success(Unit)
        try {
            val memberDoc = firestore.collection("workspaces")
                .document(cleanWsId)
                .collection("members")
                .document(memberId)
                .get()
                .await()

            if (memberDoc.exists() && memberDoc.getString("role") == Role.Owner.name) {
                return Result.failure(
                    IllegalArgumentException("The Workspace Owner cannot be removed.")
                )
            }

            val targetEmail = memberDoc.getString("email")
                ?.trim()
                ?.lowercase()

            // Delete by document ID
            firestore.collection("workspaces")
                .document(cleanWsId)
                .collection("members")
                .document(memberId)
                .delete()
                .await()

            // Also delete any duplicate member docs matching email
            if (!targetEmail.isNullOrBlank()) {
                val emailMatches = firestore.collection("workspaces")
                    .document(cleanWsId)
                    .collection("members")
                    .whereEqualTo("email", targetEmail)
                    .get()
                    .await()
                for (doc in emailMatches.documents) {
                    doc.reference.delete()
                        .await()
                }
            }

            // Remove the workspace from the removed user's /users/{memberId}/workspaces subcollection
            try {
                firestore.collection("users")
                    .document(memberId)
                    .collection("workspaces")
                    .document(cleanWsId)
                    .delete()
                    .await()
            } catch (_: Throwable) {
            }
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "removeMember error", e)
        }
        return Result.success(Unit)
    }

    override suspend fun transferOwnership(
        workspaceId: String,
        newOwnerMemberId: String
    ): Result<Unit> {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        localFallback.transferOwnership(cleanWsId, newOwnerMemberId)
        val firestore = db ?: return Result.success(Unit)
        try {
            val membersCol = firestore.collection("workspaces")
                .document(cleanWsId)
                .collection("members")
            val snap = membersCol.get()
                .await()
            for (doc in snap.documents) {
                val docId = doc.getString("id") ?: doc.id
                val roleStr = doc.getString("role") ?: "Viewer"
                if (docId == newOwnerMemberId || doc.id == newOwnerMemberId) {
                    doc.reference.update("role", Role.Owner.name)
                        .await()
                } else if (roleStr == Role.Owner.name) {
                    doc.reference.update("role", Role.Admin.name)
                        .await()
                }
            }
        } catch (e: Throwable) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun revokeInvite(workspaceId: String, inviteId: String): Result<Unit> {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        localFallback.revokeInvite(cleanWsId, inviteId)
        val firestore = db ?: return Result.success(Unit)
        if (cleanWsId.isBlank() || inviteId.isBlank()) return Result.success(Unit)
        try {
            firestore.collection("workspaces")
                .document(cleanWsId)
                .collection("invites")
                .document(inviteId)
                .delete()
                .await()
            firestore.collection("team_invites")
                .document(inviteId)
                .delete()
                .await()
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "revokeInvite error", e)
        }
        return Result.success(Unit)
    }

    override suspend fun resendInvite(workspaceId: String, inviteId: String): Result<Unit> {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        localFallback.resendInvite(cleanWsId, inviteId)
        val firestore = db ?: return Result.success(Unit)
        if (cleanWsId.isBlank() || inviteId.isBlank()) return Result.success(Unit)
        try {
            firestore.collection("workspaces")
                .document(cleanWsId)
                .collection("invites")
                .document(inviteId)
                .update("sentAt", "Resent just now")
                .await()
            firestore.collection("team_invites")
                .document(inviteId)
                .update("sentAt", "Resent just now")
                .await()
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "resendInvite error", e)
        }
        return Result.success(Unit)
    }

    override suspend fun getPendingInvitesForEmail(email: String): List<TeamInviteDetail> {
        val normalized = email.trim()
            .lowercase()
        val results = mutableListOf<TeamInviteDetail>()
        val firestore = db
        if (firestore != null && normalized.isNotBlank()) {
            try {
                val query = firestore.collection("team_invites")
                    .whereEqualTo("email", normalized)
                    .get()
                    .await()
                for (doc in query.documents) {
                    val id = doc.getString("id") ?: doc.id
                    val wsId = doc.getString("workspaceId")
                        ?.ifBlank { "ws_default" } ?: "ws_default"
                    val roleStr = doc.getString("role") ?: "Viewer"
                    val role = try {
                        Role.valueOf(roleStr)
                    } catch (_: Throwable) {
                        Role.Viewer
                    }
                    val sentAt = doc.getString("sentAt") ?: "Just now"
                    val invitedBy = doc.getString("invitedBy") ?: "Admin"
                    val wsDoc = try {
                        firestore.collection("workspaces")
                            .document(wsId)
                            .get()
                            .await()
                    } catch (_: Throwable) {
                        null
                    }
                    val wsName = wsDoc?.getString("name") ?: "RevLens Workspace"

                    results.add(
                        TeamInviteDetail(
                            inviteId = id,
                            workspaceId = wsId,
                            workspaceName = wsName,
                            invitedEmail = normalized,
                            role = role,
                            invitedBy = invitedBy,
                            sentAt = sentAt
                        )
                    )
                }
            } catch (e: Throwable) {
                Log.e("RevLensFirestore", "getPendingInvitesForEmail error", e)
            }
        }

        val localList = localFallback.getPendingInvitesForEmail(email)
        localList.forEach { local ->
            if (results.none { it.inviteId == local.inviteId }) {
                results.add(local)
            }
        }
        return results
    }

    override suspend fun acceptInvite(inviteId: String, user: UserAccount): Result<String> {
        val fallbackResult = localFallback.acceptInvite(inviteId, user)
        val firestore = db
        val targetWsId = fallbackResult.getOrNull() ?: run {
            if (firestore != null) {
                try {
                    val doc = firestore.collection("team_invites")
                        .document(inviteId)
                        .get()
                        .await()
                    doc.getString("workspaceId")
                        ?.ifBlank { "ws_default" }
                } catch (_: Throwable) {
                    null
                }
            } else null
        } ?: return Result.failure(NoSuchElementException("Invitation not found."))

        if (firestore != null) {
            try {
                val inviteDoc = try {
                    firestore.collection("team_invites")
                        .document(inviteId)
                        .get()
                        .await()
                } catch (_: Throwable) {
                    null
                }

                val roleStr = inviteDoc?.getString("role") ?: "Viewer"
                val role = try {
                    Role.valueOf(roleStr)
                } catch (_: Throwable) {
                    Role.Viewer
                }

                linkUserToSharedWorkspace(firestore, targetWsId, user, role, inviteId)
            } catch (e: Throwable) {
                Log.e("RevLensFirestore", "acceptInvite firestore error", e)
            }
        }

        return Result.success(targetWsId)
    }

    override suspend fun getWorkspaceJoinCode(workspaceId: String): String {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        val defaultCode = "RL-" + cleanWsId.removePrefix("ws_")
            .uppercase()
            .take(6)
            .padEnd(6, '0')
        val firestore = db ?: return defaultCode
        return try {
            val doc = firestore.collection("workspaces")
                .document(cleanWsId)
                .get()
                .await()
            val savedCode = doc.getString("joinCode")
            if (!savedCode.isNullOrBlank()) {
                savedCode
            } else {
                firestore.collection("workspaces")
                    .document(cleanWsId)
                    .set(mapOf("joinCode" to defaultCode), SetOptions.merge())
                    .await()
                defaultCode
            }
        } catch (_: Throwable) {
            defaultCode
        }
    }

    override suspend fun joinWorkspaceByCode(code: String, user: UserAccount): Result<String> {
        val trimmed = code.trim()
        if (trimmed.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Please enter a valid team invite code.")
            )
        }
        val firestore = db ?: return localFallback.joinWorkspaceByCode(trimmed, user)

        return try {
            // 1. Check if code matches a direct inviteId in team_invites
            val directInvite = try {
                firestore.collection("team_invites")
                    .document(trimmed)
                    .get()
                    .await()
            } catch (_: Throwable) {
                null
            }
            if (directInvite != null && directInvite.exists()) {
                return acceptInvite(directInvite.id, user)
            }

            // 2. Search workspaces by joinCode or workspaceId
            var targetWsId: String? = null
            val byJoinCode = firestore.collection("workspaces")
                .whereEqualTo("joinCode", trimmed.uppercase())
                .get()
                .await()

            if (!byJoinCode.isEmpty) {
                targetWsId = byJoinCode.documents.first().id
            } else {
                val allWs = firestore.collection("workspaces")
                    .get()
                    .await()
                for (doc in allWs.documents) {
                    val wsId = doc.id
                    val derivedCode = "RL-" + wsId.removePrefix("ws_")
                        .uppercase()
                        .take(6)
                        .padEnd(6, '0')
                    if (wsId.equals(trimmed, ignoreCase = true) || derivedCode.equals(
                            trimmed, ignoreCase = true
                        )
                    ) {
                        targetWsId = wsId
                        break
                    }
                }
            }

            val resolvedWsId = targetWsId
                ?: return localFallback.joinWorkspaceByCode(trimmed, user)

            // Check if this user had a pending invite for this workspace to honor their invited role
            var assignedRole = Role.Viewer
            var matchedInviteId: String? = null
            val cleanEmail = user.email.trim()
                .lowercase()
            if (cleanEmail.isNotBlank()) {
                val inviteQuery = firestore.collection("workspaces")
                    .document(resolvedWsId)
                    .collection("invites")
                    .whereEqualTo("email", cleanEmail)
                    .get()
                    .await()
                if (!inviteQuery.isEmpty) {
                    val invDoc = inviteQuery.documents.first()
                    matchedInviteId = invDoc.id
                    val rStr = invDoc.getString("role") ?: "Viewer"
                    assignedRole = try {
                        Role.valueOf(rStr)
                    } catch (_: Throwable) {
                        Role.Viewer
                    }
                }
            }

            linkUserToSharedWorkspace(firestore, resolvedWsId, user, assignedRole, matchedInviteId)
            localFallback.joinWorkspaceByCode(trimmed, user)
            Result.success(resolvedWsId)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private suspend fun linkUserToSharedWorkspace(
        firestore: FirebaseFirestore,
        targetWsId: String,
        user: UserAccount,
        role: Role,
        consumedInviteId: String?
    ) {
        val memberDocId = user.id.ifBlank { "mem_${user.email.hashCode()}" }
        val memberMap = mapOf(
            "id" to memberDocId,
            "name" to user.name.ifBlank { user.email.substringBefore("@") },
            "email" to user.email.trim()
                .lowercase(),
            "role" to role.name,
            "joinedAt" to "Joined team",
            "avatarUrl" to user.avatarUrl
        )

        firestore.collection("workspaces")
            .document(targetWsId)
            .collection("members")
            .document(memberDocId)
            .set(memberMap, SetOptions.merge())
            .await()

        if (!consumedInviteId.isNullOrBlank()) {
            try {
                firestore.collection("workspaces")
                    .document(targetWsId)
                    .collection("invites")
                    .document(consumedInviteId)
                    .delete()
                    .await()
                firestore.collection("team_invites")
                    .document(consumedInviteId)
                    .delete()
                    .await()
            } catch (_: Throwable) {
            }
        }

        // Single workspace rule: remove any old workspaces in /users/{uid}/workspaces and set targetWsId
        if (user.id.isNotBlank()) {
            val userWsCol = firestore.collection("users")
                .document(user.id)
                .collection("workspaces")
            val oldDocs = try {
                userWsCol.get()
                    .await()
            } catch (_: Throwable) {
                null
            }
            oldDocs?.documents?.forEach { doc ->
                if (doc.id != targetWsId) {
                    try {
                        doc.reference.delete()
                            .await()
                    } catch (_: Throwable) {
                    }
                }
            }

            val wsDoc = try {
                firestore.collection("workspaces")
                    .document(targetWsId)
                    .get()
                    .await()
            } catch (_: Throwable) {
                null
            }
            val wsData: MutableMap<String, Any?> =
                wsDoc?.data?.toMutableMap() ?: mutableMapOf(
                    "id" to targetWsId,
                    "name" to "Team Workspace",
                    "currency" to "USD"
                )
            wsData["id"] = targetWsId
            wsData["role"] = role.name
            userWsCol.document(targetWsId)
                .set(wsData, SetOptions.merge())
                .await()
        }

        // Pull the shared BusinessProfile from the workspace and save it so this member accesses the SAME profile
        val profDoc = try {
            val doc = firestore.collection("workspaces")
                .document(targetWsId)
                .collection("profile")
                .document("current")
                .get()
                .await()
            if (doc.exists()) doc else firestore.collection("profiles")
                .document(targetWsId)
                .get()
                .await()
        } catch (_: Throwable) {
            null
        }

        if (profDoc != null && profDoc.exists()) {
            val payload = profDoc.getString("payload")
            if (payload != null) {
                try {
                    val profile = json.decodeFromString<BusinessProfile>(payload)
                    profileRepository?.saveProfile(profile)
                    localFallback.saveWorkspaceProfile(targetWsId, profile)
                } catch (_: Throwable) {
                }
            }
        }
    }

    override suspend fun getRemoteWorkspace(workspaceId: String): Workspace? {
        val firestore = db ?: return null
        if (workspaceId.isBlank()) return null
        return try {
            val snapshot = firestore.collection("workspaces")
                .document(workspaceId)
                .get()
                .await()
            if (!snapshot.exists()) return null
            val name = snapshot.getString("name") ?: "Team Workspace"
            val currency = snapshot.getString("currency") ?: "USD"
            val timeZoneId = snapshot.getString("timeZoneId") ?: "UTC"
            val fiscalYearStartMonth = snapshot.getLong("fiscalYearStartMonth")
                ?.toInt() ?: 1
            val bmStr = snapshot.getString("businessModel") ?: "B2bSaas"
            val bm = try {
                BusinessModel.valueOf(bmStr)
            } catch (_: Throwable) {
                BusinessModel.B2bSaas
            }
            val createdAtMillis = snapshot.getLong("createdAtMillis") ?: System.currentTimeMillis()
            Workspace(
                id = workspaceId,
                name = name,
                currency = currency,
                timeZoneId = timeZoneId,
                fiscalYearStartMonth = fiscalYearStartMonth,
                businessModel = bm,
                createdAtMillis = createdAtMillis
            )
        } catch (_: Throwable) {
            null
        }
    }

    override suspend fun checkMembershipStatus(
        workspaceId: String,
        userId: String,
        userEmail: String
    ): MembershipCheckResult {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        if (cleanWsId == "ws_default" || (userId.isBlank() && userEmail.isBlank())) {
            return MembershipCheckResult.UNKNOWN
        }
        val firestore =
            db ?: return localFallback.checkMembershipStatus(cleanWsId, userId, userEmail)
        return try {
            val wsDoc = firestore.collection("workspaces")
                .document(cleanWsId)
                .get()
                .await()
            if (!wsDoc.exists()) return MembershipCheckResult.REVOKED

            val membersSnap = firestore.collection("workspaces")
                .document(cleanWsId)
                .collection("members")
                .get()
                .await()

            if (membersSnap.isEmpty) return MembershipCheckResult.UNKNOWN

            val normalizedEmail = userEmail.trim()
                .lowercase()
            val isPresent = membersSnap.documents.any { doc ->
                val docUserId = doc.getString("id") ?: doc.id
                val docEmail = doc.getString("email")
                    ?.trim()
                    ?.lowercase()
                    .orEmpty()
                (userId.isNotBlank() && (docUserId == userId || doc.id == userId)) ||
                        (normalizedEmail.isNotBlank() && docEmail == normalizedEmail)
            }
            if (isPresent) MembershipCheckResult.ACTIVE else MembershipCheckResult.REVOKED
        } catch (_: Throwable) {
            MembershipCheckResult.UNKNOWN
        }
    }

    override suspend fun declineInvite(inviteId: String, email: String): Result<Unit> {
        localFallback.declineInvite(inviteId, email)
        val firestore = db ?: return Result.success(Unit)
        if (inviteId.isBlank()) return Result.success(Unit)
        try {
            val invDoc = firestore.collection("team_invites")
                .document(inviteId)
                .get()
                .await()
            val wsId = invDoc.getString("workspaceId")
            if (!wsId.isNullOrBlank()) {
                firestore.collection("workspaces")
                    .document(wsId)
                    .collection("invites")
                    .document(inviteId)
                    .delete()
                    .await()
            }
            firestore.collection("team_invites")
                .document(inviteId)
                .delete()
                .await()
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "declineInvite error", e)
        }
        return Result.success(Unit)
    }

    override suspend fun isUserAssociatedWithAnotherBusiness(
        email: String, currentWorkspaceId: String?
    ): Boolean {
        return localFallback.isUserAssociatedWithAnotherBusiness(email, currentWorkspaceId)
    }

    override suspend fun getUserRoleInWorkspace(workspaceId: String, userEmail: String): Role? {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        val normalized = userEmail.trim()
            .lowercase()
        val firestore = db
        if (firestore != null && cleanWsId.isNotBlank() && normalized.isNotBlank()) {
            try {
                val snapshot = firestore.collection("workspaces")
                    .document(cleanWsId)
                    .collection("members")
                    .whereEqualTo("email", normalized)
                    .get()
                    .await()
                if (!snapshot.isEmpty) {
                    val roleStr = snapshot.documents.first()
                        .getString("role")
                    if (roleStr != null) {
                        return try {
                            Role.valueOf(roleStr)
                        } catch (_: Throwable) {
                            Role.Viewer
                        }
                    }
                }
            } catch (_: Throwable) {
            }
        }
        return localFallback.getUserRoleInWorkspace(cleanWsId, userEmail)
    }

    override suspend fun leaveWorkspace(workspaceId: String, userEmail: String): Result<Unit> {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        val res = localFallback.leaveWorkspace(cleanWsId, userEmail)
        if (res.isFailure) return res
        val firestore = db ?: return Result.success(Unit)
        if (cleanWsId.isBlank()) return Result.success(Unit)
        try {
            val normalized = userEmail.trim()
                .lowercase()
            val snapshot = firestore.collection("workspaces")
                .document(cleanWsId)
                .collection("members")
                .whereEqualTo("email", normalized)
                .get()
                .await()
            for (doc in snapshot.documents) {
                doc.reference.delete()
                    .await()
            }
            val uid = auth?.currentUser?.uid
            if (!uid.isNullOrBlank()) {
                firestore.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .document(cleanWsId)
                    .delete()
                    .await()
            }
        } catch (_: Throwable) {
        }
        return Result.success(Unit)
    }

    override suspend fun getWorkspaceProfile(workspaceId: String): BusinessProfile? {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        val firestore = db
        if (firestore != null && cleanWsId.isNotBlank()) {
            try {
                val doc = firestore.collection("workspaces")
                    .document(cleanWsId)
                    .collection("profile")
                    .document("current")
                    .get()
                    .await()
                if (doc.exists()) {
                    val payload = doc.getString("payload")
                    if (payload != null) {
                        return json.decodeFromString<BusinessProfile>(payload)
                    }
                }
                val rootDoc = firestore.collection("profiles")
                    .document(cleanWsId)
                    .get()
                    .await()
                if (rootDoc.exists()) {
                    val payload = rootDoc.getString("payload")
                    if (payload != null) {
                        return json.decodeFromString<BusinessProfile>(payload)
                    }
                }
            } catch (_: Throwable) {
            }
        }
        return localFallback.getWorkspaceProfile(cleanWsId)
    }

    override suspend fun saveWorkspaceProfile(workspaceId: String, profile: BusinessProfile) {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        localFallback.saveWorkspaceProfile(cleanWsId, profile)
        val firestore = db ?: return
        if (cleanWsId.isBlank()) return
        try {
            val serialized = json.encodeToString(BusinessProfile.serializer(), profile)
            val map = mapOf(
                "payload" to serialized,
                "workspaceId" to cleanWsId,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("workspaces")
                .document(cleanWsId)
                .collection("profile")
                .document("current")
                .set(map)
                .await()
            firestore.collection("profiles")
                .document(cleanWsId)
                .set(map)
                .await()
        } catch (e: Throwable) {
            Log.e("RevLensFirestore", "saveWorkspaceProfile error", e)
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
