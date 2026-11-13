package com.giftidea.keeper.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.giftidea.keeper.R
import com.giftidea.keeper.core.di.AppViewModelFactory
import com.giftidea.keeper.core.theme.*
import com.giftidea.keeper.core.ui.*

@Composable
fun SplashRoute(onFinished: () -> Unit) {
    val model: SplashViewModel = viewModel(factory = AppViewModelFactory())
    val finished by model.finished.collectAsStateWithLifecycle()
    LaunchedEffect(finished) { if (finished) onFinished() }
    SplashScreen()
}

@Composable
fun SplashScreen() {
    val transition = rememberInfiniteTransition(label = "notebook")
    val float by transition.animateFloat(-6f, 6f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "float")
    val rotation by transition.animateFloat(-3f, 3f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "ribbon")
    val opening = stringResource(R.string.opening_notebook)
    val artEntry = remember { MutableTransitionState(false).apply { targetState = true } }
    val titleEntry = remember { MutableTransitionState(false).apply { targetState = true } }
    DecoratedBackground(BackgroundVariant.Loader) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.private_notebook), color = TextSecondary, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(36.dp))
            AnimatedVisibility(artEntry, enter = fadeIn(tween(450)) + scaleIn(tween(450), initialScale = 0.94f)) {
                Image(painterResource(R.drawable.logo), null, Modifier.size(184.dp).offset(y = float.dp).rotate(rotation), contentScale = ContentScale.Fit)
            }
            Spacer(Modifier.height(32.dp))
            AnimatedVisibility(titleEntry, enter = fadeIn(tween(350, delayMillis = 120))) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, color = TextPrimary, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.splash_subtitle), style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { repeat(5) { GiftSymbol(it, 32) } }
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(Modifier.size(32.dp).semantics { contentDescription = opening }, color = Primary, strokeWidth = 3.dp)
            Spacer(Modifier.height(20.dp))
            Text(opening, color = TextSecondary, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}
