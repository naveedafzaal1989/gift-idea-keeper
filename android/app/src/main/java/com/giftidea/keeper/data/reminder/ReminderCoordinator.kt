package com.giftidea.keeper.data.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.giftidea.keeper.MainActivity
import com.giftidea.keeper.R
import com.giftidea.keeper.domain.model.*
import com.giftidea.keeper.domain.usecase.NotebookRules
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderCoordinator(private val context: Context) {
    private val alarms = context.getSystemService(AlarmManager::class.java)
    private val notifications = context.getSystemService(NotificationManager::class.java)

    fun available(): Boolean {
        createChannel()
        return (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) && notifications.areNotificationsEnabled() && notifications.getNotificationChannel(CHANNEL).importance != NotificationManager.IMPORTANCE_NONE
    }

    fun newlySent(book: Notebook): Set<String> = if (book.settings.reminders && available()) NotebookRules.due(book).map { it.key }.toSet() - book.sentReminders else emptySet()

    fun update(previous: Notebook?, book: Notebook, sent: Set<String> = emptySet()) {
        if (previous != null) {
            previous.people.forEach { person ->
                (LocalDate.now().year - 1..LocalDate.now().year + 2).forEach { year ->
                    alarms.cancel(pending(person.id, NotebookRules.observed(person, year).toString(), previous.settings.leadDays))
                }
                if (book.people.none { it.id == person.id } || !book.settings.reminders) notifications.cancel(person.id.hashCode())
            }
        }
        if (!book.settings.reminders) {
            notifications.cancelAll()
            if (Build.VERSION.SDK_INT >= 34) alarms.cancelAll()
            return
        }
        val now = LocalDateTime.now()
        NotebookRules.events(book).forEach { event ->
            var date = LocalDate.parse(event.date)
            var time = date.minusDays(book.settings.leadDays.toLong()).atTime(LocalTime.of(9, 0))
            if (time <= now) {
                date = NotebookRules.observed(event.person, date.year + 1)
                time = date.minusDays(book.settings.leadDays.toLong()).atTime(LocalTime.of(9, 0))
            }
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), pending(event.person.id, date.toString(), book.settings.leadDays))
        }
        if (available()) NotebookRules.due(book.copy(sentReminders = emptySet())).filter { it.key in sent }.forEach(::notify)
    }

    private fun pending(personId: String, date: String, lead: Int): PendingIntent = PendingIntent.getBroadcast(context, 0,
        Intent(context, ReminderReceiver::class.java).setData(Uri.parse("giftkeeper://reminder/$personId/$date/$lead")), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    private fun createChannel() {
        notifications.createNotificationChannel(NotificationChannel(CHANNEL, context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT))
    }

    private fun notify(reminder: DueReminder) {
        val event = reminder.event
        val open = Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .setData(Uri.parse("giftkeeper://person/${event.person.id}/${event.date}"))
            .putExtra("person_id", event.person.id).putExtra("event_date", event.date)
        val content = if (reminder.ideaTitles.isEmpty()) context.getString(R.string.no_reminder_ideas) else reminder.ideaTitles.take(3).joinToString("\n") + if (reminder.ideaTitles.size > 3) "\n" + context.getString(R.string.more_ideas, reminder.ideaTitles.size - 3) else ""
        val public = Notification.Builder(context, CHANNEL).setSmallIcon(android.R.drawable.ic_menu_my_calendar).setContentTitle(context.getString(R.string.app_name)).setContentText(context.getString(R.string.birthday_reminder)).build()
        val notification = Notification.Builder(context, CHANNEL).setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(context.getString(R.string.notification_title, event.person.name, event.days.toInt()))
            .setContentText(content).setStyle(Notification.BigTextStyle().bigText(content)).setAutoCancel(true)
            .setVisibility(Notification.VISIBILITY_PRIVATE).setPublicVersion(public)
            .setContentIntent(PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)).build()
        notifications.notify(event.person.id.hashCode(), notification)
    }

    companion object { private const val CHANNEL = "birthday_reminders" }
}
