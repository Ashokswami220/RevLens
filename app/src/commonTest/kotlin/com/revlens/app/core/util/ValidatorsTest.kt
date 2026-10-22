package com.revlens.app.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidatorsTest {

    @Test
    fun testWorkspaceNameValidation() {
        assertNotNull(Validators.workspaceName(""))
        assertNotNull(Validators.workspaceName("A"))
        assertNotNull(Validators.workspaceName("A".repeat(41)))
        assertNull(Validators.workspaceName("Acme Inc"))
        assertNull(Validators.workspaceName("SaaS Co"))
    }

    @Test
    fun testIdGenerator() {
        val generator = DefaultIdGenerator()
        val id = generator.generate("ws")
        assertTrue(id.startsWith("ws_"))
        assertEquals(19, id.length) // "ws_" (3) + 16 chars
    }
}
