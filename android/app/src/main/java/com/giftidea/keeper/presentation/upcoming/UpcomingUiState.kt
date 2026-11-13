package com.giftidea.keeper.presentation.upcoming

import com.giftidea.keeper.domain.model.*

data class UpcomingUiState(val loading: Boolean = true, val error: Boolean = false, val events: List<BirthdayEvent> = emptyList(), val reminders: List<DueReminder> = emptyList(), val yearRange: Boolean = false, val notificationsAvailable: Boolean = false, val writeError: Boolean = false)
