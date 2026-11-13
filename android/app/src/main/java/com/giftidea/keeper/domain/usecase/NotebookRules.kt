package com.giftidea.keeper.domain.usecase

import com.giftidea.keeper.domain.model.*
import java.math.BigDecimal
import java.net.URI
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay
import java.time.temporal.ChronoUnit

object NotebookRules {
    fun observed(person: Person, year: Int): LocalDate = MonthDay.of(person.month, person.day).atYear(year)
    fun next(person: Person, today: LocalDate = LocalDate.now()): BirthdayEvent {
        val thisYear = observed(person, today.year)
        val date = if (thisYear < today) observed(person, today.year + 1) else thisYear
        return event(person, date, today)
    }
    fun event(person: Person, date: LocalDate, today: LocalDate = LocalDate.now()) = BirthdayEvent(person, date.toString(), ChronoUnit.DAYS.between(today, date), person.month == 2 && person.day == 29 && !date.isLeapYear)
    fun events(book: Notebook, today: LocalDate = LocalDate.now()): List<BirthdayEvent> = book.people.map { next(it, today) }.sortedWith(compareBy<BirthdayEvent> { it.date }.thenBy { it.person.name.lowercase() }.thenBy { it.person.id })
    fun budgetEvents(book: Notebook, today: LocalDate = LocalDate.now()): List<BirthdayEvent> {
        val saved = book.plans.mapNotNull { plan ->
            val person = book.people.find { it.id == plan.personId } ?: return@mapNotNull null
            val date = LocalDate.parse(plan.eventDate)
            if (date < today || date > today.plusYears(1)) null else event(person, date, today)
        }
        return (events(book, today) + saved).distinctBy { it.person.id to it.date }.sortedBy { it.date }
    }
    fun totals(ideas: List<GiftIdea>, selected: Set<String>, limit: Long?) = PlanTotals(ideas.filter { it.id in selected }.sumOf { it.estimateMinor ?: 0L }, ideas.count { it.id in selected && it.estimateMinor == null }, limit)
    fun money(input: String): Long? {
        val separator = DecimalFormatSymbols.getInstance().decimalSeparator
        val normalized = input.trim().replace(separator, '.').replace(',', '.')
        if (!Regex("[0-9]{1,4}(\\.[0-9]{1,2})?").matches(normalized)) return null
        return runCatching { BigDecimal(normalized).movePointRight(2).longValueExact() }.getOrNull()?.takeIf { it in 0L..500000L }
    }
    fun amountInput(minor: Long): String = BigDecimal.valueOf(minor, 2).toPlainString()
    fun safeLink(link: String): Boolean = link.isEmpty() || runCatching { val uri = URI(link); uri.scheme in setOf("http", "https") && !uri.host.isNullOrBlank() && uri.userInfo == null }.getOrDefault(false)
    fun due(book: Notebook, now: LocalDateTime = LocalDateTime.now()): List<DueReminder> = events(book, now.toLocalDate()).mapNotNull { event ->
        val time = LocalDate.parse(event.date).minusDays(book.settings.leadDays.toLong()).atTime(LocalTime.of(9, 0))
        val key = "${event.person.id}:${event.date}:${book.settings.leadDays}"
        if (now < time || key in book.seenReminders) null else DueReminder(key, event, book.ideas.filter { it.personId == event.person.id }.map { it.title })
    }
    fun validate(book: Notebook) {
        require(book.people.map { it.id }.distinct().size == book.people.size)
        require(book.ideas.map { it.id }.distinct().size == book.ideas.size)
        require(book.plans.map { it.personId to it.eventDate }.distinct().size == book.plans.size)
        val people = book.people.map { it.id }.toSet()
        book.people.forEach { require(it.id.isNotBlank() && it.name.trim().length in 1..40 && it.relationship.length <= 30); MonthDay.of(it.month, it.day) }
        book.ideas.forEach {
            require(it.personId in people && it.title.trim().length in 1..80 && it.note.length <= 160 && it.link.length <= 512 && safeLink(it.link))
            require(it.estimateMinor == null || it.estimateMinor in 0L..500000L)
        }
        book.plans.forEach { plan ->
            require(plan.personId in people)
            LocalDate.parse(plan.eventDate)
            require(plan.limitMinor == null || plan.limitMinor in 0L..500000L)
            require(plan.ideaIds.all { id -> book.ideas.any { it.id == id && it.personId == plan.personId } })
        }
        book.pastGifts.forEach {
            require(it.personId in people && it.title.isNotBlank())
            require(LocalDate.parse(it.givenDate) <= LocalDate.now())
            require(it.actualMinor == null || it.actualMinor in 0L..500000L)
        }
        require(book.settings.leadDays in 0..30)
    }
    fun removePerson(book: Notebook, id: String) = book.copy(people = book.people.filterNot { it.id == id }, ideas = book.ideas.filterNot { it.personId == id }, plans = book.plans.filterNot { it.personId == id }, pastGifts = book.pastGifts.filterNot { it.personId == id }, seenReminders = book.seenReminders.filterNot { it.startsWith("$id:") }.toSet(), sentReminders = book.sentReminders.filterNot { it.startsWith("$id:") }.toSet())
    fun removeIdea(book: Notebook, id: String) = book.copy(ideas = book.ideas.filterNot { it.id == id }, plans = book.plans.map { it.copy(ideaIds = it.ideaIds - id) })
}
