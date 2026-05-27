package com.app.finnote.data.repository

import com.app.finnote.data.DefaultFinNoteData
import com.app.finnote.data.local.FinNoteLocalStore
import com.app.finnote.model.Transaction
import com.app.finnote.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinNoteRepository(
    private val localStore: FinNoteLocalStore
) {
    fun getCurrentMonthKey(): String {
        return currentMonthKey()
    }

    suspend fun getHomeSnapshot(monthKey: String): HomeSnapshot {
        return withContext(Dispatchers.IO) {
            val transactions = localStore.loadTransactions()
            val monthlyLimit = localStore.getMonthlyLimit(monthKey, DefaultFinNoteData.DEFAULT_MONTHLY_LIMIT)
            HomeSnapshot(
                user = localStore.loadUser() ?: DefaultFinNoteData.user,
                notificationCount = localStore.loadNotificationCount(),
                monthKey = monthKey,
                monthlyLimit = monthlyLimit,
                monthlyIncome = getIncomeByMonth(transactions, monthKey),
                monthlyExpense = getExpenseByMonth(transactions, monthKey),
                transactions = transactions
            )
        }
    }

    suspend fun setMonthlyLimit(monthKey: String, limit: Int): HomeSnapshot {
        return withContext(Dispatchers.IO) {
            localStore.setMonthlyLimit(monthKey, limit)
            val transactions = localStore.loadTransactions()
            HomeSnapshot(
                user = localStore.loadUser() ?: DefaultFinNoteData.user,
                notificationCount = localStore.loadNotificationCount(),
                monthKey = monthKey,
                monthlyLimit = localStore.getMonthlyLimit(monthKey, DefaultFinNoteData.DEFAULT_MONTHLY_LIMIT),
                monthlyIncome = getIncomeByMonth(transactions, monthKey),
                monthlyExpense = getExpenseByMonth(transactions, monthKey),
                transactions = transactions
            )
        }
    }

    suspend fun getTransactions(): List<Transaction> {
        return withContext(Dispatchers.IO) {
            localStore.loadTransactions()
        }
    }

    suspend fun getTransaction(index: Int): Transaction? {
        return withContext(Dispatchers.IO) {
            localStore.getTransaction(index)
        }
    }

    suspend fun getProfileSnapshot(): ProfileSnapshot {
        return withContext(Dispatchers.IO) {
            ProfileSnapshot(
                user = localStore.loadUser() ?: DefaultFinNoteData.user,
                transactionCount = localStore.loadTransactions().size
            )
        }
    }

    private fun getExpenseByMonth(transactions: List<Transaction>, monthKey: String): Int {
        return transactions
            .filter { it.type == "expense" && it.date.startsWith(monthKey) }
            .sumOf { it.amount }
    }

    private fun getIncomeByMonth(transactions: List<Transaction>, monthKey: String): Int {
        return transactions
            .filter { it.type == "income" && it.date.startsWith(monthKey) }
            .sumOf { it.amount }
    }

    private fun currentMonthKey(): String {
        val calendar = java.util.Calendar.getInstance()
        return java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(calendar.time)
    }
}

data class HomeSnapshot(
    val user: User,
    val notificationCount: Int,
    val monthKey: String,
    val monthlyLimit: Int,
    val monthlyIncome: Int,
    val monthlyExpense: Int,
    val transactions: List<Transaction>
)

data class ProfileSnapshot(
    val user: User,
    val transactionCount: Int
)
