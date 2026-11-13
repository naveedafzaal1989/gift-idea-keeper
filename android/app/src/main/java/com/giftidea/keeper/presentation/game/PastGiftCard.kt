package com.giftidea.keeper.presentation.game

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.giftidea.keeper.R
import com.giftidea.keeper.core.ui.*
import com.giftidea.keeper.domain.model.PastGift

@Composable
fun PastGiftCard(gift: PastGift) {
    NotebookCard { Text(gift.title, style = MaterialTheme.typography.titleMedium); Text(dateLabel(gift.givenDate)); Text(gift.actualMinor?.let(::moneyLabel) ?: stringResource(R.string.actual_unknown)); if (gift.note.isNotBlank()) Text(gift.note, style = MaterialTheme.typography.bodyMedium) }
}
