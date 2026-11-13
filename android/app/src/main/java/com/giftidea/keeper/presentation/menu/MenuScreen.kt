package com.giftidea.keeper.presentation.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.ui.*
import com.giftidea.keeper.domain.model.BirthdayEvent
import com.giftidea.keeper.presentation.dialog.*

@Composable
fun MenuScreen(state: MenuUiState, onPlay: () -> Unit, onOpen: (BirthdayEvent) -> Unit, onSearch: (String) -> Unit, onAdd: () -> Unit, onSettings: () -> Unit, onDismissSamples: () -> Unit, onDestination: (Int) -> Unit, onDraft: (PersonEditorAction) -> Unit, onSavePerson: () -> Unit, onCloseEditor: () -> Unit) {
    DecoratedBackground {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val compact = maxHeight < 520.dp || LocalDensity.current.fontScale > 1.3f
            Column(Modifier.fillMaxSize().widthIn(max = 720.dp).align(Alignment.TopCenter).padding(horizontal = 20.dp)) {
                ScreenHeader(stringResource(R.string.your_people), stringResource(R.string.people_subtitle), action = R.string.settings_action, onAction = onSettings)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
                    item {
                        NotebookCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(stringResource(R.string.next_birthday), style = MaterialTheme.typography.labelSmall)
                                    Text(state.nextBirthday?.person?.name ?: stringResource(R.string.empty_hero), style = MaterialTheme.typography.titleLarge)
                                    state.nextBirthday?.let { Text(dateLabel(it.date), style = MaterialTheme.typography.bodyMedium); Text(countdown(it.days), style = MaterialTheme.typography.bodyLarge) }
                                }
                                Image(painterResource(R.drawable.logo), null, Modifier.size(96.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                    if (state.loading) item { NotebookCard { Text(stringResource(R.string.loading_notebook)); LinearProgressIndicator(Modifier.fillMaxWidth()) } }
                    else if (state.error) item { EmptyStateCard(R.string.read_error, R.string.retry, onPlay) }
                    else {
                        if (state.sampleNotice) item { NotebookCard { Text(stringResource(R.string.sample_notice), style = MaterialTheme.typography.bodyMedium); LabelButton(R.string.dismiss, onDismissSamples) } }
                        if (state.writeError) item { Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error) }
                        if (state.birthdaysSoon > 0 && state.ideaCount > 0) item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { StatCard(R.string.birthdays_30, stringResource(R.string.integer_value, state.birthdaysSoon), Modifier.weight(1f)); StatCard(R.string.saved_ideas, stringResource(R.string.integer_value, state.ideaCount), Modifier.weight(1f)) } }
                        item { OutlinedTextField(state.search, onSearch, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.find_person)) }, singleLine = true); if (state.search.isNotEmpty()) LabelButton(R.string.clear, { onSearch("") }) }
                        if (state.people.isEmpty()) item { EmptyStateCard(if (state.search.isNotBlank()) R.string.no_people_match else R.string.empty_people, if (state.search.isNotBlank()) R.string.clear else R.string.add_person, if (state.search.isNotBlank()) ({ onSearch("") }) else onAdd) }
                        itemsIndexed(state.people, key = { _, person -> person.event.person.id }) { index, person -> PersonCard(person, index) { onOpen(person.event) } }
                        item { SecondaryButton(R.string.add_person, onAdd, Modifier.fillMaxWidth()) }
                    }
                    if (compact) item { MenuActions(state, onPlay, onDestination) }
                }
                if (!compact) MenuActions(state, onPlay, onDestination)
            }
        }
    }
    state.editor?.let { PersonEditorDialog(it, onDraft, onSavePerson, onCloseEditor) }
}

@Composable
private fun MenuActions(state: MenuUiState, onPlay: () -> Unit, onDestination: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.plan_next_hint), style = MaterialTheme.typography.bodyMedium)
        PrimaryButton(if (state.error) R.string.retry else R.string.play, onPlay, enabled = !state.loading)
        PeopleNavigation(0, onDestination)
    }
}
