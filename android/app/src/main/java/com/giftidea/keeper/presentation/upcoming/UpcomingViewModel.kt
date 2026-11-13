package com.giftidea.keeper.presentation.upcoming

import androidx.lifecycle.viewModelScope
import com.giftidea.keeper.core.navigation.Screen
import com.giftidea.keeper.domain.model.*
import com.giftidea.keeper.domain.repository.NotebookRepository
import com.giftidea.keeper.domain.usecase.NotebookRules
import com.giftidea.keeper.presentation.app.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UpcomingViewModel(repository: NotebookRepository) : NotebookViewModel(repository) {
    private val mutableState = MutableStateFlow(UpcomingUiState())
    override val state = mutableState.asStateFlow()
    init { observe(::render) }
    private fun render(source: NotebookState) {
        mutableState.update { current -> current.copy(loading = source.loading, error = source.error, events = source.notebook?.let { NotebookRules.events(it).filter { event -> current.yearRange || event.days <= 30 } }.orEmpty(), reminders = source.notebook?.let { NotebookRules.due(it) }.orEmpty(), notificationsAvailable = notebook.notificationsAvailable()) }
    }
    fun range(year: Boolean) { mutableState.update { it.copy(yearRange = year) }; render(notebook.state.value) }
    fun open(event: BirthdayEvent) { emit(UiEvent.Navigate(Screen.Game(event.person.id, event.date))) }
    fun seen(key: String) { viewModelScope.launch { val saved = notebook.change { it.copy(seenReminders = it.seenReminders + key) }; mutableState.update { it.copy(writeError = !saved) } } }
    override fun resume() { super.resume(); render(notebook.state.value) }
}
