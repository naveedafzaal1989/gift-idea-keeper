package com.giftidea.keeper.presentation.budget

import androidx.lifecycle.viewModelScope
import com.giftidea.keeper.core.navigation.Screen
import com.giftidea.keeper.domain.model.*
import com.giftidea.keeper.domain.repository.NotebookRepository
import com.giftidea.keeper.domain.usecase.NotebookRules
import com.giftidea.keeper.presentation.app.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BudgetViewModel(repository: NotebookRepository) : NotebookViewModel(repository) {
    private val mutableState = MutableStateFlow(BudgetUiState())
    override val state = mutableState.asStateFlow()
    init {
        observe { source ->
            val book = source.notebook
            val rows = book?.let { NotebookRules.budgetEvents(it).map { event -> val plan = book.plans.find { it.personId == event.person.id && it.eventDate == event.date }; BudgetRow(event, NotebookRules.totals(book.ideas, plan?.ideaIds.orEmpty(), plan?.limitMinor), plan?.ideaIds?.size ?: 0) } }.orEmpty()
            mutableState.update { it.copy(loading = source.loading, error = source.error, rows = rows, total = PlanTotals(rows.sumOf { row -> row.totals.knownMinor }, rows.sumOf { row -> row.totals.unknownCount }), setLimits = rows.sumOf { row -> row.totals.limitMinor ?: 0 }, plannedCount = rows.sumOf { row -> row.count }) }
        }
    }
    fun open(row: BudgetRow) { emit(UiEvent.Navigate(Screen.Game(row.event.person.id, row.event.date))) }
    fun edit(row: BudgetRow) { mutableState.update { it.copy(editor = BudgetEditorUiState(row.event.person.id, row.event.date, row.totals.limitMinor != null, NotebookRules.amountInput(row.totals.limitMinor ?: 0), canDecreaseAmount = (row.totals.limitMinor ?: 0) > 0, canIncreaseAmount = (row.totals.limitMinor ?: 0) < 500000)) } }
    fun draft(editor: BudgetEditorUiState) { if (state.value.editor?.saving != true) { val amount = NotebookRules.money(editor.amount) ?: 0L; mutableState.update { it.copy(editor = editor.copy(canDecreaseAmount = amount > 0L, canIncreaseAmount = amount < 500000L)) } } }
    fun step(delta: Int) { state.value.editor?.let { draft(it.copy(amount = NotebookRules.amountInput(((NotebookRules.money(it.amount) ?: 0) + delta * 500L).coerceIn(0, 500000)), error = false)) } }
    fun close() { if (state.value.editor?.saving != true) mutableState.update { it.copy(editor = null) } }
    fun save() {
        val editor = state.value.editor ?: return
        if (editor.saving) return
        val amount = if (editor.enabled) NotebookRules.money(editor.amount) else null
        if (editor.enabled && amount == null) { draft(editor.copy(error = true)); return }
        mutableState.update { it.copy(editor = editor.copy(saving = true)) }
        viewModelScope.launch {
            val saved = notebook.change { book ->
                val existing = book.plans.find { it.personId == editor.personId && it.eventDate == editor.eventDate }
                book.copy(plans = book.plans.filterNot { it.personId == editor.personId && it.eventDate == editor.eventDate } + BirthdayPlan(editor.personId, editor.eventDate, existing?.ideaIds.orEmpty(), amount))
            }
            mutableState.update { it.copy(editor = if (saved) null else editor.copy(storageError = true)) }
            if (saved) haptic()
        }
    }
}
