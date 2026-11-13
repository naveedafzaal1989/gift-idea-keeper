package com.giftidea.keeper.presentation.game

import androidx.lifecycle.viewModelScope
import com.giftidea.keeper.R
import com.giftidea.keeper.core.navigation.Screen
import com.giftidea.keeper.domain.model.*
import com.giftidea.keeper.domain.repository.NotebookRepository
import com.giftidea.keeper.domain.usecase.NotebookRules
import com.giftidea.keeper.presentation.app.*
import com.giftidea.keeper.presentation.dialog.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class GameViewModel(repository: NotebookRepository, private val personId: String, private val eventDate: String) : NotebookViewModel(repository) {
    private val mutableState = MutableStateFlow(GameUiState())
    override val state = mutableState.asStateFlow()
    private var initialized = false
    init { observe(::render) }
    private fun render(source: NotebookState) {
        val book = source.notebook
        val person = book?.people?.find { it.id == personId }
        val ideas = book?.ideas?.filter { it.personId == personId }.orEmpty()
        val plan = book?.plans?.find { it.personId == personId && it.eventDate == eventDate }
        val selected = (if (!initialized && book != null) plan?.ideaIds.orEmpty() else state.value.selected).intersect(ideas.map { it.id }.toSet())
        if (book != null) initialized = true
        mutableState.update { it.copy(loading = source.loading, error = source.error, person = person, event = person?.let { p -> NotebookRules.event(p, LocalDate.parse(eventDate)) }, ideas = ideas, pastGifts = book?.pastGifts?.filter { gift -> gift.personId == personId }?.sortedByDescending { gift -> gift.givenDate }.orEmpty(), selected = selected, savedSelection = plan?.ideaIds.orEmpty(), totals = NotebookRules.totals(ideas, selected, plan?.limitMinor)) }
    }
    fun toggle(id: String) {
        if (state.value.saving) return
        mutableState.update { current -> val selected = if (id in current.selected) current.selected - id else current.selected + id; current.copy(selected = selected, totals = NotebookRules.totals(current.ideas, selected, current.totals.limitMinor)) }
    }
    fun tab(past: Boolean) { mutableState.update { it.copy(pastTab = past) } }
    fun addIdea(id: String? = null) { emit(UiEvent.Navigate(Screen.AddIdea(personId, id, UUID.randomUUID().toString()))) }
    fun copyLink(idea: GiftIdea) { emit(UiEvent.Copy(idea.link)); mutableState.update { it.copy(message = R.string.link_copied) } }
    fun savePlan() {
        val current = state.value
        val person = current.person ?: return
        if (current.saving) return
        if (current.selected.isEmpty()) { emit(UiEvent.Result(PlanResult(ResultKind.NeedsChanges, person.name, eventDate))); return }
        mutableState.update { it.copy(saving = true) }
        viewModelScope.launch {
            var result = PlanResult(ResultKind.StorageError, person.name, eventDate)
            val saved = notebook.change { book ->
                val existing = book.plans.find { it.personId == personId && it.eventDate == eventDate }
                val ideas = book.ideas.filter { it.personId == personId }
                require(current.selected.all { id -> ideas.any { it.id == id } })
                result = PlanResult(ResultKind.Saved, person.name, eventDate, ideas.filter { it.id in current.selected }.map { it.title }, NotebookRules.totals(ideas, current.selected, existing?.limitMinor))
                book.copy(plans = book.plans.filterNot { it.personId == personId && it.eventDate == eventDate } + BirthdayPlan(personId, eventDate, current.selected, existing?.limitMinor))
            }
            mutableState.update { it.copy(saving = false) }
            if (saved) haptic()
            emit(UiEvent.Result(if (saved) result else result.copy(kind = ResultKind.StorageError, titles = emptyList())))
        }
    }
    fun editPerson() { state.value.person?.let { person -> mutableState.update { it.copy(editor = PersonEditorUiState.from(person)) } } }
    fun editDraft(action: PersonEditorAction) { state.value.editor?.let { setDraft(it.change(action)) } }
    private fun setDraft(draft: PersonEditorUiState) { if (state.value.editor?.saving != true) mutableState.update { it.copy(editor = draft) } }
    fun closeEditor() { if (state.value.editor?.saving != true) mutableState.update { it.copy(editor = null) } }
    fun savePerson() {
        val draft = state.value.editor ?: return
        if (draft.saving) return
        if (draft.name.trim().length !in 1..40 || draft.relationship.trim().length > 30) { setDraft(draft.copy(error = R.string.invalid_person)); return }
        mutableState.update { it.copy(editor = draft.copy(saving = true)) }
        viewModelScope.launch {
            val saved = notebook.change { book -> book.copy(people = book.people.map { if (it.id == personId) Person(it.id, draft.name.trim(), draft.relationship.trim(), draft.month, draft.day) else it }) }
            mutableState.update { it.copy(editor = if (saved) null else draft.copy(error = R.string.storage_error), message = if (saved) R.string.birthday_edit_note else it.message) }
            if (saved) haptic()
        }
    }
    fun deletePerson() {
        val draft = state.value.editor ?: return
        if (!draft.confirmDelete) { setDraft(draft.copy(confirmDelete = true)); return }
        if (draft.saving) return
        mutableState.update { it.copy(editor = draft.copy(saving = true)) }
        viewModelScope.launch {
            if (notebook.change { NotebookRules.removePerson(it, personId) }) { mutableState.update { it.copy(editor = null) }; emit(UiEvent.Menu) }
            else mutableState.update { it.copy(editor = draft.copy(error = R.string.storage_error)) }
        }
    }
    fun clearPlan(confirm: Boolean) {
        if (!confirm) { mutableState.update { it.copy(confirmClear = true) }; return }
        mutableState.update { it.copy(confirmClear = false, saving = true) }
        viewModelScope.launch {
            val saved = notebook.change { book -> book.copy(plans = book.plans.map { if (it.personId == personId && it.eventDate == eventDate) it.copy(ideaIds = emptySet()) else it }) }
            mutableState.update { it.copy(saving = false, selected = if (saved) emptySet() else it.selected, message = if (saved) R.string.plan_cleared else R.string.storage_error) }
            render(notebook.state.value)
        }
    }
    fun dismissClear() { mutableState.update { it.copy(confirmClear = false) } }
    fun markGiven(idea: GiftIdea) { mutableState.update { it.copy(given = MarkGivenUiState(idea.id, idea.title)) } }
    fun givenDraft(draft: MarkGivenUiState) { if (state.value.given?.saving != true) { val amount = NotebookRules.money(draft.amount) ?: 0L; mutableState.update { it.copy(given = draft.copy(canDecreaseAmount = amount > 0L, canIncreaseAmount = amount < 500000L)) } } }
    fun givenStep(delta: Int) { state.value.given?.let { draft -> givenDraft(draft.copy(amount = NotebookRules.amountInput(((NotebookRules.money(draft.amount) ?: 0) + delta * 500L).coerceIn(0, 500000)), error = null)) } }
    fun dismissGiven() { if (state.value.given?.saving != true) mutableState.update { it.copy(given = null) } }
    fun recordGift() {
        val draft = state.value.given ?: return
        if (draft.saving) return
        val date = runCatching { LocalDate.parse(draft.date) }.getOrNull()
        val amount = if (draft.amountEnabled) NotebookRules.money(draft.amount) else null
        if (date == null || date > LocalDate.now()) { givenDraft(draft.copy(error = R.string.invalid_date)); return }
        if (draft.amountEnabled && amount == null) { givenDraft(draft.copy(error = R.string.invalid_amount)); return }
        mutableState.update { it.copy(given = draft.copy(saving = true)) }
        viewModelScope.launch {
            var result: PlanResult? = null
            val saved = notebook.change { book ->
                val idea = book.ideas.first { it.id == draft.ideaId }
                val gift = PastGift(UUID.randomUUID().toString(), personId, idea.title, idea.note, draft.date, amount)
                result = PlanResult(ResultKind.GiftRecorded, state.value.person?.name.orEmpty(), draft.date, listOf(idea.title), PlanTotals(amount ?: 0, if (amount == null) 1 else 0))
                NotebookRules.removeIdea(book, idea.id).copy(pastGifts = book.pastGifts + gift)
            }
            if (saved) { mutableState.update { it.copy(given = null) }; haptic(); result?.let { emit(UiEvent.Result(it)) } }
            else mutableState.update { it.copy(given = draft.copy(error = R.string.storage_error)) }
        }
    }
}
