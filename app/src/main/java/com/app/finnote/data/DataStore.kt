package com.app.finnote.data

import com.app.finnote.model.Transaction

object DataStore {
    val transactions = listOf(
        // Februari
        Transaction("Gaji", 500000, "2026-02-01", "income"),
        Transaction("Makan", 150000, "2026-02-10", "expense"),

        // Maret
        Transaction("Freelance", 350000, "2026-03-05", "income"),
        Transaction("Transport", 200000, "2026-03-15", "expense"),

        // April
        Transaction("Gaji", 300000, "2026-04-01", "income"),
        Transaction("Kopi", 80000, "2026-04-10", "expense"),
        Transaction("Makan", 200000, "2026-04-20", "expense"),

        // Mei
        Transaction("Gaji", 400000, "2026-05-01", "income"),
        Transaction("Transport", 320000, "2026-05-18", "expense"),

        // Juni
        Transaction("Freelance", 380000, "2026-06-10", "income"),
        Transaction("Belanja", 290000, "2026-06-22", "expense"),

        // Juli
        Transaction("Gaji", 420000, "2026-07-01", "income"),
        Transaction("Makan", 350000, "2026-07-15", "expense")
    )
}