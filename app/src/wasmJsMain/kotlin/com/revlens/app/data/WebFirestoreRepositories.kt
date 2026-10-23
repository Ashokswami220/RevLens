package com.revlens.app.data

import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import kotlinx.browser.window
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
            val success = withTimeoutOrNull(3500L.milliseconds) {
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

    override suspend fun clearLocalData() {
        _workspaces.value = emptyList()
        localFallback.clearLocalData()
    }
}

class WebFirestoreProfileRepository(
    private val localFallback: ProfileRepository,
    private val workspaceContext: WorkspaceContext? = null,
    private val authRepository: AuthRepository? = null
) : ProfileRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override val profileFlow: Flow<BusinessProfile?> = localFallback.profileFlow
    override val isOnboardingCompletedFlow: Flow<Boolean> = localFallback.isOnboardingCompletedFlow
    override val hasSeenIntroFlow: Flow<Boolean> = localFallback.hasSeenIntroFlow

    private fun sanitizeKey(raw: String): String {
        return raw.trim().lowercase().replace(Regex("[^a-z0-9_-]"), "_")
    }

    private suspend fun resolveUserDocIds(): List<String> {
        val user = runCatching { authRepository?.getCurrentUser() }.getOrNull()
        val ids = mutableListOf<String>()
        val uid = user?.id?.trim().orEmpty()
        val email = user?.email?.trim()?.lowercase().orEmpty()
        if (uid.isNotBlank()) ids.add("user_$uid")
        if (email.isNotBlank()) ids.add("email_${sanitizeKey(email)}")
        val wsId = workspaceContext?.activeWorkspaceId?.value
            ?.takeIf { it.isNotBlank() && it != "ws_default" }
        if (wsId != null) ids.add(wsId)
        return ids.distinct()
    }

    override suspend fun setHasSeenIntro(seen: Boolean) {
        localFallback.setHasSeenIntro(seen)
    }

    override suspend fun saveProfile(profile: BusinessProfile) {
        localFallback.saveProfile(profile)
        localFallback.setOnboardingCompleted(true)

        val serialized = try {
            json.encodeToString(BusinessProfile.serializer(), profile)
        } catch (_: Exception) {
            return
        }

        val docIds = resolveUserDocIds()
        for (docId in docIds) {
            try {
                window.localStorage.setItem("revlens_backup_$docId", serialized)
            } catch (_: Exception) {
            }
            try {
                jsTriggerSaveDoc("profiles", docId, serialized)
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun fetchRemoteDoc(docId: String): BusinessProfile? {
        return try {
            jsTriggerGetDoc("profiles", docId)
            val success = withTimeoutOrNull(2500L.milliseconds) {
                while (jsGetFirestoreStatus() == "pending") {
                    delay(50.milliseconds)
                }
                jsGetFirestoreStatus() == "success"
            }
            if (success == true) {
                val res = jsGetFirestoreResult()
                if (res.isNotBlank()) {
                    json.decodeFromString<BusinessProfile>(res)
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun syncRemoteProfile(): BusinessProfile? {
        val docIds = resolveUserDocIds()
        if (docIds.isEmpty()) return null

        // 1. Try Firestore for each candidate user/workspace key
        for (docId in docIds) {
            val remote = fetchRemoteDoc(docId)
            if (remote != null) {
                localFallback.saveProfile(remote)
                localFallback.setOnboardingCompleted(true)
                try {
                    val serialized = json.encodeToString(BusinessProfile.serializer(), remote)
                    window.localStorage.setItem("revlens_backup_$docId", serialized)
                } catch (_: Exception) {
                }
                return remote
            }
        }

        // 2. Fallback to per-user localStorage backup for this account
        for (docId in docIds) {
            try {
                val raw = window.localStorage.getItem("revlens_backup_$docId")
                if (!raw.isNullOrBlank()) {
                    val decoded = json.decodeFromString<BusinessProfile>(raw)
                    localFallback.saveProfile(decoded)
                    localFallback.setOnboardingCompleted(true)
                    return decoded
                }
            } catch (_: Exception) {
            }
        }

        return null
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        localFallback.setOnboardingCompleted(completed)
    }

    override suspend fun clearLocalData() {
        localFallback.clearLocalData()
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

    override suspend fun clearLocalData() {
        _scenarios.value = emptyList()
        localFallback.clearLocalData()
    }
}
