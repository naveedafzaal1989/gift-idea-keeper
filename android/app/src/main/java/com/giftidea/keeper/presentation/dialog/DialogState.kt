package com.giftidea.keeper.presentation.dialog

import com.giftidea.keeper.domain.model.Person
import java.time.LocalDate
import java.time.Month

sealed interface PersonEditorAction {
    data class Name(val value: String) : PersonEditorAction
    data class Relationship(val value: String) : PersonEditorAction
    data class BirthdayMonth(val value: Int) : PersonEditorAction
    data class DayStep(val delta: Int) : PersonEditorAction
    data object CancelDelete : PersonEditorAction
}

data class PersonEditorUiState(
    val id: String? = null, val name: String = "", val relationship: String = "", val month: Int = 1, val day: Int = 1,
    val saving: Boolean = false, val error: Int? = null, val confirmDelete: Boolean = false
) {
    val canDecreaseDay: Boolean get() = day > 1
    val canIncreaseDay: Boolean get() = day < Month.of(month).maxLength()
    fun change(action: PersonEditorAction): PersonEditorUiState = when (action) {
        is PersonEditorAction.Name -> copy(name = action.value, error = null)
        is PersonEditorAction.Relationship -> copy(relationship = action.value, error = null)
        is PersonEditorAction.BirthdayMonth -> copy(month = action.value, day = day.coerceAtMost(Month.of(action.value).maxLength()), error = null)
        is PersonEditorAction.DayStep -> copy(day = (day + action.delta).coerceIn(1, Month.of(month).maxLength()), error = null)
        PersonEditorAction.CancelDelete -> copy(confirmDelete = false)
    }
    companion object { fun from(person: Person) = PersonEditorUiState(person.id, person.name, person.relationship, person.month, person.day) }
}

data class MarkGivenUiState(val ideaId: String, val title: String, val date: String = LocalDate.now().toString(), val amountEnabled: Boolean = false, val amount: String = "0.00", val error: Int? = null, val saving: Boolean = false, val datePicker: Boolean = false, val canDecreaseAmount: Boolean = false, val canIncreaseAmount: Boolean = true)
