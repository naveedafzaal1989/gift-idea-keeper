package com.giftidea.keeper.presentation.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.ui.*
import com.giftidea.keeper.domain.model.GiftIdea

@Composable
fun IdeaCard(idea: GiftIdea, selected: Boolean, index: Int, onToggle: () -> Unit, onEdit: () -> Unit, onCopy: () -> Unit, onGiven: () -> Unit, enabled: Boolean = true) {
    val description = stringResource(if (selected) R.string.included else R.string.not_included)
    NotebookCard(selected = selected) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { GiftSymbol(index, 44); Text(idea.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium) }
        Text(idea.estimateMinor?.let(::moneyLabel) ?: stringResource(R.string.price_unknown), style = MaterialTheme.typography.bodyLarge)
        if (idea.note.isNotBlank()) Text(idea.note, style = MaterialTheme.typography.bodyMedium)
        if (idea.link.isNotBlank()) Text(stringResource(if (idea.link.startsWith("https://example.com/")) R.string.example_reference else R.string.reference_saved), style = MaterialTheme.typography.labelSmall)
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(selected, enabled = enabled, role = Role.Checkbox, onValueChange = { onToggle() }).semantics { stateDescription = description }, verticalAlignment = Alignment.CenterVertically) {
            Checkbox(selected, null, enabled = enabled); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.include_in_plan), style = MaterialTheme.typography.bodyMedium)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { LabelButton(R.string.edit, onEdit, Modifier.weight(1f), enabled); if (idea.link.isNotBlank()) LabelButton(R.string.copy_link, onCopy, Modifier.weight(1f), enabled); LabelButton(R.string.mark_given, onGiven, Modifier.weight(1f), enabled) }
    }
}
