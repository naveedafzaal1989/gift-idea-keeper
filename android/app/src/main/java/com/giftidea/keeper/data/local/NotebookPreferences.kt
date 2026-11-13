package com.giftidea.keeper.data.local

import android.content.Context
import com.giftidea.keeper.domain.model.*
import com.giftidea.keeper.domain.usecase.NotebookRules
import org.json.JSONArray
import org.json.JSONObject

class NotebookPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("gift_notebook", Context.MODE_PRIVATE)

    fun read(): Notebook? = synchronized(lock) {
        preferences.getString("document", null)?.let(::decode)
    }

    fun transaction(transform: (Notebook?) -> Notebook): Notebook = synchronized(lock) {
        val previous = preferences.getString("document", null)
        val next = transform(previous?.let(::decode))
        NotebookRules.validate(next)
        commit(encode(next).toString(), previous)
        next
    }

    fun clear(): Notebook = synchronized(lock) {
        val next = Notebook(sampleDismissed = true)
        commit(encode(next).toString(), preferences.getString("document", null))
        next
    }

    private fun commit(document: String, previous: String?) {
        if (!preferences.edit().putString("document", document).commit()) {
            preferences.edit().putString("document", previous).commit()
            error("Persistence failed")
        }
    }

    private fun array(values: Iterable<String>) = JSONArray().also { array -> values.forEach { array.put(it) } }
    private fun json(vararg values: Pair<String, Any?>) = JSONObject().also { obj -> values.forEach { obj.put(it.first, it.second ?: JSONObject.NULL) } }
    private fun encode(book: Notebook): JSONObject = json(
        "schema" to 1, "initialized" to true,
        "people" to JSONArray().also { a -> book.people.forEach { a.put(json("id" to it.id, "name" to it.name, "relationship" to it.relationship, "month" to it.month, "day" to it.day)) } },
        "ideas" to JSONArray().also { a -> book.ideas.forEach { a.put(json("id" to it.id, "person" to it.personId, "title" to it.title, "note" to it.note, "estimate" to it.estimateMinor, "link" to it.link)) } },
        "plans" to JSONArray().also { a -> book.plans.forEach { a.put(json("person" to it.personId, "date" to it.eventDate, "ids" to array(it.ideaIds), "limit" to it.limitMinor)) } },
        "past" to JSONArray().also { a -> book.pastGifts.forEach { a.put(json("id" to it.id, "person" to it.personId, "title" to it.title, "note" to it.note, "date" to it.givenDate, "actual" to it.actualMinor)) } },
        "settings" to json("reminders" to book.settings.reminders, "lead" to book.settings.leadDays, "haptics" to book.settings.haptics),
        "dismissed" to book.sampleDismissed, "seen" to array(book.seenReminders), "sent" to array(book.sentReminders)
    )

    private fun JSONObject.strictLong(key: String): Long {
        val value = get(key)
        require(value is Int || value is Long)
        return (value as Number).toLong()
    }
    private fun JSONObject.strictInt(key: String): Int = strictLong(key).also { require(it in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) }.toInt()
    private fun JSONObject.strictString(key: String): String = get(key) as? String ?: error(key)
    private fun JSONObject.strictBoolean(key: String): Boolean = get(key) as? Boolean ?: error(key)
    private fun JSONObject.nullableLong(key: String): Long? = if (isNull(key)) null else strictLong(key)
    private fun JSONArray.strings() = (0 until length()).map { get(it) as? String ?: error(it.toString()) }.toSet()
    private fun <T> JSONArray.objects(map: (JSONObject) -> T): List<T> = (0 until length()).map { map(getJSONObject(it)) }
    private fun decode(document: String): Notebook {
        val root = JSONObject(document)
        require(root.strictInt("schema") == 1 && root.strictBoolean("initialized"))
        val settings = root.getJSONObject("settings")
        return Notebook(
            root.getJSONArray("people").objects { Person(it.strictString("id"), it.strictString("name"), it.strictString("relationship"), it.strictInt("month"), it.strictInt("day")) },
            root.getJSONArray("ideas").objects { GiftIdea(it.strictString("id"), it.strictString("person"), it.strictString("title"), it.strictString("note"), it.nullableLong("estimate"), it.strictString("link")) },
            root.getJSONArray("plans").objects { BirthdayPlan(it.strictString("person"), it.strictString("date"), it.getJSONArray("ids").strings(), it.nullableLong("limit")) },
            root.getJSONArray("past").objects { PastGift(it.strictString("id"), it.strictString("person"), it.strictString("title"), it.strictString("note"), it.strictString("date"), it.nullableLong("actual")) },
            NotebookSettings(settings.strictBoolean("reminders"), settings.strictInt("lead"), settings.strictBoolean("haptics")),
            root.strictBoolean("dismissed"), root.getJSONArray("seen").strings(), root.getJSONArray("sent").strings()
        ).also(NotebookRules::validate)
    }

    companion object { private val lock = Any() }
}
