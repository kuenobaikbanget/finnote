package com.app.finnote.data

import android.content.Context
import com.app.finnote.data.local.FinNoteLocalStore
import com.app.finnote.data.repository.FinNoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object AppContainer {
    lateinit var repository: FinNoteRepository
        private set

    fun initialize(context: Context) {
        runBlocking(Dispatchers.IO) {
            val localStore = FinNoteLocalStore(context)
            localStore.initializeIfNeeded(
                user = DefaultFinNoteData.user,
                notificationCount = 0,
                transactions = DefaultFinNoteData.transactions,
                monthlyLimits = DefaultFinNoteData.monthlyLimits
            )
            repository = FinNoteRepository(localStore)
        }
    }
}
