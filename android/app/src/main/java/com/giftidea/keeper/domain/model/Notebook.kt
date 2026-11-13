package com.giftidea.keeper.domain.model

data class Person(val id: String, val name: String, val relationship: String, val month: Int, val day: Int)
data class GiftIdea(val id: String, val personId: String, val title: String, val note: String, val estimateMinor: Long?, val link: String)
data class BirthdayPlan(val personId: String, val eventDate: String, val ideaIds: Set<String> = emptySet(), val limitMinor: Long? = null)
data class PastGift(val id: String, val personId: String, val title: String, val note: String, val givenDate: String, val actualMinor: Long?)
data class NotebookSettings(val reminders: Boolean = false, val leadDays: Int = 7, val haptics: Boolean = true)
data class Notebook(
    val people: List<Person> = emptyList(), val ideas: List<GiftIdea> = emptyList(),
    val plans: List<BirthdayPlan> = emptyList(), val pastGifts: List<PastGift> = emptyList(),
    val settings: NotebookSettings = NotebookSettings(), val sampleDismissed: Boolean = false,
    val seenReminders: Set<String> = emptySet(), val sentReminders: Set<String> = emptySet()
)
data class NotebookState(val notebook: Notebook? = null, val loading: Boolean = true, val error: Boolean = false, val platformRevision: Long = 0L)
data class BirthdayEvent(val person: Person, val date: String, val days: Long, val observedLeapDay: Boolean)
data class PlanTotals(val knownMinor: Long = 0L, val unknownCount: Int = 0, val limitMinor: Long? = null) {
    val differenceMinor: Long? = limitMinor?.minus(knownMinor)
    val comparisonMinor: Long? = differenceMinor?.let { kotlin.math.abs(it) }
    val meterRatio: Float? = limitMinor?.takeIf { it > 0L }?.let { (knownMinor.toFloat() / it.toFloat()).coerceIn(0f, 1f) }
}
enum class ResultKind { Saved, NeedsChanges, StorageError, GiftRecorded }
data class PlanResult(val kind: ResultKind, val personName: String, val eventDate: String, val titles: List<String> = emptyList(), val totals: PlanTotals = PlanTotals())
data class DueReminder(val key: String, val event: BirthdayEvent, val ideaTitles: List<String>)
