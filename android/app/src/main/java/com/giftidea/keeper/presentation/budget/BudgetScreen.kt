package com.giftidea.keeper.presentation.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.ui.*

@Composable
fun BudgetScreen(state: BudgetUiState, onBack: () -> Unit, onRetry: () -> Unit, onOpen: (BudgetRow) -> Unit, onEdit: (BudgetRow) -> Unit, onDraft: (BudgetEditorUiState) -> Unit, onStep: (Int) -> Unit, onSave: () -> Unit, onClose: () -> Unit, onDestination: (Int) -> Unit) {
    DecoratedBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding().widthIn(max = 720.dp).align(Alignment.TopCenter).padding(horizontal = 20.dp)) {
            ScreenHeader(stringResource(R.string.birthday_budgets), onBack = onBack)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                item { NotebookCard(paper = true) { Text(stringResource(R.string.next_12_months), style = MaterialTheme.typography.labelSmall); Text(moneyLabel(state.total.knownMinor), style = MaterialTheme.typography.displaySmall); Text(stringResource(R.string.known_planned_label), style = MaterialTheme.typography.bodyMedium); if (state.total.unknownCount > 0) Text(pluralStringResource(R.plurals.unpriced_ideas, state.total.unknownCount, state.total.unknownCount)) } }
                if (state.setLimits > 0 && state.plannedCount > 0) item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { StatCard(R.string.set_budgets, moneyLabel(state.setLimits), Modifier.weight(1f)); StatCard(R.string.planned_gifts, stringResource(R.string.integer_value, state.plannedCount), Modifier.weight(1f)) } }
                item { Text(stringResource(R.string.estimates_hint), style = MaterialTheme.typography.bodyMedium) }
                if (state.loading) item { Text(stringResource(R.string.loading_notebook)) }
                else if (state.error) item { EmptyStateCard(R.string.read_error, R.string.retry, onRetry) }
                else if (state.rows.isEmpty()) item { EmptyStateCard(R.string.no_budgets, R.string.people, { onDestination(0) }) }
                items(state.rows, key = { "${it.event.person.id}:${it.event.date}" }) { row ->
                    NotebookCard {
                        Text(row.event.person.name, style = MaterialTheme.typography.titleLarge)
                        Text(dateLabel(row.event.date))
                        if (row.count == 0) Text(stringResource(R.string.no_gifts_planned))
                        BudgetMeter(row.totals)
                        SecondaryButton(R.string.edit_budget, { onEdit(row) }, Modifier.fillMaxWidth())
                        LabelButton(R.string.open_ideas, { onOpen(row) })
                    }
                }
            }
            PeopleNavigation(2, onDestination)
        }
    }
    state.editor?.let { BudgetEditorSheet(it, onDraft, onStep, onSave, onClose) }
}
