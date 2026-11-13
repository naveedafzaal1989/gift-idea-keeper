package com.giftidea.keeper.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.giftidea.keeper.R
import com.giftidea.keeper.core.theme.*

@Composable
fun PrimaryButton(text: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.98f else 1f, spring(0.85f, 600f), label = "button")
    val status = stringResource(if (loading) R.string.saving else if (enabled) R.string.ready else R.string.unavailable)
    val shape = RoundedCornerShape(16.dp)
    Button(onClick, modifier.fillMaxWidth().heightIn(min = 60.dp).scale(scale).shadow(if (enabled) 6.dp else 0.dp, shape).clip(shape)
        .background(Brush.verticalGradient(if (enabled) listOf(Action, ActionDeep) else listOf(Secondary, BackgroundRaised))).semantics { stateDescription = status },
        enabled = enabled && !loading, shape = shape, interactionSource = interaction,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextPrimary, disabledContainerColor = Color.Transparent, disabledContentColor = TextSecondary)) {
        if (loading) { CircularProgressIndicator(Modifier.size(18.dp), color = TextSecondary, strokeWidth = 2.dp); Spacer(Modifier.width(10.dp)) }
        Text(stringResource(if (loading) R.string.saving_action else text), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(text: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick, modifier.heightIn(min = 48.dp), enabled = enabled, shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (enabled) Outline else Outline.copy(alpha = 0.25f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Secondary.copy(alpha = 0.52f), contentColor = TextPrimary, disabledContentColor = TextSecondary.copy(alpha = 0.6f))) {
        Text(stringResource(text))
    }
}

@Composable
fun LabelButton(text: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    TextButton(onClick, modifier.heightIn(min = 48.dp), enabled = enabled, colors = ButtonDefaults.textButtonColors(contentColor = Paper, disabledContentColor = TextSecondary.copy(alpha = 0.5f))) { Text(stringResource(text)) }
}
