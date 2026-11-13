package com.giftidea.keeper.core.di

import android.content.Context
import com.giftidea.keeper.data.local.NotebookPreferences
import com.giftidea.keeper.data.reminder.ReminderCoordinator
import com.giftidea.keeper.data.repository.NotebookRepositoryImpl
import com.giftidea.keeper.domain.repository.NotebookRepository

object ServiceLocator {
    private var instance: NotebookRepository? = null
    val isInitialized: Boolean get() = instance != null
    val notebook: NotebookRepository get() = checkNotNull(instance)
    fun initialize(context: Context) {
        if (instance == null) {
            val application = context.applicationContext
            instance = NotebookRepositoryImpl(application, NotebookPreferences(application), ReminderCoordinator(application))
        }
    }
}
