package com.revlens.app.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.revlens.app.data.fake.FakeTeamRepository
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.TeamInvite
import com.revlens.app.domain.model.TeamInviteDetail
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.model.Workspace
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
    private val json = Json { ignoreUnknownKeys = true }
    private val _workspaces = MutableStateFlow<List<Workspace>>(
        listOf(Workspace(id = "ws_default", name = "My Workspace", currency = "USD"))
    )
    private var hasRemoteWorkspaces = false

    override val workspacesFlow: Flow<List<Workspace>> = callbackFlow {
        val firestore = db
        val fbAuth = auth
        var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null

        // Immediately emit current workspaces so collectors never block
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
                            android.util.Log.e("RevLensFirestore", "Workspace snapshot error", error)
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
                                    com.revlens.app.domain.model.BusinessModel.valueOf(bmStr)
                                } catch (_: Throwable) {
                                    com.revlens.app.domain.model.BusinessModel.B2bSaas
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
                                _workspaces.value = list
                                trySend(list)
                            } else {
                                trySend(_workspaces.value)
                            }
                        }
                    }
            } catch (e: Throwable) {
                android.util.Log.e("RevLensFirestore", "Workspace snapshot attach error", e)
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
                android.util.Log.e("RevLensFirestore", "Workspace auth listener attach error", e)
            }
        }

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.workspacesFlow.collect { localList ->
                if (!hasRemoteWorkspaces) {
                    if (localList.isNotEmpty()) {
                        _workspaces.value = localList
                        trySend(localList)
                    } else {
                        trySend(_workspaces.value)
                    }
                }
            }
        }

        awaitClose {
            if (fbAuth != null && authStateListener != null) {
                try { fbAuth.removeAuthStateListener(authStateListener) } catch (_: Throwable) {}
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
                    com.revlens.app.domain.model.BusinessModel.valueOf(bmStr)
                } catch (_: Throwable) {
                    com.revlens.app.domain.model.BusinessModel.B2bSaas
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
            android.util.Log.e("RevLensFirestore", "getWorkspace error", e)
        }
        return localFallback.getWorkspace(id)
    }

    override suspend fun createWorkspace(workspace: Workspace) {
        val current = _workspaces.value.toMutableList()
        val index = current.indexOfFirst { it.id == workspace.id }
        if (index != -1) {
            current[index] = workspace
        } else {
            current.add(workspace)
        }
        _workspaces.value = current
        localFallback.createWorkspace(workspace)

        val firestore = db ?: return
        if (workspace.id.isBlank()) return
        val uid = auth?.currentUser?.uid
        val map = mutableMapOf<String, Any>(
            "id" to workspace.id,
            "name" to workspace.name,
            "currency" to workspace.currency,
            "timeZoneId" to workspace.timeZoneId,
            "fiscalYearStartMonth" to workspace.fiscalYearStartMonth,
            "businessModel" to workspace.businessModel.name,
            "createdAtMillis" to workspace.createdAtMillis,
            "updatedAt" to System.currentTimeMillis()
        )
        if (!uid.isNullOrBlank()) {
            map["userId"] = uid
            try {
                firestore.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .document(workspace.id)
                    .set(map)
                    .await()
            } catch (e: Throwable) {
                android.util.Log.e("RevLensFirestore", "Failed to save user-scoped workspace", e)
            }
        }
        try {
            firestore.collection("workspaces")
                .document(workspace.id)
                .set(map)
                .await()
        } catch (_: Throwable) {}
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
            try {
                firestore.collection("workspaces")
                    .document(id)
                    .delete()
                    .await()
            } catch (_: Throwable) {}
        } catch (e: Throwable) {
            android.util.Log.e("RevLensFirestore", "deleteWorkspace error", e)
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
        var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null

        // Immediately emit current cached profile so collectors never hang
        trySend(_profile.value)

        fun attachListener(uid: String) {
            if (uid.isBlank() || firestore == null) return
            firestoreListener?.remove()
            try {
                firestoreListener = firestore.collection("users")
                    .document(uid)
                    .collection("profile")
                    .document("current")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("RevLensFirestore", "Profile snapshot error", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            if (snapshot.exists()) {
                                val payload = snapshot.getString("payload")
                                if (payload != null) {
                                    try {
                                        val profile = json.decodeFromString<BusinessProfile>(payload)
                                        _profile.value = profile
                                        CoroutineScope(Dispatchers.IO).launch {
                                            localFallback.saveProfile(profile)
                                        }
                                        trySend(profile)
                                    } catch (e: Throwable) {
                                        android.util.Log.e(
                                            "RevLensFirestore",
                                            "Decode profile error in realtime listener", e
                                        )
                                    }
                                }
                            } else {
                                if (_profile.value == null) {
                                    trySend(null)
                                }
                            }
                        }
                    }
            } catch (e: Throwable) {
                android.util.Log.e("RevLensFirestore", "Profile snapshot attach error", e)
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
                android.util.Log.e("RevLensFirestore", "Profile auth listener error", e)
            }
        }

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.profileFlow.collect { localProf ->
                _profile.value = localProf
                trySend(localProf)
            }
        }

        awaitClose {
            if (fbAuth != null && authStateListener != null) {
                try { fbAuth.removeAuthStateListener(authStateListener) } catch (_: Throwable) {}
            }
            firestoreListener?.remove()
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
            val map = mutableMapOf<String, Any>(
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
            } catch (_: Throwable) {}

            if (!uid.isNullOrBlank()) {
                map["userId"] = uid
                try {
                    firestore.collection("users")
                        .document(uid)
                        .collection("profile")
                        .document("current")
                        .set(map)
                        .await()
                } catch (_: Throwable) {}

                try {
                    firestore.collection("users")
                        .document(uid)
                        .set(
                            mapOf(
                                "id" to uid,
                                "onboardingCompleted" to true,
                                "updatedAt" to System.currentTimeMillis()
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                        .await()
                } catch (_: Throwable) {}

                try {
                    firestore.collection("profiles")
                        .document(uid)
                        .set(map)
                        .await()
                } catch (_: Throwable) {}
            } else {
                try {
                    firestore.collection("profiles")
                        .document("current")
                        .set(map)
                        .await()
                } catch (_: Throwable) {}
            }
        } catch (e: Throwable) {
            android.util.Log.e("RevLensFirestore", "saveProfile error", e)
        }
    }

    override suspend fun syncRemoteProfile(): BusinessProfile? {
        val firestore = db ?: return null
        return try {
            val uid = auth?.currentUser?.uid
            val rawWsId = workspaceContext?.activeWorkspaceId?.value
            val wsId = if (rawWsId.isNullOrBlank()) "ws_default" else rawWsId

            var snapshot = if (!uid.isNullOrBlank()) {
                val userDoc = kotlinx.coroutines.withTimeoutOrNull(2500L) {
                    try {
                        firestore.collection("users")
                            .document(uid)
                            .collection("profile")
                            .document("current")
                            .get()
                            .await()
                    } catch (_: Throwable) { null }
                }
                if (userDoc != null && userDoc.exists()) {
                    userDoc
                } else {
                    kotlinx.coroutines.withTimeoutOrNull(2500L) {
                        try {
                            firestore.collection("profiles")
                                .document(uid)
                                .get()
                                .await()
                        } catch (_: Throwable) { null }
                    }
                }
            } else {
                null
            }

            if (snapshot == null || !snapshot.exists()) {
                val wsDoc = kotlinx.coroutines.withTimeoutOrNull(2500L) {
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
                    } catch (_: Throwable) { null }
                }
                if (wsDoc != null && wsDoc.exists()) {
                    snapshot = wsDoc
                }
            }

            if (snapshot == null || !snapshot.exists()) {
                snapshot = kotlinx.coroutines.withTimeoutOrNull(2500L) {
                    try {
                        firestore.collection("profiles")
                            .document("current")
                            .get()
                            .await()
                    } catch (_: Throwable) { null }
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
            android.util.Log.e("RevLensFirestore", "syncRemoteProfile failed", e)
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
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                    .await()
            }
        } catch (e: Throwable) {
            android.util.Log.e("RevLensFirestore", "setOnboardingCompleted error", e)
        }
    }
}

