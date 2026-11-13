package com.giftidea.keeper.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.giftidea.keeper.data.local.NotebookPreferences
import java.util.concurrent.Executors

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { ReminderWork.run(context, goAsync()) }
}

class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { ReminderWork.run(context, goAsync()) }
}

private object ReminderWork {
    private val executor = Executors.newSingleThreadExecutor()
    fun run(context: Context, pending: BroadcastReceiver.PendingResult) {
        val application = context.applicationContext
        executor.execute {
            try {
                val storage = NotebookPreferences(application)
                val coordinator = ReminderCoordinator(application)
                val previous = storage.read()
                if (previous != null) {
                    var sent = emptySet<String>()
                    val next = storage.transaction { current ->
                        val book = requireNotNull(current)
                        sent = coordinator.newlySent(book)
                        book.copy(sentReminders = book.sentReminders + sent)
                    }
                    coordinator.update(previous, next, sent)
                }
            } catch (_: Exception) {
                pending.finish()
                return@execute
            }
            pending.finish()
        }
    }
}
