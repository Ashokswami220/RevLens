package com.revlens.app.core.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    fun asString(): String = when (this) {
        is DynamicString -> value
    }
}

sealed interface Loadable<out T> {
    data object Loading : Loadable<Nothing>
    data class Content<T>(val data: T) : Loadable<T>
    data class Failed(val error: AppError) : Loadable<Nothing>
}

abstract class MviViewModel<S, E, F>(initialState: S) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<F>(Channel.BUFFERED)
    val effects: Flow<F> = _effects.receiveAsFlow()

    protected fun update(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }

    protected fun emit(effect: F) {
        _effects.trySend(effect)
    }

    abstract fun onEvent(event: E)
}
