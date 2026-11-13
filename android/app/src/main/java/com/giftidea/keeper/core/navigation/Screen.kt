package com.giftidea.keeper.core.navigation

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data class Game(val personId: String, val eventDate: String) : Screen
    data class GameOver(val resultId: String) : Screen
    data class AddIdea(val personId: String, val ideaId: String? = null, val draftId: String) : Screen
    data object Upcoming : Screen
    data object Budget : Screen
    data object Settings : Screen
}
