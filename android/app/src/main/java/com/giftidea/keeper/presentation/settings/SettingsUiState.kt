package com.giftidea.keeper.presentation.settings

data class SettingsUiState(val reminders: Boolean = false, val leadDays: Int = 7, val haptics: Boolean = true, val available: Boolean = false, val saving: Boolean = false, val saved: Boolean = false, val error: Boolean = false, val confirmClear: Boolean = false, val version: String = "")
