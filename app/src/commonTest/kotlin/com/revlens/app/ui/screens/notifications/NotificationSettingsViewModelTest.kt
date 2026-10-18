package com.revlens.app.ui.screens.notifications

import com.revlens.app.core.platform.LocalNotifier
import com.revlens.app.data.DefaultNotificationPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
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

    private val fakeNotifier = object : LocalNotifier {
        override fun notify(id: Int, title: String, message: String) {}
    }

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
        val repo = DefaultNotificationPreferencesRepository()
        val vm = NotificationSettingsViewModel(repo, fakeNotifier)
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(NotificationSyncStatus.Synced, s.syncStatus)
        assertTrue(s.prefs.pushEnabled)
    }

    @Test
    fun testPreferenceChangeTriggersDebouncedSync() = runTest(testDispatcher) {
        val repo = DefaultNotificationPreferencesRepository()
        val vm = NotificationSettingsViewModel(repo, fakeNotifier)
        advanceUntilIdle()

        // Toggle push
        vm.onEvent(NotificationSettingsEvent.TogglePush(false))

        // Immediately state is updated locally and status is Syncing
        assertEquals(false, vm.state.value.prefs.pushEnabled)
        assertEquals(NotificationSyncStatus.Syncing, vm.state.value.syncStatus)

        // Advance debounce time (500ms)
        advanceTimeBy(600)
        advanceUntilIdle()

        assertEquals(NotificationSyncStatus.Synced, vm.state.value.syncStatus)
        assertEquals(false, repo.preferencesFlow.first().pushEnabled)
    }

    @Test
    fun testMultipleRapidChangesDebounceCorrectly() = runTest(testDispatcher) {
        val repo = DefaultNotificationPreferencesRepository()
        val vm = NotificationSettingsViewModel(repo, fakeNotifier)
        advanceUntilIdle()

        vm.onEvent(NotificationSettingsEvent.ToggleEmail(false))
        vm.onEvent(NotificationSettingsEvent.ToggleGoalMilestones(false))
        vm.onEvent(NotificationSettingsEvent.UpdateChurnThreshold(15.0))

        assertEquals(NotificationSyncStatus.Syncing, vm.state.value.syncStatus)
        assertEquals(15.0, vm.state.value.prefs.churnSpikeThresholdPercent)

        advanceTimeBy(600)
        advanceUntilIdle()

        assertEquals(NotificationSyncStatus.Synced, vm.state.value.syncStatus)
        assertEquals(15.0, repo.preferencesFlow.first().churnSpikeThresholdPercent)
        assertFalse(repo.preferencesFlow.first().goalMilestones)
    }
}
