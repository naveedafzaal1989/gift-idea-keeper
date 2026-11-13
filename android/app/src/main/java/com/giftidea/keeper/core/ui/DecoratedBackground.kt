package com.giftidea.keeper.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.giftidea.keeper.R
import com.giftidea.keeper.core.theme.*

enum class BackgroundVariant { Loader, People, Detail, Result, Form }

@Composable
fun DecoratedBackground(variant: BackgroundVariant = BackgroundVariant.People, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        val colors = when (variant) {
            BackgroundVariant.Loader -> listOf(Background, Secondary.copy(alpha = 0.88f), Background.copy(alpha = 0.98f))
            BackgroundVariant.Result -> listOf(Secondary.copy(alpha = 0.88f), Background.copy(alpha = 0.95f), BackgroundRaised.copy(alpha = 0.97f))
            BackgroundVariant.Detail -> listOf(Background.copy(alpha = 0.94f), Secondary.copy(alpha = 0.92f), Background.copy(alpha = 0.98f))
            BackgroundVariant.Form -> listOf(Background.copy(alpha = 0.95f), BackgroundRaised.copy(alpha = 0.96f), Secondary.copy(alpha = 0.94f))
            BackgroundVariant.People -> listOf(Background.copy(alpha = 0.90f), BackgroundRaised.copy(alpha = 0.94f), Background.copy(alpha = 0.97f))
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Primary.copy(alpha = 0.08f), Background.copy(alpha = 0f)), radius = 950f)))
        content()
    }
}
