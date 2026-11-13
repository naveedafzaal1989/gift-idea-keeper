package com.giftidea.keeper.presentation.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.ui.*
import com.giftidea.keeper.domain.model.GiftIdea
import com.giftidea.keeper.presentation.dialog.*

@Composable
fun GameScreen(state: GameUiState, onBack: () -> Unit, onMenu: () -> Unit, onRetry: () -> Unit, onEditPerson: () -> Unit, onTab: (Boolean) -> Unit, onToggle: (String) -> Unit, onAddIdea: (String?) -> Unit, onCopy: (GiftIdea) -> Unit, onGiven: (GiftIdea) -> Unit, onSave: () -> Unit, onClear: (Boolean) -> Unit, onDismissClear: () -> Unit, onDraft: (PersonEditorAction) -> Unit, onSavePerson: () -> Unit, onCloseEditor: () -> Unit, onDeletePerson: () -> Unit, onGivenDraft: (MarkGivenUiState) -> Unit, onGivenStep: (Int) -> Unit, onRecord: () -> Unit, onCloseGiven: () -> Unit) {
    DecoratedBackground(BackgroundVariant.Detail) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val compact = maxHeight < 520.dp || LocalDensity.current.fontScale > 1.3f
            Column(Modifier.fillMaxSize().widthIn(max = 720.dp).align(Alignment.TopCenter).padding(horizontal = 20.dp)) {
                ScreenHeader(state.person?.name ?: stringResource(R.string.person_detail), onBack = onBack, action = if (state.person != null) R.string.edit else null, onAction = onEditPerson)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
                    if (state.loading) item { Text(stringResource(R.string.loading_notebook)) }
                    else if (state.error) item { EmptyStateCard(R.string.read_error, R.string.retry, onRetry) }
                    else if (state.person == null) item { EmptyStateCard(R.string.person_missing, R.string.menu, onMenu) }
                    else {
                        state.event?.let { event -> item { BirthdayEventCard(event) } }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SecondaryButton(R.string.ideas_tab, { onTab(false) }, Modifier.weight(1f).semantics { selected = !state.pastTab }, state.pastTab)
                                SecondaryButton(R.string.past_gifts_tab, { onTab(true) }, Modifier.weight(1f).semantics { selected = state.pastTab }, !state.pastTab)
                            }
                        }
                        state.message?.let { message -> item { SavedFeedback(message) } }
                        if (state.pastTab) {
                            if (state.pastGifts.isEmpty()) item { NotebookCard { Text(stringResource(R.string.empty_history)) } }
                            items(state.pastGifts, key = { it.id }) { PastGiftCard(it) }
                        } else {
                            if (state.ideas.isEmpty()) item { EmptyStateCard(R.string.empty_ideas, R.string.add_idea, { onAddIdea(null) }) }
                            itemsIndexed(state.ideas, key = { _, idea -> idea.id }) { index, idea -> IdeaCard(idea, idea.id in state.selected, index, { onToggle(idea.id) }, { onAddIdea(idea.id) }, { onCopy(idea) }, { onGiven(idea) }, !state.saving) }
                        }
                        if (state.savedSelection.isNotEmpty()) item { LabelButton(R.string.clear_plan, { onClear(false) }, enabled = !state.saving) }
                        if (compact) item { PlanActions(state, onAddIdea, onSave) }
                    }
                }
                if (!compact && state.person != null && !state.error) PlanActions(state, onAddIdea, onSave)
            }
        }
    }
    state.editor?.let { PersonEditorDialog(it, onDraft, onSavePerson, onCloseEditor, onDeletePerson) }
    state.given?.let { MarkGivenDialog(it, onGivenDraft, onGivenStep, onRecord, onCloseGiven) }
    if (state.confirmClear) ConfirmActionDialog(R.string.clear_plan, stringResource(R.string.clear_plan_message), R.string.clear_plan, { onClear(true) }, onDismissClear)
}

@Composable
private fun PlanActions(state: GameUiState, onAddIdea: (String?) -> Unit, onSave: () -> Unit) {
    NotebookCard(Modifier.padding(vertical = 8.dp)) {
        BudgetMeter(state.totals)
        Text(stringResource(R.string.select_ideas_hint), style = MaterialTheme.typography.bodyMedium)
        SecondaryButton(R.string.add_idea, { onAddIdea(null) }, Modifier.fillMaxWidth(), !state.saving)
        PrimaryButton(R.string.save_plan, onSave, loading = state.saving)
    }
}
