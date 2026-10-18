package com.revlens.app.domain.calc

import com.revlens.app.data.DefaultAppLockRepository
import com.revlens.app.domain.model.LockTimeout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppLockTest {

    @Test
    fun testDefaultStateUnlocked() = runTest {
        val repo = DefaultAppLockRepository()
        val prefs = repo.preferencesFlow.first()
        assertFalse(prefs.isBiometricEnabled)
        assertFalse(prefs.isLocked)
        assertEquals(LockTimeout.ONE_MINUTE, prefs.timeout)
    }

    @Test
    fun testEnableBiometricsAndLockTimeout() = runTest {
        val repo = DefaultAppLockRepository()
        repo.setBiometricEnabled(true)
        repo.setTimeout(LockTimeout.FIVE_MINUTES)

        val prefs = repo.preferencesFlow.first()
        assertTrue(prefs.isBiometricEnabled)
        assertEquals(LockTimeout.FIVE_MINUTES, prefs.timeout)

        // Record background at t = 1000
        repo.recordBackground(1000L)

        // Check after 2 minutes (120,000 ms) -> should NOT lock yet because timeout is 5 min
        val lockedEarly = repo.checkAndApplyTimeout(1000L + 120_000L)
        assertFalse(lockedEarly)

        // Check after 6 minutes (360,000 ms) -> should lock!
        val lockedLate = repo.checkAndApplyTimeout(1000L + 360_000L)
        assertTrue(lockedLate)
        assertTrue(repo.preferencesFlow.first().isLocked)

        // Manual unlock
        repo.setLocked(false)
        assertFalse(repo.preferencesFlow.first().isLocked)
    }

    @Test
    fun testImmediateLock() = runTest {
        val repo = DefaultAppLockRepository()
        repo.setBiometricEnabled(true)
        repo.setTimeout(LockTimeout.IMMEDIATE)
        repo.recordBackground(5000L)

        val locked = repo.checkAndApplyTimeout(5001L)
        assertTrue(locked)
        assertTrue(repo.preferencesFlow.first().isLocked)
    }
}
