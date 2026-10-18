package com.revlens.app.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidatorsTest {

    @Test
    fun testEmailValidation() {
        assertNotNull(Validators.email(""))
        assertNotNull(Validators.email("invalid-email"))
        assertNotNull(Validators.email("missing@tld"))
        assertNull(Validators.email("founder@example.com"))
        assertNull(Validators.email("test.user+tag@domain.co.in"))
    }

    @Test
    fun testWorkspaceNameValidation() {
        assertNotNull(Validators.workspaceName(""))
        assertNotNull(Validators.workspaceName("A"))
        assertNotNull(Validators.workspaceName("A".repeat(41)))
        assertNull(Validators.workspaceName("Acme Inc"))
        assertNull(Validators.workspaceName("SaaS Co"))
    }

    @Test
    fun testNumberValidation() {
        assertNotNull(Validators.positiveNumber("0"))
        assertNotNull(Validators.positiveNumber("-5"))
        assertNotNull(Validators.positiveNumber("abc"))
        assertNull(Validators.positiveNumber("10.5"))

        assertNull(Validators.nonNegativeNumber("0"))
        assertNotNull(Validators.nonNegativeNumber("-0.1"))
    }

    @Test
    fun testIdGenerator() {
        val generator = DefaultIdGenerator()
        val id = generator.generate("ws")
        assertTrue(id.startsWith("ws_"))
        assertEquals(19, id.length) // "ws_" (3) + 16 chars
    }
}
