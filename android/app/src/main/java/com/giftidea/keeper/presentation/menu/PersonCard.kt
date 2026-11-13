package com.giftidea.keeper.presentation.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.theme.TextSecondary
import com.giftidea.keeper.core.ui.*

@Composable
fun PersonCard(person: PersonSummary, index: Int, onOpen: () -> Unit) {
    NotebookCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GiftSymbol(index)
            Column(Modifier.weight(1f)) { Text(person.event.person.name, style = MaterialTheme.typography.titleMedium); if (person.event.person.relationship.isNotBlank()) Text(person.event.person.relationship, color = TextSecondary, style = MaterialTheme.typography.bodyMedium) }
        }
        Text(stringResource(R.string.birthday_summary, dateLabel(person.event.date), countdown(person.event.days)), style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(pluralStringResource(R.plurals.idea_count, person.ideaCount, person.ideaCount), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium); SecondaryButton(R.string.open, onOpen) }
    }
}
