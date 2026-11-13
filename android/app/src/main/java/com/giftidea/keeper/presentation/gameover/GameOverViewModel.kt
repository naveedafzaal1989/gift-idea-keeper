package com.giftidea.keeper.presentation.gameover

import androidx.lifecycle.ViewModel
import com.giftidea.keeper.domain.model.PlanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameOverViewModel(result: PlanResult) : ViewModel() {
    private val mutableState = MutableStateFlow(GameOverUiState(result))
    val state = mutableState.asStateFlow()
}
