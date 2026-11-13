package com.giftidea.keeper.presentation.gameover

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.theme.*
import com.giftidea.keeper.core.ui.*
import com.giftidea.keeper.domain.model.ResultKind

@Composable
fun GameOverScreen(state: GameOverUiState, onAgain: () -> Unit, onMenu: () -> Unit) {
    val result = state.result
    val success = result.kind == ResultKind.Saved || result.kind == ResultKind.GiftRecorded
    val title = when (result.kind) { ResultKind.Saved -> R.string.plan_saved; ResultKind.GiftRecorded -> R.string.gift_recorded; ResultKind.NeedsChanges -> R.string.choose_gift_first; ResultKind.StorageError -> R.string.plan_failed }
    DecoratedBackground(BackgroundVariant.Result) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp).widthIn(max = 520.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (success) {
                Image(painterResource(R.drawable.logo), null, Modifier.size(112.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.height(16.dp))
            }
            NotebookCard(paper = true) {
                Canvas(Modifier.size(48.dp).align(Alignment.CenterHorizontally)) {
                    if (success) {
                        drawLine(Primary, Offset(size.width * 0.18f, size.height * 0.5f), Offset(size.width * 0.42f, size.height * 0.74f), 7f, StrokeCap.Round)
                        drawLine(Primary, Offset(size.width * 0.42f, size.height * 0.74f), Offset(size.width * 0.85f, size.height * 0.23f), 7f, StrokeCap.Round)
                    } else {
                        drawCircle(Ribbon, radius = size.minDimension * 0.45f)
                        drawLine(Paper, Offset(size.width / 2, size.height * 0.22f), Offset(size.width / 2, size.height * 0.56f), 6f, StrokeCap.Round)
                        drawCircle(Paper, 3f, Offset(size.width / 2, size.height * 0.74f))
                    }
                }
                Text(stringResource(title), Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text(result.personName, Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Text(dateLabel(result.eventDate), Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                if (success) {
                    result.titles.forEach { Text(stringResource(R.string.gift_title_bullet, it), style = MaterialTheme.typography.bodyLarge) }
                    if (result.kind == ResultKind.GiftRecorded) {
                        Text(if (result.totals.unknownCount > 0) stringResource(R.string.actual_unknown) else stringResource(R.string.actual_paid, moneyLabel(result.totals.knownMinor)), style = MaterialTheme.typography.titleMedium)
                    } else BudgetMeter(result.totals)
                } else Text(stringResource(if (result.kind == ResultKind.NeedsChanges) R.string.choose_gift_message else R.string.plan_failed_message), style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.keep_planning), color = TextSecondary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            PrimaryButton(R.string.play_again, onAgain)
            Spacer(Modifier.height(12.dp))
            SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth())
        }
    }
}