class AndroidFirestoreScenarioRepository(
    private val localFallback: ScenarioRepository
) : ScenarioRepository {

    private val db: FirebaseFirestore? get() = safeFirestore
    private val auth: FirebaseAuth? get() = safeAuth
    private val json = Json { ignoreUnknownKeys = true }
    private val _scenarios = MutableStateFlow<List<Scenario>>(emptyList())

    override val scenariosFlow: Flow<List<Scenario>> = callbackFlow {
        val firestore = db
        val fbAuth = auth
        var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null

        // Immediately emit current scenarios cache so collectors never block
        trySend(_scenarios.value)

        fun attachListener(uid: String) {
            if (uid.isBlank() || firestore == null) return
            firestoreListener?.remove()
            try {
                firestoreListener = firestore.collection("users")
                    .document(uid)
                    .collection("scenarios")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("RevLensFirestore", "Scenarios snapshot error", error)
                            return@addSnapshotListener
                        }
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
            } catch (e: Throwable) {
                android.util.Log.e("RevLensFirestore", "Scenarios snapshot attach error", e)
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
                android.util.Log.e("RevLensFirestore", "Scenarios auth listener error", e)
            }
        }

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.scenariosFlow.collect { localList ->
                _scenarios.value = localList
                trySend(localList)
            }
        }

        awaitClose {
            if (fbAuth != null && authStateListener != null) {
                try { fbAuth.removeAuthStateListener(authStateListener) } catch (_: Throwable) {}
            }
            firestoreListener?.remove()
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
        val serialized = json.encodeToString(Scenario.serializer(), scenario)
        val map = mutableMapOf<String, Any>(
            "id" to scenario.id,
            "name" to scenario.name,
            "payload" to serialized,
            "updatedAt" to System.currentTimeMillis()
        )
        if (!uid.isNullOrBlank()) {
            map["userId"] = uid
            try {
                firestore.collection("users")
                    .document(uid)
                    .collection("scenarios")
                    .document(scenario.id)
                    .set(map)
                    .await()
            } catch (e: Throwable) {
                android.util.Log.e("RevLensFirestore", "Failed to save user-scoped scenario", e)
            }
        }
        try {
            firestore.collection("scenarios")
                .document(scenario.id)
                .set(map)
                .await()
        } catch (_: Throwable) {}
    }

    override suspend fun deleteScenario(scenarioId: String) {
        _scenarios.value = _scenarios.value.filter { it.id != scenarioId }
        localFallback.deleteScenario(scenarioId)
        val firestore = db ?: return
        if (scenarioId.isBlank()) return
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
            try {
                firestore.collection("scenarios")
                    .document(scenarioId)
                    .delete()
                    .await()
            } catch (_: Throwable) {}
        } catch (e: Throwable) {
            android.util.Log.e("RevLensFirestore", "deleteScenario error", e)
        }
    }
}

