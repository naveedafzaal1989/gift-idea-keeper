package com.giftidea.keeper.presentation.menu

import com.giftidea.keeper.domain.model.BirthdayEvent
import com.giftidea.keeper.presentation.dialog.PersonEditorUiState

data class PersonSummary(val event: BirthdayEvent, val ideaCount: Int, val plannedMinor: Long)
data class MenuUiState(
    val loading: Boolean = true, val error: Boolean = false, val people: List<PersonSummary> = emptyList(),
    val nextBirthday: BirthdayEvent? = null, val search: String = "", val birthdaysSoon: Int = 0,
    val ideaCount: Int = 0, val sampleNotice: Boolean = false, val editor: PersonEditorUiState? = null,
    val writeError: Boolean = false
)
