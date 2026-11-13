package com.giftidea.keeper.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.theme.*
import com.giftidea.keeper.domain.model.BirthdayEvent
import com.giftidea.keeper.domain.model.PlanTotals
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

fun moneyLabel(minor: Long): String = NumberFormat.getCurrencyInstance().apply { currency = Currency.getInstance("EUR") }.format(minor / 100.0)

@Composable
fun dateLabel(iso: String): String {
    val pattern = stringResource(R.string.date_pattern)
    return runCatching { LocalDate.parse(iso).format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)) }.getOrDefault(iso)
}

@Composable
fun countdown(days: Long): String = if (days == 0L) stringResource(R.string.today) else pluralStringResource(R.plurals.days_to_go, days.toInt(), days.toInt())

@Composable
fun NotebookCard(modifier: Modifier = Modifier, paper: Boolean = false, selected: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = if (paper) Paper.copy(alpha = 0.98f) else Secondary.copy(alpha = 0.86f),
        contentColor = if (paper) Background else TextPrimary, border = BorderStroke(1.dp, if (selected) Primary else PaperLight.copy(alpha = 0.14f)), shadowElevation = 5.dp) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null, action: Int? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (onBack != null) LabelButton(R.string.back, onBack)
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.headlineMedium); if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary) }
        if (action != null && onAction != null) LabelButton(action, onAction)
    }
}

@Composable
fun StatCard(label: Int, value: String, modifier: Modifier = Modifier) {
    NotebookCard(modifier) { Text(value, style = MaterialTheme.typography.headlineMedium); Text(stringResource(label), style = MaterialTheme.typography.labelSmall) }
}

@Composable
fun PeopleNavigation(selected: Int, onDestination: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf(R.string.people, R.string.upcoming, R.string.budget).forEachIndexed { index, label ->
            val description = stringResource(if (selected == index) R.string.selected else R.string.not_selected)
            TextButton({ onDestination(index) }, Modifier.weight(1f).heightIn(min = 48.dp).semantics { stateDescription = description; this.selected = selected == index }, enabled = selected != index,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary, disabledContentColor = Paper), shape = RoundedCornerShape(12.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { if (index == selected) HorizontalDivider(Modifier.width(24.dp), 3.dp, Action); if (index == 0) Image(painterResource(R.drawable.icon_home), null, Modifier.size(20.dp), contentScale = ContentScale.Fit); Text(stringResource(label), style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

@Composable
fun BirthdayEventCard(event: BirthdayEvent, paper: Boolean = true) {
    NotebookCard(paper = paper) {
        Text(stringResource(R.string.birthday_notebook), style = MaterialTheme.typography.labelSmall)
        Text(event.person.name, style = MaterialTheme.typography.titleLarge)
        Text(dateLabel(event.date), style = MaterialTheme.typography.bodyLarge)
        Text(countdown(event.days), style = MaterialTheme.typography.titleMedium)
        if (event.observedLeapDay) Text(stringResource(R.string.leap_day_note), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun BudgetMeter(totals: PlanTotals, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.known_planned, moneyLabel(totals.knownMinor))
    Column(modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite; stateDescription = description }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AnimatedContent(totals.knownMinor, label = "amount") { amount -> Text(stringResource(R.string.known_planned, moneyLabel(amount)), style = MaterialTheme.typography.titleMedium) }
        if (totals.unknownCount > 0) Text(pluralStringResource(R.plurals.unpriced_ideas, totals.unknownCount, totals.unknownCount), style = MaterialTheme.typography.bodyMedium)
        val limit = totals.limitMinor
        if (limit == null) Text(stringResource(R.string.no_budget), style = MaterialTheme.typography.bodyMedium)
        else {
            Text(stringResource(R.string.budget_limit, moneyLabel(limit)), style = MaterialTheme.typography.bodyMedium)
            val difference = totals.differenceMinor ?: 0L
            Text(stringResource(if (difference < 0) R.string.over_budget else R.string.remaining_budget, moneyLabel(totals.comparisonMinor ?: 0L)), color = if (difference < 0) { if (LocalContentColor.current == Background) Ribbon else ErrorLight } else LocalContentColor.current)
            if (totals.unknownCount > 0) Text(stringResource(R.string.based_on_priced), style = MaterialTheme.typography.labelSmall)
            if (limit > 0) {
                val ratio by animateFloatAsState(totals.meterRatio ?: 0f, tween(180), label = "budget")
                LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth(), color = if (difference < 0) Ribbon else Primary, trackColor = Outline.copy(alpha = 0.25f))
            } else if (totals.knownMinor == 0L) Text(stringResource(R.string.no_spending), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun MoneyStepper(input: String, onInput: (String) -> Unit, onStep: (Int) -> Unit, error: Boolean = false, modifier: Modifier = Modifier, enabled: Boolean = true, canDecrease: Boolean = true, canIncrease: Boolean = true) {
    val description = stringResource(R.string.amount_eur)
    OutlinedTextField(input, onInput, modifier.fillMaxWidth().heightIn(min = 64.dp).semantics { stateDescription = input }, label = { Text(description) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, isError = error, enabled = enabled,
        supportingText = { Text(stringResource(if (error) R.string.invalid_amount else R.string.amount_hint)) })
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { SecondaryButton(R.string.decrease, { onStep(-1) }, Modifier.weight(1f), enabled && canDecrease); SecondaryButton(R.string.increase, { onStep(1) }, Modifier.weight(1f), enabled && canIncrease) }
}

@Composable
fun ToggleRow(label: Int, checked: Boolean, onChecked: (Boolean) -> Unit, enabled: Boolean = true) {
    val description = stringResource(if (checked) R.string.enabled else R.string.disabled)
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onChecked).semantics(mergeDescendants = true) { stateDescription = description }, verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(label), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked, null, enabled = enabled)
    }
}

@Composable
fun EmptyStateCard(title: Int, action: Int, onAction: () -> Unit) { NotebookCard { Text(stringResource(title), style = MaterialTheme.typography.bodyLarge); SecondaryButton(action, onAction) } }

@Composable
fun SavedFeedback(message: Int) { Text(stringResource(message), Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = Primary, style = MaterialTheme.typography.bodyMedium) }

@Composable
fun GiftSymbol(index: Int, size: Int = 48) {
    when (index.mod(5)) {
        0 -> Image(painterResource(R.drawable.sprite_1), null, Modifier.size(size.dp), contentScale = ContentScale.Fit)
        1 -> Image(painterResource(R.drawable.sprite_2), null, Modifier.size(size.dp), contentScale = ContentScale.Fit)
        2 -> Image(painterResource(R.drawable.sprite_3), null, Modifier.size(size.dp), contentScale = ContentScale.Fit)
        3 -> Image(painterResource(R.drawable.sprite_4), null, Modifier.size(size.dp), contentScale = ContentScale.Fit)
        else -> Image(painterResource(R.drawable.sprite_5), null, Modifier.size(size.dp), contentScale = ContentScale.Fit)
    }
}
