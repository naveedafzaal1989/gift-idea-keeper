package com.giftidea.keeper.presentation.game

import com.giftidea.keeper.domain.model.*
import com.giftidea.keeper.presentation.dialog.MarkGivenUiState
import com.giftidea.keeper.presentation.dialog.PersonEditorUiState

data class GameUiState(
    val loading: Boolean = true, val error: Boolean = false, val person: Person? = null, val event: BirthdayEvent? = null,
    val ideas: List<GiftIdea> = emptyList(), val pastGifts: List<PastGift> = emptyList(), val selected: Set<String> = emptySet(),
    val savedSelection: Set<String> = emptySet(), val totals: PlanTotals = PlanTotals(), val pastTab: Boolean = false,
    val saving: Boolean = false, val editor: PersonEditorUiState? = null, val given: MarkGivenUiState? = null,
    val confirmClear: Boolean = false, val message: Int? = null
)
