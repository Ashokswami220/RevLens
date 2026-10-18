package com.revlens.app.domain.calc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PasswordStrengthTest {

    @Test
    fun testEmptyPassword() {
        val eval = PasswordStrength.evaluate("")
        assertEquals(0, eval.score)
        assertEquals("Empty", eval.label)
    }

    @Test
    fun testCommonPassword() {
        val eval = PasswordStrength.evaluate("password123")
        assertEquals(1, eval.score)
        assertTrue(eval.feedback != null)
    }

    @Test
    fun testShortPassword() {
        val eval = PasswordStrength.evaluate("aB1!")
        assertTrue(eval.score <= 1)
        assertEquals("Weak", eval.label)
    }

    @Test
    fun testModeratePassword() {
        val eval = PasswordStrength.evaluate("Str0ngPass")
        assertTrue(eval.score >= 2)
    }

    @Test
    fun testVeryStrongPassword() {
        val eval = PasswordStrength.evaluate("Super#Secure&Password2026!")
        assertEquals(4, eval.score)
        assertEquals("Strong", eval.label)
    }
}
