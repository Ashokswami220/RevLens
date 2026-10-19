package com.revlens.app.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.revlens.app.domain.model.Workspace
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

class AndroidFirestoreWorkspaceRepository(
    private val localFallback: WorkspaceRepository = DefaultWorkspaceRepository()
) : WorkspaceRepository {

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val json = Json { ignoreUnknownKeys = true }
    private val _workspaces = MutableStateFlow<List<Workspace>>(
        listOf(Workspace(id = "ws_default", name = "My Workspace", currency = "USD"))
    )
    override val workspacesFlow: Flow<List<Workspace>> = callbackFlow {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null

        // Immediately emit current workspaces so collectors never block
        trySend(_workspaces.value)

        fun attachListener(uid: String) {
            firestoreListener?.remove()
            firestoreListener = db.collection("users")
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
                            } catch (_: Exception) {
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
                            _workspaces.value = list
                            trySend(list)
                        } else {
                            trySend(_workspaces.value)
                        }
                    }
                }
        }

        val authStateListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { fbAuth ->
            val uid = fbAuth.currentUser?.uid
            if (uid != null) attachListener(uid) else firestoreListener?.remove()
        }
        auth.addAuthStateListener(authStateListener)
        val initialUid = auth.currentUser?.uid
        if (initialUid != null) attachListener(initialUid)

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.workspacesFlow.collect { localList ->
                if (localList.isNotEmpty()) {
                    _workspaces.value = localList
                    trySend(localList)
                } else {
                    trySend(_workspaces.value)
                }
            }
        }

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
            firestoreListener?.remove()
            localJob.cancel()
        }
    }

    override suspend fun getWorkspace(id: String): Workspace? {
        val inMem = _workspaces.value.firstOrNull { it.id == id }
        if (inMem != null) return inMem

        try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            val snapshot = if (uid != null) {
                val userWs = db.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .document(id)
                    .get()
                    .await()
                if (userWs.exists()) userWs else db.collection("workspaces")
                    .document(id)
                    .get()
                    .await()
            } else {
                db.collection("workspaces")
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
                } catch (_: Exception) {
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
        } catch (e: Exception) {
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

        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        android.util.Log.d(
            "RevLensFirestore",
            "createWorkspace id=${workspace.id}, name=${workspace.name}, uid=$uid"
        )
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
        if (uid != null) {
            map["userId"] = uid
            try {
                db.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .document(workspace.id)
                    .set(map)
                    .await()
                android.util.Log.d(
                    "RevLensFirestore", "Saved workspace to users/$uid/workspaces/${workspace.id}"
                )
            } catch (e: Exception) {
                android.util.Log.e(
                    "RevLensFirestore", "Failed to save user-scoped workspace: ${e.message}", e
                )
                if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true) {
                    android.util.Log.e(
                        "RevLensFirestore",
                        "PERMISSION_DENIED: Firestore security rules in Firebase Console are blocking writes! Update rules in Firebase Console."
                    )
                }
            }
        }
        try {
            db.collection("workspaces")
                .document(workspace.id)
                .set(map)
                .await()
            android.util.Log.d(
                "RevLensFirestore", "Saved workspace to root workspaces/${workspace.id}"
            )
        } catch (e: Exception) {
            android.util.Log.w(
                "RevLensFirestore", "Could not write to root workspaces: ${e.message}"
            )
        }
    }

    override suspend fun updateWorkspace(workspace: Workspace) {
        createWorkspace(workspace)
    }

    override suspend fun deleteWorkspace(id: String) {
        _workspaces.value = _workspaces.value.filter { it.id != id }
        localFallback.deleteWorkspace(id)
        try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                db.collection("users")
                    .document(uid)
                    .collection("workspaces")
                    .document(id)
                    .delete()
                    .await()
            }
            try {
                db.collection("workspaces")
                    .document(id)
                    .delete()
                    .await()
            } catch (_: Exception) {
            }
        } catch (e: Exception) {
            android.util.Log.e("RevLensFirestore", "deleteWorkspace error", e)
        }
    }
}

