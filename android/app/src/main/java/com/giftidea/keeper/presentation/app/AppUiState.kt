package com.giftidea.keeper.presentation.app

import com.giftidea.keeper.core.navigation.Screen
import com.giftidea.keeper.domain.model.PlanResult

data class AppUiState(
    val backStack: List<Screen> = emptyList(), val results: Map<String, PlanResult> = emptyMap(),
    val pendingPerson: String? = null, val pendingDate: String? = null, val notice: Int? = null
)
