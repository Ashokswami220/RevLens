package com.revlens.app.ui.screens.notifications

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() = runTest(testDispatcher) {
        val vm = NotificationSettingsViewModel()
        val s = vm.state.value
        assertEquals(NotificationSyncStatus.Synced, s.syncStatus)
        assertTrue(s.prefs.pushEnabled)
    }

    @Test
    fun testPreferenceChange() = runTest(testDispatcher) {
        val vm = NotificationSettingsViewModel()

        vm.onEvent(NotificationSettingsEvent.TogglePush(false))
        assertFalse(vm.state.value.prefs.pushEnabled)
        assertFalse(vm.state.value.isPushRegistered)

        vm.onEvent(NotificationSettingsEvent.ToggleEmail(false))
        assertFalse(vm.state.value.prefs.emailEnabled)

        vm.onEvent(NotificationSettingsEvent.UpdateChurnThreshold(15.0))
        assertEquals(15.0, vm.state.value.prefs.churnSpikeThresholdPercent)
    }
}
