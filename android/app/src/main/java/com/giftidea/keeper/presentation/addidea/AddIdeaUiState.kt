package com.giftidea.keeper.presentation.addidea

data class AddIdeaUiState(
    val loading: Boolean = true, val personName: String = "", val title: String = "", val note: String = "",
    val estimateEnabled: Boolean = false, val amount: String = "0.00", val link: String = "",
    val dirty: Boolean = false, val saving: Boolean = false, val error: Int? = null, val invalidField: Int = 0,
    val confirmDiscard: Boolean = false, val confirmDelete: Boolean = false, val editing: Boolean = false, val missing: Boolean = false,
    val canDecreaseAmount: Boolean = false, val canIncreaseAmount: Boolean = true
)
