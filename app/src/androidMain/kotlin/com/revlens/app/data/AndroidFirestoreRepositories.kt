package com.revlens.app.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    override val workspacesFlow: Flow<List<Workspace>> = _workspaces.asStateFlow()

    override suspend fun getWorkspace(id: String): Workspace? {
        val inMem = _workspaces.value.firstOrNull { it.id == id }
        if (inMem != null) return inMem

        try {
            val snapshot = db.collection("workspaces")
                .document(id)
                .get()
                .await()
            if (snapshot.exists()) {
                val data = snapshot.data
                if (data != null) {
                    val rawJson = json.encodeToString(data)
                    return json.decodeFromString<Workspace>(rawJson)
                }
            }
        } catch (_: Exception) {
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

        try {
            val map = mapOf(
                "id" to workspace.id,
                "name" to workspace.name,
                "currency" to workspace.currency,
                "timeZoneId" to workspace.timeZoneId,
                "fiscalYearStartMonth" to workspace.fiscalYearStartMonth,
                "businessModel" to workspace.businessModel.name,
                "createdAtMillis" to workspace.createdAtMillis
            )
            db.collection("workspaces")
                .document(workspace.id)
                .set(map)
        } catch (_: Exception) {
        }
    }

    override suspend fun updateWorkspace(workspace: Workspace) {
        createWorkspace(workspace)
    }

    override suspend fun deleteWorkspace(id: String) {
        _workspaces.value = _workspaces.value.filter { it.id != id }
        localFallback.deleteWorkspace(id)
        try {
            db.collection("workspaces")
                .document(id)
                .delete()
        } catch (_: Exception) {
        }
    }
}

class AndroidFirestoreProfileRepository(
    private val localFallback: ProfileRepository
) : ProfileRepository {

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val json = Json { ignoreUnknownKeys = true }
    private val _profile = MutableStateFlow<BusinessProfile?>(null)
    override val profileFlow: Flow<BusinessProfile?> = _profile.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    override val isOnboardingCompletedFlow: Flow<Boolean> = _onboardingCompleted.asStateFlow()

    override suspend fun saveProfile(profile: BusinessProfile) {
        _profile.value = profile
        localFallback.saveProfile(profile)
        try {
            val serialized = json.encodeToString(BusinessProfile.serializer(), profile)
            val map = mapOf("payload" to serialized, "updatedAt" to System.currentTimeMillis())
            db.collection("profiles")
                .document("current")
                .set(map)
        } catch (_: Exception) {
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _onboardingCompleted.value = completed
        localFallback.setOnboardingCompleted(completed)
    }
}

class AndroidFirestoreScenarioRepository(
    private val localFallback: ScenarioRepository
) : ScenarioRepository {

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val json = Json { ignoreUnknownKeys = true }
    private val _scenarios = MutableStateFlow<List<Scenario>>(emptyList())
    override val scenariosFlow: Flow<List<Scenario>> = _scenarios.asStateFlow()

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

        try {
            val serialized = json.encodeToString(Scenario.serializer(), scenario)
            val map = mapOf("id" to scenario.id, "payload" to serialized)
            db.collection("scenarios")
                .document(scenario.id)
                .set(map)
        } catch (_: Exception) {
        }
    }

    override suspend fun deleteScenario(scenarioId: String) {
        _scenarios.value = _scenarios.value.filter { it.id != scenarioId }
        localFallback.deleteScenario(scenarioId)
        try {
            db.collection("scenarios")
                .document(scenarioId)
                .delete()
        } catch (_: Exception) {
        }
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}