class AndroidFirestoreProfileRepository(
    private val localFallback: ProfileRepository
) : ProfileRepository {

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val json = Json { ignoreUnknownKeys = true }
    private val _profile = MutableStateFlow<BusinessProfile?>(null)

    override val profileFlow: Flow<BusinessProfile?> = callbackFlow {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null

        // Immediately emit current cached profile so collectors never hang
        trySend(_profile.value)

        fun attachListener(uid: String) {
            firestoreListener?.remove()
            firestoreListener = db.collection("users")
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
                                } catch (e: Exception) {
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
        }

        val authStateListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { fbAuth ->
            val uid = fbAuth.currentUser?.uid
            if (uid != null) attachListener(uid) else firestoreListener?.remove()
        }
        auth.addAuthStateListener(authStateListener)
        val initialUid = auth.currentUser?.uid
        if (initialUid != null) attachListener(initialUid)

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.profileFlow.collect { localProf ->
                _profile.value = localProf
                trySend(localProf)
            }
        }

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
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
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        android.util.Log.d("RevLensFirestore", "saveProfile called, uid=$uid")
        val serialized = json.encodeToString(BusinessProfile.serializer(), profile)
        val map = mutableMapOf<String, Any>(
            "payload" to serialized,
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

        if (uid != null) {
            map["userId"] = uid
            // 1. User-scoped profile document users/{uid}/profile/current
            try {
                db.collection("users")
                    .document(uid)
                    .collection("profile")
                    .document("current")
                    .set(map)
                    .await()
                android.util.Log.d(
                    "RevLensFirestore", "Profile saved to users/$uid/profile/current"
                )
            } catch (e: Exception) {
                android.util.Log.e(
                    "RevLensFirestore",
                    "Failed to save profile to users/$uid/profile/current: ${e.message}", e
                )
                if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true) {
                    android.util.Log.e(
                        "RevLensFirestore",
                        "PERMISSION_DENIED: Firestore security rules in Firebase Console are blocking writes! Update rules in Firebase Console."
                    )
                }
            }

            // 2. Ensure root users/{uid} document exists and marks onboarding complete
            try {
                db.collection("users")
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
                android.util.Log.d("RevLensFirestore", "Updated users/$uid document")
            } catch (e: Exception) {
                android.util.Log.w(
                    "RevLensFirestore", "Could not update user document: ${e.message}"
                )
            }

            // 3. Root profiles/{uid} document
            try {
                db.collection("profiles")
                    .document(uid)
                    .set(map)
                    .await()
                android.util.Log.d("RevLensFirestore", "Profile saved to profiles/$uid")
            } catch (pe: Exception) {
                android.util.Log.w(
                    "RevLensFirestore", "Could not write to profiles/$uid: ${pe.message}"
                )
            }
        } else {
            try {
                db.collection("profiles")
                    .document("current")
                    .set(map)
                    .await()
            } catch (ce: Exception) {
                android.util.Log.w(
                    "RevLensFirestore", "Could not write to profiles/current: ${ce.message}"
                )
            }
        }
    }

    override suspend fun syncRemoteProfile(): BusinessProfile? {
        return try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            android.util.Log.d("RevLensFirestore", "syncRemoteProfile called, uid=$uid")
            val snapshot = if (uid != null) {
                val userDoc = kotlinx.coroutines.withTimeoutOrNull(2500L) {
                    db.collection("users")
                        .document(uid)
                        .collection("profile")
                        .document("current")
                        .get()
                        .await()
                }
                if (userDoc != null && userDoc.exists()) {
                    userDoc
                } else {
                    kotlinx.coroutines.withTimeoutOrNull(2500L) {
                        db.collection("profiles")
                            .document(uid)
                            .get()
                            .await()
                    }
                }
            } else {
                kotlinx.coroutines.withTimeoutOrNull(2500L) {
                    db.collection("profiles")
                        .document("current")
                        .get()
                        .await()
                }
            }
            if (snapshot != null && snapshot.exists()) {
                val payload = snapshot.getString("payload")
                if (payload != null) {
                    val profile = json.decodeFromString<BusinessProfile>(payload)
                    _profile.value = profile
                    localFallback.saveProfile(profile)
                    localFallback.setOnboardingCompleted(true)
                    android.util.Log.d("RevLensFirestore", "Remote profile synced successfully")
                    profile
                } else null
            } else null
        } catch (e: Exception) {
            android.util.Log.e("RevLensFirestore", "syncRemoteProfile failed", e)
            null
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        localFallback.setOnboardingCompleted(completed)
        try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                db.collection("users")
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
        } catch (e: Exception) {
            android.util.Log.e("RevLensFirestore", "setOnboardingCompleted error", e)
        }
    }
}

