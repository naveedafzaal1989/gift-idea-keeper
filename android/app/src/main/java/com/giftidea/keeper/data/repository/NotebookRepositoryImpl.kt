package com.giftidea.keeper.data.repository

import android.content.Context
import com.giftidea.keeper.data.local.NotebookPreferences
import com.giftidea.keeper.data.reminder.ReminderCoordinator
import com.giftidea.keeper.data.sample.SampleData
import com.giftidea.keeper.domain.model.*
import com.giftidea.keeper.domain.repository.NotebookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NotebookRepositoryImpl(private val context: Context, private val storage: NotebookPreferences, private val reminders: ReminderCoordinator) : NotebookRepository {
    private val mutableState = MutableStateFlow(NotebookState())
    override val state = mutableState.asStateFlow()
    private val mutex = Mutex()

    override suspend fun load() = withContext(Dispatchers.IO) {
        mutex.withLock {
            mutableState.update { it.copy(loading = true, error = false) }
            try {
                val book = storage.read() ?: storage.transaction { it ?: SampleData.create() }
                mutableState.update { it.copy(notebook = book, loading = false, error = false) }
            } catch (_: Exception) {
                mutableState.update { it.copy(loading = false, error = true) }
            }
        }
        refreshReminders()
    }

    override suspend fun change(transform: (Notebook) -> Notebook): Boolean = withContext(Dispatchers.IO) {
        val success = mutex.withLock {
            try {
                val old = mutableState.value.notebook
                val next = storage.transaction { transform(requireNotNull(it)) }
                mutableState.update { it.copy(notebook = next, loading = false, error = false) }
                runCatching { reminders.update(old, next) }
                true
            } catch (_: Exception) { false }
        }
        if (success) refreshReminders()
        success
    }

    override suspend fun clear(): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val old = mutableState.value.notebook ?: runCatching { storage.read() }.getOrNull()
                val book = storage.clear()
                mutableState.update { it.copy(notebook = book, loading = false, error = false) }
                runCatching { reminders.update(old, book) }
                true
            } catch (_: Exception) { false }
        }
    }

    override suspend fun refreshReminders() = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val previous = storage.read() ?: return@withLock
                val sent = reminders.newlySent(previous)
                val next = if (sent.isEmpty()) previous else storage.transaction { book -> requireNotNull(book).copy(sentReminders = book.sentReminders + sent) }
                mutableState.update { it.copy(notebook = next, platformRevision = it.platformRevision + 1L) }
                reminders.update(previous, next, sent)
            }
            Unit
        }
    }

    override fun notificationsAvailable() = reminders.available()
    override fun appVersion(): String = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty() }.getOrDefault("")
}
