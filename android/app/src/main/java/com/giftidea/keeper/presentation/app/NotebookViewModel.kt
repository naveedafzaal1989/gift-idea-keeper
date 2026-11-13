package com.giftidea.keeper.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftidea.keeper.domain.model.NotebookState
import com.giftidea.keeper.domain.repository.NotebookRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime

abstract class NotebookViewModel(protected val notebook: NotebookRepository) : ViewModel() {
    abstract val state: StateFlow<*>
    private val channel = Channel<UiEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    private var refresh: ((NotebookState) -> Unit)? = null
    private var dateJob: Job? = null
    private var visible = false

    protected fun observe(onChange: (NotebookState) -> Unit) {
        refresh = onChange
        viewModelScope.launch { notebook.state.collect { onChange(it) } }
    }

    protected fun emit(event: UiEvent) { channel.trySend(event) }
    protected fun haptic() { if (notebook.state.value.notebook?.settings?.haptics == true) emit(UiEvent.Haptic) }
    fun retry() { viewModelScope.launch { notebook.load() } }
    open fun resume() {
        if (visible) return
        visible = true
        refresh?.invoke(notebook.state.value)
        dateJob = viewModelScope.launch {
            notebook.refreshReminders()
            while (isActive) {
                val now = ZonedDateTime.now()
                val midnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
                val morning = now.toLocalDate().atTime(9, 0).atZone(now.zone)
                val nextRefresh = if (morning > now) morning else midnight
                delay(Duration.between(now, nextRefresh).toMillis().coerceAtLeast(1000L))
                refresh?.invoke(notebook.state.value)
                notebook.refreshReminders()
            }
        }
    }
    open fun pause() { visible = false; dateJob?.cancel(); dateJob = null }
}
