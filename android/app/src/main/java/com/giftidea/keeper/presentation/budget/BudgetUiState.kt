package com.giftidea.keeper.presentation.budget

import com.giftidea.keeper.domain.model.*

data class BudgetRow(val event: BirthdayEvent, val totals: PlanTotals, val count: Int)
data class BudgetEditorUiState(val personId: String, val eventDate: String, val enabled: Boolean, val amount: String, val error: Boolean = false, val storageError: Boolean = false, val saving: Boolean = false, val canDecreaseAmount: Boolean = false, val canIncreaseAmount: Boolean = true)
data class BudgetUiState(val loading: Boolean = true, val error: Boolean = false, val rows: List<BudgetRow> = emptyList(), val total: PlanTotals = PlanTotals(), val setLimits: Long = 0, val plannedCount: Int = 0, val editor: BudgetEditorUiState? = null)
