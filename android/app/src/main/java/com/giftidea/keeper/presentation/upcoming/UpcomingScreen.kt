package com.giftidea.keeper.presentation.upcoming

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.ui.*
import com.giftidea.keeper.domain.model.BirthdayEvent

@Composable
fun UpcomingScreen(state: UpcomingUiState, onBack: () -> Unit, onRange: (Boolean) -> Unit, onOpen: (BirthdayEvent) -> Unit, onSeen: (String) -> Unit, onRetry: () -> Unit, onSettings: () -> Unit, onDestination: (Int) -> Unit, onAddPerson: () -> Unit) {
    DecoratedBackground(BackgroundVariant.Detail) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().widthIn(max = 720.dp).align(Alignment.TopCenter).padding(horizontal = 20.dp)) {
            ScreenHeader(stringResource(R.string.coming_up), onBack = onBack)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SecondaryButton(R.string.next_30_days, { onRange(false) }, Modifier.weight(1f).semantics { selected = !state.yearRange }, state.yearRange); SecondaryButton(R.string.next_year, { onRange(true) }, Modifier.weight(1f).semantics { selected = state.yearRange }, !state.yearRange) } }
                if (state.loading) item { Text(stringResource(R.string.loading_notebook)) }
                else if (state.error) item { EmptyStateCard(R.string.read_error, R.string.retry, onRetry) }
                else {
                    if (!state.notificationsAvailable) item { NotebookCard { Text(stringResource(R.string.reminders_here)); LabelButton(R.string.settings_action, onSettings) } }
                    if (state.writeError) item { Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error) }
                    items(state.reminders, key = { it.key }) { reminder -> ReminderCard(reminder, { onOpen(reminder.event) }, { onSeen(reminder.key) }) }
                    if (state.events.isEmpty()) item { EmptyStateCard(if (state.yearRange) R.string.no_birthdays_year else R.string.no_birthdays_30, if (state.yearRange) R.string.add_person else R.string.next_year, if (state.yearRange) onAddPerson else ({ onRange(true) })) }
                    items(state.events, key = { it.person.id }) { event -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { BirthdayEventCard(event); SecondaryButton(R.string.open_ideas, { onOpen(event) }, Modifier.fillMaxWidth()) } }
                }
            }
            PeopleNavigation(1, onDestination)
        }
    }
}
