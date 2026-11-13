package com.giftidea.keeper.presentation.settings

import androidx.lifecycle.viewModelScope
import com.giftidea.keeper.domain.model.NotebookSettings
import com.giftidea.keeper.domain.repository.NotebookRepository
import com.giftidea.keeper.presentation.app.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(repository: NotebookRepository) : NotebookViewModel(repository) {
    private val mutableState = MutableStateFlow(SettingsUiState(version = repository.appVersion()))
    override val state = mutableState.asStateFlow()
    private var dirty = false
    init {
        observe { source ->
            mutableState.update { it.copy(available = notebook.notificationsAvailable()) }
            val settings = source.notebook?.settings
            if (!dirty && settings != null) mutableState.update { it.copy(reminders = settings.reminders, leadDays = settings.leadDays, haptics = settings.haptics) }
        }
    }
    override fun resume() { super.resume(); mutableState.update { it.copy(available = notebook.notificationsAvailable()) } }
    fun reminder(enabled: Boolean) { if (state.value.saving) return; dirty = true; mutableState.update { it.copy(reminders = enabled, saved = false) }; if (enabled && !notebook.notificationsAvailable()) emit(UiEvent.Permission) }
    fun lead(delta: Int) { if (state.value.saving) return; dirty = true; mutableState.update { it.copy(leadDays = (it.leadDays + delta).coerceIn(0, 30), saved = false) } }
    fun haptics(enabled: Boolean) { if (state.value.saving) return; dirty = true; mutableState.update { it.copy(haptics = enabled, saved = false) } }
    fun save() {
        if (state.value.saving) return
        val settings = NotebookSettings(state.value.reminders, state.value.leadDays, state.value.haptics)
        mutableState.update { it.copy(saving = true, saved = false, error = false) }
        viewModelScope.launch {
            val saved = notebook.change { it.copy(settings = settings) }
            if (saved) { dirty = false; haptic() }
            mutableState.update { it.copy(saving = false, saved = saved, error = !saved, available = notebook.notificationsAvailable()) }
        }
    }
    fun confirmClear(value: Boolean) { if (!state.value.saving) mutableState.update { it.copy(confirmClear = value) } }
    fun clear() {
        if (state.value.saving) return
        mutableState.update { it.copy(saving = true) }
        viewModelScope.launch {
            if (notebook.clear()) { dirty = false; mutableState.update { SettingsUiState(version = it.version) }; emit(UiEvent.Menu) }
            else mutableState.update { it.copy(saving = false, error = true, confirmClear = false) }
        }
    }
    fun notificationSettings() { emit(UiEvent.NotificationSettings) }
}
