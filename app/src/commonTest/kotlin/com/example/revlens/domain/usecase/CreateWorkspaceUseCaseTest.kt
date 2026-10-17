package com.example.revlens.domain.usecase

import com.example.revlens.core.util.DefaultIdGenerator
import com.example.revlens.data.DefaultWorkspaceRepository
import com.example.revlens.domain.context.DefaultWorkspaceContext
import com.example.revlens.domain.model.BusinessModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CreateWorkspaceUseCaseTest {

    @Test
    fun testCreateWorkspaceSuccess() = runTest {
        val repo = DefaultWorkspaceRepository(emptyList())
        val context = DefaultWorkspaceContext(null)
        val idGenerator = DefaultIdGenerator()
        val useCase = CreateWorkspaceUseCase(repo, context, idGenerator)

        val created = useCase(
            name = "SaaS Alpha",
            currency = "EUR",
            businessModel = BusinessModel.B2bSaas,
            timeZoneId = "Europe/Berlin",
            fiscalYearStartMonth = 4
        )

        assertNotNull(created.id)
        assertEquals("SaaS Alpha", created.name)
        assertEquals("EUR", created.currency)
        assertEquals(BusinessModel.B2bSaas, created.businessModel)
        assertEquals("Europe/Berlin", created.timeZoneId)
        assertEquals(4, created.fiscalYearStartMonth)

        // Verify active context was switched
        assertEquals(created.id, context.activeWorkspaceId.value)

        // Verify saved in repository
        val fetched = repo.getWorkspace(created.id)
        assertNotNull(fetched)
        assertEquals("SaaS Alpha", fetched.name)
    }
}
