package com.app.finnote.data

import com.app.finnote.model.Transaction
import com.app.finnote.model.User

object DataStore {
    private const val DEFAULT_MONTHLY_LIMIT = 5_000_000

    var currentUser = User(
        name = "Rizky",
        email = "rizky@gmail.com",
        joinedDate = "Januari 2024"
    )

    val transactions = mutableListOf(
        // Desember
        Transaction("Gaji", 500000, "2025-12-01", "income"),
        Transaction("Makan", 100000, "2025-12-05", "expense"),
        Transaction("Bayar Cicilan", 2000000, "2025-12-10", "expense"),


        // Januari
        Transaction("Gaji", 500000, "2026-01-01", "income"),
        Transaction("Makan", 100000, "2026-01-05", "expense"),

        // Februari
        Transaction("Gaji", 500000, "2026-02-01", "income"),
        Transaction("Makan", 150000, "2026-02-10", "expense"),

        // Maret
        Transaction("Freelance", 350000, "2026-03-05", "income"),
        Transaction("Transport", 200000, "2026-03-15", "expense"),

        // April
        Transaction("Gaji Bulanan", 300000, "2026-04-01", "income"),
        Transaction("Kopi", 80000, "2026-04-10", "expense"),
        Transaction("Makan", 200000, "2026-04-20", "expense"),
        Transaction("Transport", 150000, "2026-04-25", "expense"),
        Transaction("Jual Jaket", 174000, "2026-04-30", "income"),

    )

    private val monthlyLimits = mutableMapOf(
        "2026-02" to 4_000_000,
        "2026-03" to 4_500_000,
        "2026-04" to 2_000_000
    )

    fun getAll(): List<Transaction> = transactions.toList()

    fun add(transaction: Transaction) {
        transactions.add(transaction)
    }

    fun remove(index: Int) {
        transactions.removeAt(index)
    }

    fun setMonthlyLimit(monthKey: String, limit: Int) {
        monthlyLimits[monthKey] = limit
    }

    fun getMonthlyLimit(monthKey: String): Int {
        return monthlyLimits[monthKey] ?: DEFAULT_MONTHLY_LIMIT
    }

    fun getExpenseByMonth(monthKey: String): Int {
        return transactions
            .filter { it.type == "expense" && it.date.startsWith(monthKey) }
            .sumOf { it.amount }
    }

    fun getLatestTransactionMonth(): String {
        return transactions
            .maxByOrNull { it.date }
            ?.date
            ?.take(7)
            ?: "2026-04"
    }
}