class AndroidFirestoreTeamRepository(
    private val localFallback: FakeTeamRepository,
    private val workspaceContext: WorkspaceContext? = null,
    private val profileRepository: ProfileRepository? = null
) : TeamRepository {

    private val db: FirebaseFirestore? get() = safeFirestore
    private val auth: FirebaseAuth? get() = safeAuth
    private val json = Json { ignoreUnknownKeys = true }

    override fun getMembers(workspaceId: String): Flow<List<Member>> = callbackFlow {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        var listener: com.google.firebase.firestore.ListenerRegistration? = null
        var currentList = emptyList<Member>()

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
                            android.util.Log.e("RevLensFirestore", "Team members snapshot error", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && !snapshot.isEmpty) {
                            val members = snapshot.documents.mapNotNull { doc ->
                                val id = doc.getString("id") ?: doc.id
                                val name = doc.getString("name") ?: "Member"
                                val email = doc.getString("email") ?: ""
                                val roleStr = doc.getString("role") ?: "Viewer"
                                val role = try { Role.valueOf(roleStr) } catch (_: Throwable) { Role.Viewer }
                                val joinedAt = doc.getString("joinedAt") ?: "Member"
                                val avatarUrl = doc.getString("avatarUrl")
                                Member(id = id, name = name, email = email, role = role, avatarUrl = avatarUrl, joinedAt = joinedAt)
                            }
                            if (members.isNotEmpty()) {
                                currentList = members
                                trySend(members)
                            }
                        }
                    }
            } catch (e: Throwable) {
                android.util.Log.e("RevLensFirestore", "Team members attach error", e)
            }
        }

        attach(cleanWsId)

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.getMembers(cleanWsId).collect { localList ->
                if (currentList.isEmpty()) {
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
        var listener: com.google.firebase.firestore.ListenerRegistration? = null
        var currentInvites = emptyList<TeamInvite>()

        fun attach(wsId: String) {
            val firestore = db ?: return
            if (wsId.isBlank()) return
            listener?.remove()
            try {
                listener = firestore.collection("workspaces")
                    .document(wsId)
                    .collection("invites")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("RevLensFirestore", "Invites snapshot error", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val invites = snapshot.documents.mapNotNull { doc ->
                                val id = doc.getString("id") ?: doc.id
                                val email = doc.getString("email") ?: return@mapNotNull null
                                val roleStr = doc.getString("role") ?: "Viewer"
                                val role = try { Role.valueOf(roleStr) } catch (_: Throwable) { Role.Viewer }
                                val sentAt = doc.getString("sentAt") ?: "Just now"
                                val invitedBy = doc.getString("invitedBy") ?: "Admin"
                                TeamInvite(id = id, email = email, role = role, sentAt = sentAt, invitedBy = invitedBy)
                            }
                            currentInvites = invites
                            trySend(invites)
                        }
                    }
            } catch (e: Throwable) {
                android.util.Log.e("RevLensFirestore", "Invites snapshot attach error", e)
            }
        }

        attach(cleanWsId)

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.getInvites(cleanWsId).collect { localList ->
                if (currentInvites.isEmpty()) {
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
                "email" to invite.email,
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

            val authUser = auth?.currentUser
            if (authUser != null) {
                val ownerMap = mapOf(
                    "id" to authUser.uid,
                    "name" to (authUser.displayName ?: inviterName),
                    "email" to (authUser.email ?: ""),
                    "role" to Role.Owner.name,
                    "joinedAt" to "Workspace Founder",
                    "avatarUrl" to authUser.photoUrl?.toString()
                )
                firestore.collection("workspaces")
                    .document(cleanWsId)
                    .collection("members")
                    .document(authUser.uid)
                    .set(ownerMap, com.google.firebase.firestore.SetOptions.merge())
                    .await()
            }
        } catch (e: Throwable) {
            android.util.Log.e("RevLensFirestore", "inviteMember firestore error", e)
        }
        return Result.success(invite)
    }

    override suspend fun updateMemberRole(
        workspaceId: String, memberId: String, newRole: Role
    ): Result<Unit> {
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
            android.util.Log.e("RevLensFirestore", "updateMemberRole error", e)
        }
        return Result.success(Unit)
    }

    override suspend fun removeMember(workspaceId: String, memberId: String): Result<Unit> {
        val cleanWsId = workspaceId.ifBlank { "ws_default" }
        localFallback.removeMember(cleanWsId, memberId)
        val firestore = db ?: return Result.success(Unit)
        if (cleanWsId.isBlank() || memberId.isBlank()) return Result.success(Unit)
        try {
            firestore.collection("workspaces")
                .document(cleanWsId)
                .collection("members")
                .document(memberId)
                .delete()
                .await()
        } catch (e: Throwable) {
            android.util.Log.e("RevLensFirestore", "removeMember error", e)
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
            android.util.Log.e("RevLensFirestore", "revokeInvite error", e)
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
            android.util.Log.e("RevLensFirestore", "resendInvite error", e)
        }
        return Result.success(Unit)
    }

    override suspend fun getPendingInvitesForEmail(email: String): List<TeamInviteDetail> {
        val normalized = email.trim().lowercase()
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
                    val wsId = doc.getString("workspaceId")?.ifBlank { "ws_default" } ?: "ws_default"
                    val roleStr = doc.getString("role") ?: "Viewer"
                    val role = try { Role.valueOf(roleStr) } catch (_: Throwable) { Role.Viewer }
                    val sentAt = doc.getString("sentAt") ?: "Just now"
                    val invitedBy = doc.getString("invitedBy") ?: "Admin"
                    val wsDoc = try {
                        firestore.collection("workspaces").document(wsId).get().await()
                    } catch (_: Throwable) { null }
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
                android.util.Log.e("RevLensFirestore", "getPendingInvitesForEmail error", e)
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
                    val doc = firestore.collection("team_invites").document(inviteId).get().await()
                    doc.getString("workspaceId")?.ifBlank { "ws_default" }
                } catch (_: Throwable) { null }
            } else null
        } ?: return Result.failure(NoSuchElementException("Invitation not found."))

        if (firestore != null) {
            try {
                val inviteDoc = try {
                    firestore.collection("team_invites").document(inviteId).get().await()
                } catch (_: Throwable) { null }

                val roleStr = inviteDoc?.getString("role") ?: "Viewer"
                val role = try { Role.valueOf(roleStr) } catch (_: Throwable) { Role.Viewer }

                val memberMap = mapOf(
                    "id" to user.id,
                    "name" to user.name.ifBlank { user.email.substringBefore("@") },
                    "email" to user.email.trim().lowercase(),
                    "role" to role.name,
                    "joinedAt" to "Joined just now",
                    "avatarUrl" to user.avatarUrl
                )

                firestore.collection("workspaces")
                    .document(targetWsId)
                    .collection("members")
                    .document(user.id.ifBlank { "mem_${user.email.hashCode()}" })
                    .set(memberMap)
                    .await()

                firestore.collection("workspaces")
                    .document(targetWsId)
                    .collection("invites")
                    .document(inviteId)
                    .delete()
                    .await()
                firestore.collection("team_invites")
                    .document(inviteId)
                    .delete()
                    .await()

                val wsDoc = try { firestore.collection("workspaces").document(targetWsId).get().await() } catch (_: Throwable) { null }
                if (wsDoc != null && wsDoc.exists()) {
                    val wsData = wsDoc.data?.toMutableMap() ?: mutableMapOf()
                    wsData["id"] = targetWsId
                    wsData["role"] = role.name
                    firestore.collection("users")
                        .document(user.id)
                        .collection("workspaces")
                        .document(targetWsId)
                        .set(wsData)
                        .await()
                }

                val profDoc = try {
                    val doc = firestore.collection("workspaces").document(targetWsId).collection("profile").document("current").get().await()
                    if (doc.exists()) doc else firestore.collection("profiles").document(targetWsId).get().await()
                } catch (_: Throwable) { null }

                if (profDoc != null && profDoc.exists()) {
                    val payload = profDoc.getString("payload")
                    if (payload != null) {
                        try {
                            val profile = json.decodeFromString<BusinessProfile>(payload)
                            profileRepository?.saveProfile(profile)
                            localFallback.saveWorkspaceProfile(targetWsId, profile)
                        } catch (_: Throwable) {}
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("RevLensFirestore", "acceptInvite firestore error", e)
            }
        }

        return Result.success(targetWsId)
    }

    override suspend fun declineInvite(inviteId: String, email: String): Result<Unit> {
        localFallback.declineInvite(inviteId, email)
        val firestore = db ?: return Result.success(Unit)
        if (inviteId.isBlank()) return Result.success(Unit)
        try {
            firestore.collection("team_invites").document(inviteId).delete().await()
        } catch (e: Throwable) {
            android.util.Log.e("RevLensFirestore", "declineInvite error", e)
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
        val normalized = userEmail.trim().lowercase()
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
                    val roleStr = snapshot.documents.first().getString("role")
                    if (roleStr != null) {
                        return try { Role.valueOf(roleStr) } catch (_: Throwable) { Role.Viewer }
                    }
                }
            } catch (_: Throwable) {}
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
            val normalized = userEmail.trim().lowercase()
            val snapshot = firestore.collection("workspaces")
                .document(cleanWsId)
                .collection("members")
                .whereEqualTo("email", normalized)
                .get()
                .await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
        } catch (_: Throwable) {}
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
                val rootDoc = firestore.collection("profiles").document(cleanWsId).get().await()
                if (rootDoc.exists()) {
                    val payload = rootDoc.getString("payload")
                    if (payload != null) {
                        return json.decodeFromString<BusinessProfile>(payload)
                    }
                }
            } catch (_: Throwable) {}
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
            android.util.Log.e("RevLensFirestore", "saveWorkspaceProfile error", e)
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
