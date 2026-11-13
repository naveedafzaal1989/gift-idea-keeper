package com.giftidea.keeper.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.giftidea.keeper.core.navigation.Screen
import com.giftidea.keeper.domain.model.PlanResult
import com.giftidea.keeper.presentation.app.AppViewModel
import com.giftidea.keeper.presentation.splash.SplashViewModel
import com.giftidea.keeper.presentation.menu.MenuViewModel
import com.giftidea.keeper.presentation.game.GameViewModel
import com.giftidea.keeper.presentation.gameover.GameOverViewModel
import com.giftidea.keeper.presentation.addidea.AddIdeaViewModel
import com.giftidea.keeper.presentation.upcoming.UpcomingViewModel
import com.giftidea.keeper.presentation.budget.BudgetViewModel
import com.giftidea.keeper.presentation.settings.SettingsViewModel

class AppViewModelFactory(private val route: Screen? = null, private val result: PlanResult? = null) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val notebook = ServiceLocator.notebook
        val instance: ViewModel = when (modelClass) {
            AppViewModel::class.java -> AppViewModel(notebook)
            SplashViewModel::class.java -> SplashViewModel()
            MenuViewModel::class.java -> MenuViewModel(notebook)
            GameViewModel::class.java -> { val game = route as Screen.Game; GameViewModel(notebook, game.personId, game.eventDate) }
            GameOverViewModel::class.java -> GameOverViewModel(requireNotNull(result))
            AddIdeaViewModel::class.java -> { val form = route as Screen.AddIdea; AddIdeaViewModel(notebook, form.personId, form.ideaId) }
            UpcomingViewModel::class.java -> UpcomingViewModel(notebook)
            BudgetViewModel::class.java -> BudgetViewModel(notebook)
            SettingsViewModel::class.java -> SettingsViewModel(notebook)
            else -> error(modelClass.name)
        }
        return modelClass.cast(instance) ?: error(modelClass.name)
    }
}
