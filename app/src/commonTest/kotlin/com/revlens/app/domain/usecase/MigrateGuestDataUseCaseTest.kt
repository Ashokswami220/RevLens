package com.revlens.app.domain.usecase

import com.revlens.app.data.DefaultWorkspaceRepository
import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.context.DefaultWorkspaceContext
import com.revlens.app.domain.model.Workspace
import com.revlens.app.model.Assumptions
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FakeProfileRepository : ProfileRepository {
    private val _profile = MutableStateFlow<BusinessProfile?>(null)
    override val profileFlow: Flow<BusinessProfile?> = _profile.asStateFlow()
    private val _isOnboardingCompleted = MutableStateFlow(false)
    override val isOnboardingCompletedFlow: Flow<Boolean> = _isOnboardingCompleted.asStateFlow()

    val savedProfiles = mutableListOf<BusinessProfile>()

    override suspend fun saveProfile(profile: BusinessProfile) {
        _profile.value = profile
        savedProfiles.add(profile)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _isOnboardingCompleted.value = completed
    }
}

class FakeScenarioRepository : ScenarioRepository {
    private val _scenarios = MutableStateFlow<List<Scenario>>(emptyList())
    override val scenariosFlow: Flow<List<Scenario>> = _scenarios.asStateFlow()

    val savedScenarios = mutableListOf<Scenario>()

    override suspend fun saveScenario(scenario: Scenario) {
        _scenarios.value = _scenarios.value + scenario
        savedScenarios.add(scenario)
    }

    override suspend fun deleteScenario(scenarioId: String) {
        _scenarios.value = _scenarios.value.filterNot { it.id == scenarioId }
    }
}

class MigrateGuestDataUseCaseTest {

    @Test
    fun testMigrateGuestDataCreatesDefaultWorkspaceWhenNoneExists() = runTest {
        val wsRepo = DefaultWorkspaceRepository(emptyList())
        val wsContext = DefaultWorkspaceContext(null)
        val profileRepo = FakeProfileRepository()
        val scenarioRepo = FakeScenarioRepository()

        val useCase = MigrateGuestDataUseCase(wsRepo, wsContext, profileRepo, scenarioRepo)

        useCase("usr_user123456")

        // Default workspace created
        val activeWsId = wsContext.activeWorkspaceId.value
        assertNotNull(activeWsId)
        assertTrue(activeWsId.contains("123456"))
        val ws = wsRepo.getWorkspace(activeWsId)
        assertNotNull(ws)
        assertEquals("My SaaS Workspace", ws.name)
    }

    @Test
    fun testMigrateGuestDataMigratesProfileAndScenarios() = runTest {
        val wsRepo =
            DefaultWorkspaceRepository(listOf(Workspace("ws_existing", "Existing Workspace")))
        val wsContext = DefaultWorkspaceContext("ws_existing")
        val profileRepo = FakeProfileRepository()
        val scenarioRepo = FakeScenarioRepository()

        val guestProfile = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.15,
            cac = BigDecimal("150.00"),
            fixedCosts = BigDecimal("5000.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = emptyList(),
            currencyCode = "USD"
        )
        profileRepo.saveProfile(guestProfile)

        val guestScenario = Scenario(
            id = "sc_1",
            name = "Test Growth Scenario",
            baselineProfile = guestProfile,
            assumptions = Assumptions(priceChangePercent = 10.0)
        )
        scenarioRepo.saveScenario(guestScenario)

        // Clear tracking lists to see what migrate calls save on
        profileRepo.savedProfiles.clear()
        scenarioRepo.savedScenarios.clear()

        val useCase = MigrateGuestDataUseCase(wsRepo, wsContext, profileRepo, scenarioRepo)

        useCase("usr_abc")

        // Workspace should still be existing
        assertEquals("ws_existing", wsContext.activeWorkspaceId.value)

        // Profile and scenario should be re-saved / migrated
        assertEquals(1, profileRepo.savedProfiles.size)
        assertEquals(guestProfile, profileRepo.savedProfiles[0])

        assertEquals(1, scenarioRepo.savedScenarios.size)
        assertEquals(guestScenario.id, scenarioRepo.savedScenarios[0].id)
    }
}
