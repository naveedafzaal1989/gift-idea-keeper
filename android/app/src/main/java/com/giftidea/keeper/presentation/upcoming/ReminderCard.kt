package com.giftidea.keeper.presentation.upcoming

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import com.giftidea.keeper.R
import com.giftidea.keeper.core.ui.*
import com.giftidea.keeper.domain.model.DueReminder

@Composable
fun ReminderCard(reminder: DueReminder, onOpen: () -> Unit, onSeen: () -> Unit) {
    NotebookCard(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        Text(stringResource(R.string.birthday_reminder), style = MaterialTheme.typography.labelSmall)
        Text(reminder.event.person.name, style = MaterialTheme.typography.titleLarge)
        Text(countdown(reminder.event.days))
        if (reminder.ideaTitles.isEmpty()) Text(stringResource(R.string.no_reminder_ideas))
        else reminder.ideaTitles.take(3).forEach { Text(it) }
        if (reminder.ideaTitles.size > 3) Text(pluralStringResource(R.plurals.more_idea_count, reminder.ideaTitles.size - 3, reminder.ideaTitles.size - 3))
        SecondaryButton(R.string.open_ideas, onOpen)
        LabelButton(R.string.mark_seen, onSeen)
    }
}
