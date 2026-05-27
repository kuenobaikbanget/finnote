package com.app.finnote.data.local

import android.content.Context
import androidx.room.Room
import com.app.finnote.data.local.entity.AppPreferenceEntity
import com.app.finnote.data.local.entity.MonthlyBudgetEntity
import com.app.finnote.data.local.entity.TransactionEntity
import com.app.finnote.data.local.entity.UserProfileEntity
import com.app.finnote.model.Transaction
import com.app.finnote.model.User

class FinNoteLocalStore(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        FinNoteDatabase::class.java,
        DATABASE_NAME
    )
        .build()

    fun isInitialized(): Boolean {
        return database.appPreferenceDao().getValue(KEY_STORAGE_INITIALIZED) == VALUE_TRUE
    }

    fun markInitialized() {
        database.appPreferenceDao().upsert(AppPreferenceEntity(KEY_STORAGE_INITIALIZED, VALUE_TRUE))
    }

    fun initializeIfNeeded(
        user: User,
        notificationCount: Int,
        transactions: List<Transaction>,
        monthlyLimits: Map<String, Int>
    ) {
        if (isInitialized()) return

        database.runInTransaction {
            database.userProfileDao().upsert(UserProfileEntity.fromModel(user))
            database.appPreferenceDao().upsert(
                AppPreferenceEntity(KEY_NOTIFICATION_COUNT, notificationCount.coerceAtLeast(0).toString())
            )
            database.transactionDao().clear()
            database.transactionDao().insertAll(transactions.map(TransactionEntity::fromModel))
            database.monthlyBudgetDao().clear()
            database.monthlyBudgetDao().upsertAll(
                monthlyLimits.map { (monthKey, limit) ->
                    MonthlyBudgetEntity(monthKey, limit)
                }
            )
            database.appPreferenceDao().upsert(AppPreferenceEntity(KEY_STORAGE_INITIALIZED, VALUE_TRUE))
        }
    }

    fun loadTransactions(): List<Transaction> {
        return database.transactionDao()
            .getAll()
            .map { it.toModel() }
    }

    fun getTransaction(index: Int): Transaction? {
        return loadTransactions().getOrNull(index)
    }

    fun replaceTransactions(transactions: List<Transaction>) {
        database.runInTransaction {
            database.transactionDao().clear()
            database.transactionDao().insertAll(transactions.map(TransactionEntity::fromModel))
        }
    }

    fun loadMonthlyLimits(): Map<String, Int> {
        return database.monthlyBudgetDao()
            .getAll()
            .associate { it.monthKey to it.limit }
    }

    fun getMonthlyLimit(monthKey: String, defaultLimit: Int): Int {
        return loadMonthlyLimits()[monthKey] ?: defaultLimit
    }

    fun replaceMonthlyLimits(monthlyLimits: Map<String, Int>) {
        database.runInTransaction {
            database.monthlyBudgetDao().clear()
            database.monthlyBudgetDao().upsertAll(
                monthlyLimits.map { (monthKey, limit) ->
                    MonthlyBudgetEntity(monthKey, limit)
                }
            )
        }
    }

    fun setMonthlyLimit(monthKey: String, limit: Int) {
        database.monthlyBudgetDao().upsert(MonthlyBudgetEntity(monthKey, limit))
    }

    fun loadUser(): User? {
        return database.userProfileDao().get()?.toModel()
    }

    fun saveUser(user: User) {
        database.userProfileDao().upsert(UserProfileEntity.fromModel(user))
    }

    fun loadNotificationCount(): Int {
        return database.appPreferenceDao()
            .getValue(KEY_NOTIFICATION_COUNT)
            ?.toIntOrNull()
            ?: 0
    }

    fun saveNotificationCount(count: Int) {
        database.appPreferenceDao().upsert(
            AppPreferenceEntity(KEY_NOTIFICATION_COUNT, count.coerceAtLeast(0).toString())
        )
    }

    private companion object {
        const val DATABASE_NAME = "finnote.db"
        const val KEY_STORAGE_INITIALIZED = "storage_initialized"
        const val KEY_NOTIFICATION_COUNT = "notification_count"
        const val VALUE_TRUE = "true"
    }
}
