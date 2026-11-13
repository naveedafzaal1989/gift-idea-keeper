package com.giftidea.keeper.data.sample

import com.giftidea.keeper.domain.model.*
import com.giftidea.keeper.domain.usecase.NotebookRules
import java.time.LocalDate
import java.util.UUID

object SampleData {
    fun create(today: LocalDate = LocalDate.now()): Notebook {
        val people = listOf(Triple("Maya Chen", "Friend", 5L), Triple("Daniel Rivera", "Brother", 12L), Triple("Elena Rossi", "Mum", 26L)).map { (name, relationship, days) ->
            val date = today.plusDays(days)
            Person(UUID.randomUUID().toString(), name, relationship, date.monthValue, date.dayOfMonth)
        }
        fun idea(person: Int, title: String, amount: Long?, note: String, link: String = "") = GiftIdea(UUID.randomUUID().toString(), people[person].id, title, note, amount, link)
        val ideas = listOf(
            idea(0, "Handmade ceramic mug", 3500L, "Look for a wide handle at the local ceramics studio"),
            idea(0, "Small-batch coffee beans", 1400L, "Medium roast, whole beans"),
            idea(0, "Botanical postcards", null, "Check the museum shop"),
            idea(1, "Trail running socks", 1800L, "Size 42–44, cushioned"),
            idea(1, "Weekend cookbook", 3200L, "Simple recipes for a new kitchen", "https://example.com/gifts/weekend-cookbook"),
            idea(2, "Flower arranging workshop", 4500L, "Choose a Saturday session"),
            idea(2, "Linen apron", 2800L, "Forest green if available")
        )
        val plans = listOf(BirthdayPlan(people[0].id, NotebookRules.next(people[0], today).date, setOf(ideas[0].id), 5000L), BirthdayPlan(people[1].id, NotebookRules.next(people[1], today).date, setOf(ideas[3].id, ideas[4].id), 6500L), BirthdayPlan(people[2].id, NotebookRules.next(people[2], today).date, setOf(ideas[5].id), 6000L))
        val previous = LocalDate.parse(plans[0].eventDate).minusYears(1)
        return Notebook(people, ideas, plans, listOf(PastGift(UUID.randomUUID().toString(), people[0].id, "Pocket sketchbook", "Sample history", previous.toString(), 2200L)))
    }
}
