package com.example.revlens.ui.screens.auth

import com.example.revlens.core.platform.InMemorySecureStorage
import com.example.revlens.data.fake.FakeAuthRepository
import com.example.revlens.domain.session.DefaultSessionManager
import com.example.revlens.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LoginViewModelTest {

    @Test
    fun testInitialState() {
        val repo = FakeAuthRepository(InMemorySecureStorage())
        val session = DefaultSessionManager()
        val vm = LoginViewModel(LoginUseCase(repo, session), session)

        val s = vm.state.value
        assertEquals("", s.email)
        assertEquals("", s.password)
        assertFalse(s.isLoading)
        assertFalse(s.canSubmit)
    }

    @Test
    fun testEmailValidationFailure() = runTest {
        val repo = FakeAuthRepository(InMemorySecureStorage())
        val session = DefaultSessionManager()
        val vm = LoginViewModel(LoginUseCase(repo, session), session)

        vm.onEvent(LoginEvent.EmailChanged("not-an-email"))
        vm.onEvent(LoginEvent.PasswordChanged("secret123"))
        vm.onEvent(LoginEvent.Submit)

        val s = vm.state.value
        assertNotNull(s.emailError)
        assertFalse(s.isLoading)
    }

    @Test
    fun testLoginSuccess() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeAuthRepository(InMemorySecureStorage())
        val session = DefaultSessionManager()
        val vm = LoginViewModel(LoginUseCase(repo, session), session)

        vm.onEvent(LoginEvent.EmailChanged("alex@revlens.com"))
        vm.onEvent(LoginEvent.PasswordChanged("password123"))
        assertTrue(vm.state.value.canSubmit)

        vm.onEvent(LoginEvent.Submit)

        assertFalse(vm.state.value.isLoading)
        assertEquals(null, vm.state.value.emailError)
        assertEquals(null, vm.state.value.passwordError)
    }

    @Test
    fun testLoginFailureEmitsEffect() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeAuthRepository(InMemorySecureStorage())
        val session = DefaultSessionManager()
        val vm = LoginViewModel(LoginUseCase(repo, session), session)

        var emittedEffect: LoginEffect? = null
        val job = backgroundScope.launch {
            vm.effects.collect { emittedEffect = it }
        }

        vm.onEvent(LoginEvent.EmailChanged("alex@revlens.com"))
        vm.onEvent(LoginEvent.PasswordChanged("wrongpass"))
        vm.onEvent(LoginEvent.Submit)

        assertNotNull(emittedEffect)
        assertTrue(emittedEffect is LoginEffect.ShowMessage)
        job.cancel()
    }
}
