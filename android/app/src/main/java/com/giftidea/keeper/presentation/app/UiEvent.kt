package com.giftidea.keeper.presentation.app

import com.giftidea.keeper.core.navigation.Screen
import com.giftidea.keeper.domain.model.PlanResult

sealed interface HostEvent {
    data class Notification(val personId: String?, val date: String?) : HostEvent
    data object Refresh : HostEvent
}

sealed interface UiEvent {
    data class Navigate(val screen: Screen) : UiEvent
    data class Result(val result: PlanResult) : UiEvent
    data class Copy(val text: String) : UiEvent
    data object Back : UiEvent
    data object Menu : UiEvent
    data object Haptic : UiEvent
    data object Permission : UiEvent
    data object NotificationSettings : UiEvent
}
