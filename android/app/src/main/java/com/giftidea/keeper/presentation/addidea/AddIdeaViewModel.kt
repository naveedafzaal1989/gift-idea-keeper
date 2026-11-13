package com.giftidea.keeper.presentation.addidea

import androidx.lifecycle.viewModelScope
import com.giftidea.keeper.R
import com.giftidea.keeper.domain.model.*
import com.giftidea.keeper.domain.repository.NotebookRepository
import com.giftidea.keeper.domain.usecase.NotebookRules
import com.giftidea.keeper.presentation.app.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AddIdeaViewModel(repository: NotebookRepository, private val personId: String, private val ideaId: String?) : NotebookViewModel(repository) {
    private val mutableState = MutableStateFlow(AddIdeaUiState(editing = ideaId != null))
    override val state = mutableState.asStateFlow()
    private val recordId = ideaId ?: UUID.randomUUID().toString()
    private var initialized = false
    init {
        observe { source ->
            val book = source.notebook
            if (book != null && !initialized) {
                initialized = true
                val person = book.people.find { it.id == personId }
                val idea = book.ideas.find { it.id == ideaId }
                mutableState.update { it.copy(loading = false, personName = person?.name.orEmpty(), title = idea?.title.orEmpty(), note = idea?.note.orEmpty(), estimateEnabled = idea?.estimateMinor != null, amount = NotebookRules.amountInput(idea?.estimateMinor ?: 0), link = idea?.link.orEmpty(), missing = person == null || (ideaId != null && idea == null), canDecreaseAmount = (idea?.estimateMinor ?: 0) > 0, canIncreaseAmount = (idea?.estimateMinor ?: 0) < 500000) }
            } else if (book == null) mutableState.update { it.copy(loading = source.loading, error = if (source.error) R.string.read_error else null) }
        }
    }
    fun edit(value: AddIdeaUiState) { if (!state.value.saving) { val amount = NotebookRules.money(value.amount) ?: 0L; mutableState.update { value.copy(dirty = true, error = null, invalidField = 0, canDecreaseAmount = amount > 0L, canIncreaseAmount = amount < 500000L) } } }
    fun step(delta: Int) { edit(state.value.copy(amount = NotebookRules.amountInput(((NotebookRules.money(state.value.amount) ?: 0L) + delta * 500L).coerceIn(0L, 500000L)))) }
    fun back() { if (state.value.saving) return; if (state.value.dirty) mutableState.update { it.copy(confirmDiscard = true) } else emit(UiEvent.Back) }
    fun keepEditing() { mutableState.update { it.copy(confirmDiscard = false, confirmDelete = false) } }
    fun discard() { mutableState.update { AddIdeaUiState(loading = false) }; emit(UiEvent.Back) }
    fun save() {
        val draft = state.value
        if (draft.saving) return
        val error = when {
            draft.title.trim().length !in 1..80 -> R.string.invalid_title to 1
            draft.note.trim().length > 160 -> R.string.invalid_note to 2
            draft.estimateEnabled && NotebookRules.money(draft.amount) == null -> R.string.invalid_amount to 3
            draft.link.trim().length > 512 || !NotebookRules.safeLink(draft.link.trim()) -> R.string.invalid_link to 4
            else -> null
        }
        if (error != null) { mutableState.update { it.copy(error = error.first, invalidField = error.second) }; return }
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val idea = GiftIdea(recordId, personId, draft.title.trim(), draft.note.trim(), if (draft.estimateEnabled) NotebookRules.money(draft.amount) else null, draft.link.trim())
            val saved = notebook.change { book -> require(book.people.any { it.id == personId }); book.copy(ideas = book.ideas.filterNot { it.id == recordId } + idea) }
            if (saved) { mutableState.update { AddIdeaUiState(loading = false) }; haptic(); emit(UiEvent.Back) }
            else mutableState.update { it.copy(saving = false, error = R.string.storage_error) }
        }
    }
    fun delete(confirm: Boolean) {
        if (!confirm) { mutableState.update { it.copy(confirmDelete = true) }; return }
        if (state.value.saving) return
        mutableState.update { it.copy(saving = true) }
        viewModelScope.launch {
            if (notebook.change { NotebookRules.removeIdea(it, recordId) }) { mutableState.update { AddIdeaUiState(loading = false) }; emit(UiEvent.Back) }
            else mutableState.update { it.copy(saving = false, confirmDelete = false, error = R.string.storage_error) }
        }
    }
}
