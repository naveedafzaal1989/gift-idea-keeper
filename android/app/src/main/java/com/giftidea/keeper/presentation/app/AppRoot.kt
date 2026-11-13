package com.giftidea.keeper.presentation.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.giftidea.keeper.MainActivity
import com.giftidea.keeper.R
import com.giftidea.keeper.core.di.AppViewModelFactory
import com.giftidea.keeper.core.navigation.Screen
import com.giftidea.keeper.domain.model.PlanResult
import com.giftidea.keeper.domain.model.ResultKind
import com.giftidea.keeper.presentation.addidea.*
import com.giftidea.keeper.presentation.budget.*
import com.giftidea.keeper.presentation.dialog.ConfirmActionDialog
import com.giftidea.keeper.presentation.game.*
import com.giftidea.keeper.presentation.gameover.*
import com.giftidea.keeper.presentation.menu.*
import com.giftidea.keeper.presentation.settings.*
import com.giftidea.keeper.presentation.splash.SplashRoute
import com.giftidea.keeper.presentation.upcoming.*
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.collect

@Composable
fun AppRoot() {
    val app: AppViewModel = viewModel(factory = AppViewModelFactory())
    val people: MenuViewModel = viewModel(factory = AppViewModelFactory())
    val screen by app.screen.collectAsStateWithLifecycle()
    val appState by app.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        (context as? MainActivity)?.hostEvents?.collect { event ->
            when (event) {
                is HostEvent.Notification -> app.handleNotification(event.personId, event.date)
                HostEvent.Refresh -> app.refreshPlatform()
            }
        }
    }
    val eventHandler: (UiEvent) -> Unit = { event ->
        when (event) {
            is UiEvent.Navigate -> app.navigate(event.screen)
            is UiEvent.Result -> app.result(event.result)
            UiEvent.Back -> app.back()
            UiEvent.Menu -> app.menu()
            UiEvent.Haptic -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            is UiEvent.Copy -> context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(context.getString(R.string.reference_link), event.text))
            UiEvent.Permission -> (context as? MainActivity)?.requestNotifications()
            UiEvent.NotificationSettings -> (context as? MainActivity)?.openNotificationSettings()
        }
    }
    BackHandler(enabled = screen != Screen.Menu && screen != Screen.Splash) { app.back() }
    AnimatedContent(targetState = screen, transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }, label = "screen") { route ->
        when (route) {
            Screen.Splash -> SplashRoute(app::finishSplash)
            Screen.Menu -> {
                val model = people
                val state by model.state.collectAsStateWithLifecycle()
                BindModel(model, eventHandler)
                MenuScreen(state, if (state.error) model::retry else model::play, model::open, model::search, model::addPerson, model::settings, model::dismissSamples, model::destination, model::editDraft, model::savePerson, model::closeEditor)
            }
            is Screen.Game -> {
                val model: GameViewModel = viewModel(key = "person:${route.personId}:${route.eventDate}", factory = AppViewModelFactory(route))
                val state by model.state.collectAsStateWithLifecycle()
                BindModel(model, eventHandler)
                GameScreen(state, app::back, app::menu, model::retry, model::editPerson, model::tab, model::toggle, model::addIdea, model::copyLink, model::markGiven, model::savePlan, model::clearPlan, model::dismissClear, model::editDraft, model::savePerson, model::closeEditor, model::deletePerson, model::givenDraft, model::givenStep, model::recordGift, model::dismissGiven)
            }
            is Screen.GameOver -> {
                val result = appState.results[route.resultId] ?: PlanResult(ResultKind.StorageError, "", java.time.LocalDate.now().toString())
                val model: GameOverViewModel = viewModel(key = "result:${route.resultId}", factory = AppViewModelFactory(route, result))
                val state by model.state.collectAsStateWithLifecycle()
                GameOverScreen(state, app::back, app::menu)
            }
            is Screen.AddIdea -> {
                val model: AddIdeaViewModel = viewModel(key = "idea:${route.personId}:${route.ideaId}:${route.draftId}", factory = AppViewModelFactory(route))
                val state by model.state.collectAsStateWithLifecycle()
                BindModel(model, eventHandler)
                BackHandler { model.back() }
                AddIdeaScreen(state, model::edit, model::step, model::save, model::back, model::retry, model::discard, model::keepEditing, model::delete)
            }
            Screen.Upcoming -> {
                val model: UpcomingViewModel = viewModel(factory = AppViewModelFactory())
                val state by model.state.collectAsStateWithLifecycle()
                BindModel(model, eventHandler)
                UpcomingScreen(state, app::back, model::range, model::open, model::seen, model::retry, { app.navigate(Screen.Settings) }, { destination(app, it) }, { app.menu(); people.addPerson() })
            }
            Screen.Budget -> {
                val model: BudgetViewModel = viewModel(factory = AppViewModelFactory())
                val state by model.state.collectAsStateWithLifecycle()
                BindModel(model, eventHandler)
                BudgetScreen(state, app::back, model::retry, model::open, model::edit, model::draft, model::step, model::save, model::close, { destination(app, it) })
            }
            Screen.Settings -> {
                val model: SettingsViewModel = viewModel(factory = AppViewModelFactory())
                val state by model.state.collectAsStateWithLifecycle()
                BindModel(model, eventHandler)
                SettingsScreen(state, app::back, app::menu, model::reminder, model::lead, model::haptics, model::save, model::notificationSettings, model::confirmClear, model::clear)
            }
        }
    }
    appState.notice?.let { notice -> ConfirmActionDialog(R.string.app_name, stringResource(notice), R.string.dismiss, app::dismissNotice, app::dismissNotice) }
}

private fun destination(app: AppViewModel, index: Int) { when (index) { 0 -> app.menu(); 1 -> app.navigate(Screen.Upcoming); else -> app.navigate(Screen.Budget) } }

@Composable
private fun BindModel(model: NotebookViewModel, onEvent: (UiEvent) -> Unit) {
    val owner = LocalLifecycleOwner.current
    val handler by rememberUpdatedState(onEvent)
    LaunchedEffect(Unit) { model.events.collect { handler(it) } }
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event -> when (event) { Lifecycle.Event.ON_RESUME -> model.resume(); Lifecycle.Event.ON_PAUSE -> model.pause(); else -> Unit } }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) model.resume()
        onDispose { owner.lifecycle.removeObserver(observer); model.pause() }
    }
}