class AndroidFirestoreScenarioRepository(
    private val localFallback: ScenarioRepository
) : ScenarioRepository {

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val json = Json { ignoreUnknownKeys = true }
    private val _scenarios = MutableStateFlow<List<Scenario>>(emptyList())
    override val scenariosFlow: Flow<List<Scenario>> = callbackFlow {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null

        // Immediately emit current scenarios cache so collectors never block
        trySend(_scenarios.value)

        fun attachListener(uid: String) {
            firestoreListener?.remove()
            firestoreListener = db.collection("users")
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
                                } catch (_: Exception) {
                                    null
                                }
                            } else null
                        }
                        _scenarios.value = list
                        trySend(list)
                    }
                }
        }

        val authStateListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { fbAuth ->
            val uid = fbAuth.currentUser?.uid
            if (uid != null) attachListener(uid) else firestoreListener?.remove()
        }
        auth.addAuthStateListener(authStateListener)
        val initialUid = auth.currentUser?.uid
        if (initialUid != null) attachListener(initialUid)

        val localJob = CoroutineScope(Dispatchers.IO).launch {
            localFallback.scenariosFlow.collect { localList ->
                _scenarios.value = localList
                trySend(localList)
            }
        }

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
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

        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        val serialized = json.encodeToString(Scenario.serializer(), scenario)
        val map = mutableMapOf<String, Any>(
            "id" to scenario.id,
            "name" to scenario.name,
            "payload" to serialized,
            "updatedAt" to System.currentTimeMillis()
        )
        if (uid != null) {
            map["userId"] = uid
            try {
                db.collection("users")
                    .document(uid)
                    .collection("scenarios")
                    .document(scenario.id)
                    .set(map)
                    .await()
                android.util.Log.d(
                    "RevLensFirestore", "Saved scenario to users/$uid/scenarios/${scenario.id}"
                )
            } catch (e: Exception) {
                android.util.Log.e(
                    "RevLensFirestore", "Failed to save user-scoped scenario: ${e.message}", e
                )
                if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true) {
                    android.util.Log.e(
                        "RevLensFirestore",
                        "PERMISSION_DENIED: Firestore security rules in Firebase Console are blocking writes! Update rules in Firebase Console."
                    )
                }
            }
        }
        try {
            db.collection("scenarios")
                .document(scenario.id)
                .set(map)
                .await()
            android.util.Log.d(
                "RevLensFirestore", "Saved scenario to root scenarios/${scenario.id}"
            )
        } catch (_: Exception) {
        }
    }

    override suspend fun deleteScenario(scenarioId: String) {
        _scenarios.value = _scenarios.value.filter { it.id != scenarioId }
        localFallback.deleteScenario(scenarioId)
        try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                db.collection("users")
                    .document(uid)
                    .collection("scenarios")
                    .document(scenarioId)
                    .delete()
                    .await()
            }
            try {
                db.collection("scenarios")
                    .document(scenarioId)
                    .delete()
                    .await()
            } catch (_: Exception) {
            }
        } catch (e: Exception) {
            android.util.Log.e("RevLensFirestore", "deleteScenario error", e)
        }
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}
