package com.app.finnote.data

import com.app.finnote.model.Transaction
import com.app.finnote.model.User

object DataStore {
    private const val DEFAULT_MONTHLY_LIMIT = 5_000_000

    var currentUser = User(
        name = "Rizky",
        email = "rizky@gmail.com",
        joinedDate = "21 Januari 2024"
    )

    val transactions = mutableListOf(
        // Oktober 2025
        Transaction("Gaji", 5500000, "2025-10-01", "income"),
        Transaction("Makan", 150000, "2025-10-03", "expense"),
        Transaction("Transport", 50000, "2025-10-05", "expense"),
        Transaction("Listrik", 250000, "2025-10-10", "expense"),
        Transaction("Belanja Bulanan", 500000, "2025-10-15", "expense"),

        // November 2025
        Transaction("Gaji", 5500000, "2025-11-01", "income"),
        Transaction("Makan", 180000, "2025-11-05", "expense"),
        Transaction("Transport", 60000, "2025-11-08", "expense"),
        Transaction("Internet", 300000, "2025-11-15", "expense"),
        Transaction("Kopi", 50000, "2025-11-20", "expense"),

        // Desember 2025
        Transaction("Gaji", 6000000, "2025-12-01", "income"),
        Transaction("Bonus Akhir Tahun", 2000000, "2025-12-20", "income"),
        Transaction("Makan", 200000, "2025-12-05", "expense"),
        Transaction("Transport", 70000, "2025-12-10", "expense"),
        Transaction("Hadiah Natal", 500000, "2025-12-24", "expense"),

        // Januari 2026
        Transaction("Gaji", 5500000, "2026-01-01", "income"),
        Transaction("Makan", 170000, "2026-01-05", "expense"),
        Transaction("Transport", 55000, "2026-01-10", "expense"),
        Transaction("Sewa Rumah", 1500000, "2026-01-01", "expense"),

        // Februari 2026
        Transaction("Gaji", 5500000, "2026-02-01", "income"),
        Transaction("Makan", 160000, "2026-02-05", "expense"),
        Transaction("Transport", 65000, "2026-02-12", "expense"),
        Transaction("Pulsa", 100000, "2026-02-15", "expense"),
        Transaction("Freelance", 800000, "2026-02-20", "income"),

        // Maret 2026
        Transaction("Gaji", 5500000, "2026-03-01", "income"),
        Transaction("Makan", 190000, "2026-03-05", "expense"),
        Transaction("Transport", 60000, "2026-03-10", "expense"),
        Transaction("Kesehatan", 300000, "2026-03-15", "expense"),
        Transaction("Freelance", 600000, "2026-03-25", "income"),

        // April 2026
        Transaction("Gaji", 5500000, "2026-04-01", "income"),
        Transaction("Makan", 180000, "2026-04-05", "expense"),
        Transaction("Transport", 55000, "2026-04-10", "expense"),
        Transaction("Listrik", 280000, "2026-04-15", "expense"),
        Transaction("Jual Buku", 150000, "2026-04-20", "income"),
    )

    private val monthlyLimits = mutableMapOf(
        "2025-10" to 5_000_000,
        "2025-11" to 5_000_000,
        "2025-12" to 6_000_000,
        "2026-01" to 5_500_000,
        "2026-02" to 5_500_000,
        "2026-03" to 5_500_000,
        "2026-04" to 5_500_000
    )

    fun getAll(): List<Transaction> = transactions.toList()

    fun getMonthlyLimit(monthKey: String): Int {
        return monthlyLimits[monthKey] ?: DEFAULT_MONTHLY_LIMIT
    }

    fun getExpenseByMonth(monthKey: String): Int {
        return transactions
            .filter { it.type == "expense" && it.date.startsWith(monthKey) }
            .sumOf { it.amount }
    }

    fun getIncomeByMonth(monthKey: String): Int {
        return transactions
            .filter { it.type == "income" && it.date.startsWith(monthKey) }
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
