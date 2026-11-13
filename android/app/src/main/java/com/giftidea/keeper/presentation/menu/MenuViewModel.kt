package com.giftidea.keeper.presentation.menu

import androidx.lifecycle.viewModelScope
import com.giftidea.keeper.R
import com.giftidea.keeper.core.navigation.Screen
import com.giftidea.keeper.domain.model.*
import com.giftidea.keeper.domain.repository.NotebookRepository
import com.giftidea.keeper.domain.usecase.NotebookRules
import com.giftidea.keeper.presentation.app.*
import com.giftidea.keeper.presentation.dialog.PersonEditorUiState
import com.giftidea.keeper.presentation.dialog.PersonEditorAction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MenuViewModel(repository: NotebookRepository) : NotebookViewModel(repository) {
    private val mutableState = MutableStateFlow(MenuUiState())
    override val state = mutableState.asStateFlow()
    private var playAfterCreate = false
    init { observe(::render) }
    private fun render(source: NotebookState) {
        val book = source.notebook
        val events = book?.let { NotebookRules.events(it) }.orEmpty()
        val query = state.value.search.trim()
        val summaries = events.filter { query.isEmpty() || it.person.name.contains(query, true) || it.person.relationship.contains(query, true) }.map { event ->
            val plan = book?.plans?.find { it.personId == event.person.id && it.eventDate == event.date }
            PersonSummary(event, book?.ideas?.count { it.personId == event.person.id } ?: 0, NotebookRules.totals(book?.ideas.orEmpty(), plan?.ideaIds.orEmpty(), plan?.limitMinor).knownMinor)
        }
        mutableState.update { it.copy(loading = source.loading, error = source.error, people = summaries, nextBirthday = events.firstOrNull(), birthdaysSoon = events.count { event -> event.days <= 30 }, ideaCount = book?.ideas?.size ?: 0, sampleNotice = book?.sampleDismissed == false) }
    }
    fun search(value: String) { mutableState.update { it.copy(search = value) }; render(notebook.state.value) }
    fun open(event: BirthdayEvent) { emit(UiEvent.Navigate(Screen.Game(event.person.id, event.date))) }
    fun play() { val event = state.value.nextBirthday; if (event != null) open(event) else { playAfterCreate = true; addPerson() } }
    fun addPerson() { mutableState.update { it.copy(editor = PersonEditorUiState()) } }
    fun editDraft(action: PersonEditorAction) { state.value.editor?.let { setDraft(it.change(action)) } }
    private fun setDraft(draft: PersonEditorUiState) { if (state.value.editor?.saving != true) mutableState.update { it.copy(editor = draft) } }
    fun closeEditor() { if (state.value.editor?.saving != true) { playAfterCreate = false; mutableState.update { it.copy(editor = null) } } }
    fun savePerson() {
        val draft = state.value.editor ?: return
        if (draft.saving) return
        if (draft.name.trim().length !in 1..40 || draft.relationship.trim().length > 30) { setDraft(draft.copy(error = R.string.invalid_person)); return }
        val person = Person(draft.id ?: UUID.randomUUID().toString(), draft.name.trim(), draft.relationship.trim(), draft.month, draft.day)
        mutableState.update { it.copy(editor = draft.copy(saving = true, error = null)) }
        viewModelScope.launch {
            val saved = notebook.change { book -> book.copy(people = book.people.filterNot { it.id == person.id } + person) }
            if (saved) {
                mutableState.update { it.copy(editor = null) }; haptic()
                if (playAfterCreate) { playAfterCreate = false; open(NotebookRules.next(person)) }
            } else mutableState.update { it.copy(editor = draft.copy(error = R.string.storage_error)) }
        }
    }
    fun dismissSamples() { viewModelScope.launch { val saved = notebook.change { it.copy(sampleDismissed = true) }; mutableState.update { it.copy(writeError = !saved) } } }
    fun destination(index: Int) { emit(UiEvent.Navigate(if (index == 1) Screen.Upcoming else Screen.Budget)) }
    fun settings() { emit(UiEvent.Navigate(Screen.Settings)) }
}
