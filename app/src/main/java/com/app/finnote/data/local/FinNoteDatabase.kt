package com.app.finnote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.finnote.data.local.dao.AppPreferenceDao
import com.app.finnote.data.local.dao.MonthlyBudgetDao
import com.app.finnote.data.local.dao.TransactionDao
import com.app.finnote.data.local.dao.UserProfileDao
import com.app.finnote.data.local.entity.AppPreferenceEntity
import com.app.finnote.data.local.entity.MonthlyBudgetEntity
import com.app.finnote.data.local.entity.TransactionEntity
import com.app.finnote.data.local.entity.UserProfileEntity

@Database(
    entities = [
        TransactionEntity::class,
        MonthlyBudgetEntity::class,
        UserProfileEntity::class,
        AppPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinNoteDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun monthlyBudgetDao(): MonthlyBudgetDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun appPreferenceDao(): AppPreferenceDao
}
