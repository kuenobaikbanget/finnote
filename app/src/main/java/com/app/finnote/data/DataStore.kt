package com.app.finnote.data

import com.app.finnote.model.Transaction
import com.app.finnote.model.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DataStore {
    private const val DEFAULT_MONTHLY_LIMIT = 5_000_000

    var currentUser = User(
        name = "Rizky",
        email = "rizky@gmail.com",
        joinedDate = "21 Januari 2024"
    )

    var notificationCount = 0

    val transactions = mutableListOf(
        // Oktober 2025
        Transaction("Gaji", 5500000, "2025-10-01", "income", "Gaji", "Gaji bulanan Oktober"),
        Transaction("Makan", 150000, "2025-10-03", "expense", "Makan & Minum", "Makan siang di kantin kampus"),
        Transaction("Transport", 50000, "2025-10-05", "expense", "Transport", "Ongkos angkot ke kampus"),
        Transaction("Listrik", 250000, "2025-10-10", "expense", "Tagihan", "Bayar listrik kos bulan Oktober"),
        Transaction("Belanja Bulanan", 500000, "2025-10-15", "expense", "Belanja", "Belanja kebutuhan bulanan di minimarket"),

        // November 2025
        Transaction("Gaji", 5500000, "2025-11-01", "income", "Gaji", "Gaji bulanan November"),
        Transaction("Makan", 180000, "2025-11-05", "expense", "Makan & Minum", "Makan bareng teman di warteg"),
        Transaction("Transport", 60000, "2025-11-08", "expense", "Transport", "Grab ke kampus"),
        Transaction("Internet", 300000, "2025-11-15", "expense", "Tagihan", "Bayar WiFi kos"),
        Transaction("Kopi", 50000, "2025-11-20", "expense", "Makan & Minum", "Kopi susu di kafe dekat kampus"),

        // Desember 2025
        Transaction("Gaji", 6000000, "2025-12-01", "income", "Gaji", "Gaji bulanan Desember"),
        Transaction("Bonus Akhir Tahun", 2000000, "2025-12-20", "income", "Bonus", "Bonus akhir tahun dari kantor"),
        Transaction("Makan", 200000, "2025-12-05", "expense", "Makan & Minum", "Makan malam natal bersama keluarga"),
        Transaction("Transport", 70000, "2025-12-10", "expense", "Transport", "Pulang pergi naik kereta"),
        Transaction("Hadiah Natal", 500000, "2025-12-24", "expense", "Hadiah", "Hadiah untuk keluarga"),

        // Januari 2026
        Transaction("Gaji", 5500000, "2026-01-01", "income", "Gaji", "Gaji bulanan Januari"),
        Transaction("Makan", 170000, "2026-01-05", "expense", "Makan & Minum", "Makan di food court mall"),
        Transaction("Transport", 55000, "2026-01-10", "expense", "Transport", "Ongkos ojol"),
        Transaction("Sewa Rumah", 1500000, "2026-01-01", "expense", "Tagihan", "Sewa kos bulan Januari"),

        // Februari 2026
        Transaction("Gaji", 5500000, "2026-02-01", "income", "Gaji", "Gaji bulanan Februari"),
        Transaction("Makan", 160000, "2026-02-05", "expense", "Makan & Minum", "Makan siang di kantin"),
        Transaction("Transport", 65000, "2026-02-12", "expense", "Transport", "Naik Transjakarta"),
        Transaction("Pulsa", 100000, "2026-02-15", "expense", "Tagihan", "Isi pulsa dan paket data"),
        Transaction("Freelance", 800000, "2026-02-20", "income", "Freelance", "Desain logo untuk UMKM"),

        // Maret 2026
        Transaction("Gaji", 5500000, "2026-03-01", "income", "Gaji", "Gaji bulanan Maret"),
        Transaction("Freelance", 600000, "2026-03-25", "income", "Freelance", "Freelance desain undangan"),

        // April 2026
        Transaction("Transport", 55000, "2026-04-10", "expense", "Transport", "Ongkos angkot"),
        Transaction("Listrik", 280000, "2026-04-15", "expense", "Tagihan", "Bayar listrik kos bulan April"),
        Transaction("Jual Sepatu", 476000, "2026-04-20", "income", "Jual Barang", "Jual sepatu bekas di marketplace"),

        // Mei 2026
        Transaction("Gaji", 5500000, "2026-05-01", "income", "Gaji", "Gaji bulanan Mei"),
        Transaction("Makan", 150000, "2026-05-03", "expense", "Makan & Minum", "Makan bakso di pinggir jalan"),
        Transaction("Transport", 50000, "2026-05-05", "expense", "Transport", "Ongkos angkot ke kampus"),
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

    fun getCurrentMonthKey(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
    }
}
