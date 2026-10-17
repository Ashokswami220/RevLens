package com.example.revlens.domain.usecase

import com.example.revlens.domain.model.SessionState
import com.example.revlens.domain.model.UserAccount
import com.example.revlens.domain.model.Workspace
import com.example.revlens.model.BigDecimal
import com.example.revlens.model.BusinessProfile
import com.example.revlens.ui.navigation.Gate
import com.example.revlens.ui.navigation.PublicStart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResolveGateUseCaseTest {

    private val useCase = ResolveGateUseCase()

    private val sampleWorkspace = Workspace(
        id = "ws_1",
        name = "Acme Inc",
        currency = "USD"
    )

    private val sampleProfile = BusinessProfile(
        monthlyChurnRate = 2.5,
        monthlyGrowthRate = 5.0,
        cac = BigDecimal("150.00"),
        fixedCosts = BigDecimal("5000.00"),
        variableCostPerCustomer = BigDecimal("10.00"),
        plans = emptyList(),
        currencyCode = "USD"
    )

    @Test
    fun testSignedOutRoutes() {
        val webGate = useCase(
            session = SessionState.SignedOut,
            workspace = null,
            baseline = null,
            isWeb = true,
            introSeen = false
        )
        assertEquals(Gate.Public(PublicStart.Landing), webGate)

        val introGate = useCase(
            session = SessionState.SignedOut,
            workspace = null,
            baseline = null,
            isWeb = false,
            introSeen = false
        )
        assertEquals(Gate.Public(PublicStart.Intro), introGate)

        val loginGate = useCase(
            session = SessionState.SignedOut,
            workspace = null,
            baseline = null,
            isWeb = false,
            introSeen = true
        )
        assertEquals(Gate.Public(PublicStart.Login), loginGate)
    }

    @Test
    fun testGuestFlow() {
        val noWs = useCase(
            session = SessionState.Guest,
            workspace = null,
            baseline = null
        )
        assertEquals(Gate.CreateWorkspace, noWs)

        val noBaseline = useCase(
            session = SessionState.Guest,
            workspace = sampleWorkspace,
            baseline = null
        )
        assertEquals(Gate.Onboarding, noBaseline)

        val complete = useCase(
            session = SessionState.Guest,
            workspace = sampleWorkspace,
            baseline = sampleProfile
        )
        assertEquals(Gate.Main, complete)
    }

    @Test
    fun testAuthenticatedFlow() {
        val user = UserAccount(id = "u1", name = "Founder", email = "founder@acme.com")
        val session = SessionState.Authenticated(user)

        val noWs = useCase(session = session, workspace = null, baseline = null)
        assertEquals(Gate.CreateWorkspace, noWs)

        val noBaseline = useCase(session = session, workspace = sampleWorkspace, baseline = null)
        assertEquals(Gate.Onboarding, noBaseline)

        val main = useCase(session = session, workspace = sampleWorkspace, baseline = sampleProfile)
        assertEquals(Gate.Main, main)
    }
}
