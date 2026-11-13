package com.giftidea.keeper.domain.repository

import com.giftidea.keeper.domain.model.Notebook
import com.giftidea.keeper.domain.model.NotebookState
import kotlinx.coroutines.flow.StateFlow

interface NotebookRepository {
    val state: StateFlow<NotebookState>
    suspend fun load()
    suspend fun change(transform: (Notebook) -> Notebook): Boolean
    suspend fun clear(): Boolean
    suspend fun refreshReminders()
    fun notificationsAvailable(): Boolean
    fun appVersion(): String
}
