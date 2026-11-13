package com.giftidea.keeper.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftidea.keeper.R
import com.giftidea.keeper.core.navigation.Screen
import com.giftidea.keeper.domain.model.PlanResult
import com.giftidea.keeper.domain.repository.NotebookRepository
import com.giftidea.keeper.domain.usecase.NotebookRules
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(private val notebook: NotebookRepository) : ViewModel() {
    private val mutableScreen = MutableStateFlow<Screen>(Screen.Splash)
    val screen = mutableScreen.asStateFlow()
    private val mutableState = MutableStateFlow(AppUiState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            notebook.load()
            notebook.state.collect { if (it.notebook != null && screen.value != Screen.Splash) openPending() }
        }
    }
    fun finishSplash() {
        if (screen.value != Screen.Splash) return
        mutableScreen.value = Screen.Menu
        openPending()
    }
    fun navigate(next: Screen) {
        if (next == screen.value || screen.value == Screen.Splash) return
        if (next == Screen.Menu) { menu(); return }
        if (next == Screen.Upcoming || next == Screen.Budget) {
            mutableState.update { it.copy(backStack = listOf(Screen.Menu)) }
        } else mutableState.update { it.copy(backStack = it.backStack + screen.value) }
        mutableScreen.value = next
    }
    fun back() {
        val previous = state.value.backStack.lastOrNull() ?: return
        mutableState.update { it.copy(backStack = it.backStack.dropLast(1)) }
        mutableScreen.value = previous
    }
    fun menu() {
        mutableState.update { it.copy(backStack = emptyList()) }
        mutableScreen.value = Screen.Menu
    }
    fun result(result: PlanResult) {
        val id = UUID.randomUUID().toString()
        mutableState.update { it.copy(results = (it.results + (id to result)).entries.toList().takeLast(20).associate { entry -> entry.toPair() }) }
        navigate(Screen.GameOver(id))
    }
    fun handleNotification(person: String?, date: String?) {
        if (person == null) return
        mutableState.update { it.copy(pendingPerson = person, pendingDate = date) }
        if (screen.value != Screen.Splash) openPending()
    }
    private fun openPending() {
        val id = state.value.pendingPerson ?: return
        val book = notebook.state.value.notebook ?: return
        val person = book.people.find { it.id == id }
        val requestedDate = state.value.pendingDate
        mutableState.update { it.copy(pendingPerson = null, pendingDate = null) }
        menu()
        if (person == null) mutableState.update { it.copy(notice = R.string.person_missing) }
        else {
            val next = NotebookRules.next(person).date
            val valid = requestedDate == next || book.plans.any { it.personId == id && it.eventDate == requestedDate }
            navigate(Screen.Game(id, if (valid && requestedDate != null) requestedDate else next))
        }
    }
    fun dismissNotice() { mutableState.update { it.copy(notice = null) } }
    fun refreshPlatform() { viewModelScope.launch { notebook.refreshReminders() } }
}
