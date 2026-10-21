package com.revlens.app.data

import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

@JsFun(
    "(collection, docId, jsonStr) => { if (window.revlensFirebase) { window.revlensFirebase.triggerSaveDoc(collection, docId, jsonStr); } }"
)
private external fun jsTriggerSaveDoc(collection: String, docId: String, jsonStr: String)

@JsFun(
    "(collection, docId) => { if (window.revlensFirebase) { window.revlensFirebase.triggerGetDoc(collection, docId); } }"
)
private external fun jsTriggerGetDoc(collection: String, docId: String)

@JsFun(
    "(collection, docId) => { if (window.revlensFirebase) { window.revlensFirebase.triggerDeleteDoc(collection, docId); } }"
)
private external fun jsTriggerDeleteDoc(collection: String, docId: String)

@JsFun("() => window.revlensFirebase ? window.revlensFirebase.lastFirestoreStatus : 'error'")
private external fun jsGetFirestoreStatus(): String

@JsFun(
    "() => (window.revlensFirebase && window.revlensFirebase.lastFirestoreResult) ? window.revlensFirebase.lastFirestoreResult : ''"
)
private external fun jsGetFirestoreResult(): String

class WebFirestoreWorkspaceRepository(
    private val localFallback: DefaultWorkspaceRepository = DefaultWorkspaceRepository()
) : WorkspaceRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val _workspaces = MutableStateFlow(
        listOf(Workspace(id = "ws_default", name = "My Workspace", currency = "USD"))
    )
    override val workspacesFlow: Flow<List<Workspace>> = _workspaces.asStateFlow()

    override suspend fun getWorkspace(id: String): Workspace? {
        val inMem = _workspaces.value.firstOrNull { it.id == id }
        if (inMem != null) return inMem

        try {
            jsTriggerGetDoc("workspaces", id)
            val success = withTimeoutOrNull(4000L.milliseconds) {
                while (jsGetFirestoreStatus() == "pending") {
                    delay(50.milliseconds)
                }
                jsGetFirestoreStatus() == "success"
            }
            if (success == true) {
                val res = jsGetFirestoreResult()
                if (res.isNotBlank()) {
                    return json.decodeFromString<Workspace>(res)
                }
            }
        } catch (_: Exception) {
        }
        return localFallback.getWorkspace(id)
    }

    override suspend fun createWorkspace(workspace: Workspace) {
        val oldIds = _workspaces.value.map { it.id }
            .filter { it != workspace.id }
        _workspaces.value = listOf(workspace)
        oldIds.forEach { localFallback.deleteWorkspace(it) }
        localFallback.createWorkspace(workspace)

        try {
            val serialized = json.encodeToString(Workspace.serializer(), workspace)
            jsTriggerSaveDoc("workspaces", workspace.id, serialized)
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
            jsTriggerDeleteDoc("workspaces", id)
        } catch (_: Exception) {
        }
    }
}

class WebFirestoreProfileRepository(
    private val localFallback: ProfileRepository,
    private val workspaceContext: WorkspaceContext? = null
) : ProfileRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override val profileFlow: Flow<BusinessProfile?> = localFallback.profileFlow
    override val isOnboardingCompletedFlow: Flow<Boolean> = localFallback.isOnboardingCompletedFlow
    override val hasSeenIntroFlow: Flow<Boolean> = localFallback.hasSeenIntroFlow

    override suspend fun setHasSeenIntro(seen: Boolean) {
        localFallback.setHasSeenIntro(seen)
    }

    override suspend fun saveProfile(profile: BusinessProfile) {
        localFallback.saveProfile(profile)
        val wsId =
            workspaceContext?.activeWorkspaceId?.value?.takeIf { it.isNotBlank() } ?: "current"
        try {
            val serialized = json.encodeToString(BusinessProfile.serializer(), profile)
            jsTriggerSaveDoc("profiles", wsId, serialized)
            jsTriggerSaveDoc("profiles", "current", serialized)
        } catch (_: Exception) {
        }
    }

    override suspend fun syncRemoteProfile(): BusinessProfile? {
        val wsId =
            workspaceContext?.activeWorkspaceId?.value?.takeIf { it.isNotBlank() } ?: "current"
        try {
            jsTriggerGetDoc("profiles", wsId)
            val success = withTimeoutOrNull(3000L.milliseconds) {
                while (jsGetFirestoreStatus() == "pending") {
                    delay(50.milliseconds)
                }
                jsGetFirestoreStatus() == "success"
            }
            if (success == true) {
                val res = jsGetFirestoreResult()
                if (res.isNotBlank()) {
                    val decoded = json.decodeFromString<BusinessProfile>(res)
                    localFallback.saveProfile(decoded)
                    localFallback.setOnboardingCompleted(true)
                    return decoded
                }
            }
        } catch (_: Exception) {
        }
        return null
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        localFallback.setOnboardingCompleted(completed)
    }
}

class WebFirestoreScenarioRepository(
    private val localFallback: ScenarioRepository
) : ScenarioRepository {

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
            jsTriggerSaveDoc("scenarios", scenario.id, serialized)
        } catch (_: Exception) {
        }
    }

    override suspend fun deleteScenario(scenarioId: String) {
        _scenarios.value = _scenarios.value.filter { it.id != scenarioId }
        localFallback.deleteScenario(scenarioId)
        try {
            jsTriggerDeleteDoc("scenarios", scenarioId)
        } catch (_: Exception) {
        }
    }
}
