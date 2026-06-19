package com.app.finnote.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.app.finnote.model.Transaction

class FinNoteDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_TRANSACTIONS)
        db.execSQL(CREATE_TABLE_USERS)
        db.execSQL(CREATE_TABLE_MONTHLY_BUDGETS)
        db.execSQL(CREATE_TABLE_PREFERENCES)
        seedData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS transactions")
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS monthly_budgets")
        db.execSQL("DROP TABLE IF EXISTS preferences")
        onCreate(db)
    }

    private fun seedData(db: SQLiteDatabase) {
        // Seed user
        db.execSQL(
            "INSERT INTO users (email, name, joined_date) VALUES (?, ?, ?)",
            arrayOf("rizky@gmail.com", "Rizky", "21 Januari 2024")
        )

        // Seed transactions
        val transactions = listOf(
            Transaction("Gaji", 5500000, "2025-10-01", "income", "Gaji", "Gaji bulanan Oktober"),
            Transaction("Makan", 150000, "2025-10-03", "expense", "Makan & Minum", "Makan siang di kantin kampus"),
            Transaction("Transport", 50000, "2025-10-05", "expense", "Transport", "Ongkos angkot ke kampus"),
            Transaction("Listrik", 250000, "2025-10-10", "expense", "Tagihan", "Bayar listrik kos bulan Oktober"),
            Transaction("Belanja Bulanan", 500000, "2025-10-15", "expense", "Belanja", "Belanja kebutuhan bulanan di minimarket"),

            Transaction("Gaji", 5500000, "2025-11-01", "income", "Gaji", "Gaji bulanan November"),
            Transaction("Makan", 180000, "2025-11-05", "expense", "Makan & Minum", "Makan bareng teman di warteg"),
            Transaction("Transport", 60000, "2025-11-08", "expense", "Transport", "Grab ke kampus"),
            Transaction("Internet", 300000, "2025-11-15", "expense", "Tagihan", "Bayar WiFi kos"),
            Transaction("Kopi", 50000, "2025-11-20", "expense", "Makan & Minum", "Kopi susu di kafe dekat kampus"),

            Transaction("Gaji", 6000000, "2025-12-01", "income", "Gaji", "Gaji bulanan Desember"),
            Transaction("Bonus Akhir Tahun", 2000000, "2025-12-20", "income", "Bonus", "Bonus akhir tahun dari kantor"),
            Transaction("Makan", 200000, "2025-12-05", "expense", "Makan & Minum", "Makan malam natal bersama keluarga"),
            Transaction("Transport", 70000, "2025-12-10", "expense", "Transport", "Pulang pergi naik kereta"),
            Transaction("Hadiah Natal", 500000, "2025-12-24", "expense", "Hadiah", "Hadiah untuk keluarga"),

            Transaction("Gaji", 5500000, "2026-01-01", "income", "Gaji", "Gaji bulanan Januari"),
            Transaction("Makan", 170000, "2026-01-05", "expense", "Makan & Minum", "Makan di food court mall"),
            Transaction("Transport", 55000, "2026-01-10", "expense", "Transport", "Ongkos ojol"),
            Transaction("Sewa Rumah", 1500000, "2026-01-01", "expense", "Tagihan", "Sewa kos bulan Januari"),

            Transaction("Gaji", 5500000, "2026-02-01", "income", "Gaji", "Gaji bulanan Februari"),
            Transaction("Makan", 160000, "2026-02-05", "expense", "Makan & Minum", "Makan siang di kantin"),
            Transaction("Transport", 65000, "2026-02-12", "expense", "Transport", "Naik Transjakarta"),
            Transaction("Pulsa", 100000, "2026-02-15", "expense", "Tagihan", "Isi pulsa dan paket data"),
            Transaction("Freelance", 800000, "2026-02-20", "income", "Freelance", "Desain logo untuk UMKM"),

            Transaction("Gaji", 5500000, "2026-03-01", "income", "Gaji", "Gaji bulanan Maret"),
            Transaction("Freelance", 600000, "2026-03-25", "income", "Freelance", "Freelance desain undangan"),

            Transaction("Transport", 55000, "2026-04-10", "expense", "Transport", "Ongkos angkot"),
            Transaction("Listrik", 280000, "2026-04-15", "expense", "Tagihan", "Bayar listrik kos bulan April"),
            Transaction("Jual Sepatu", 476000, "2026-04-20", "income", "Jual Barang", "Jual sepatu bekas di marketplace"),

            Transaction("Gaji", 5500000, "2026-05-01", "income", "Gaji", "Gaji bulanan Mei"),
            Transaction("Makan", 150000, "2026-05-03", "expense", "Makan & Minum", "Makan bakso di pinggir jalan"),
            Transaction("Transport", 50000, "2026-05-05", "expense", "Transport", "Ongkos angkot ke kampus"),

            Transaction("Gaji", 5500000, "2026-06-01", "income", "Gaji", "Gaji bulanan Juni"),
            Transaction("Cicilan Laptop", 950000, "2026-06-05", "expense", "Tagihan", "Bayar cicilan Laptop"),
        )

        for (t in transactions) {
            val values = ContentValues().apply {
                put("title", t.title)
                put("amount", t.amount)
                put("date", t.date)
                put("type", t.type)
                put("category", t.category)
                put("description", t.description)
            }
            db.insert("transactions", null, values)
        }

        // Seed monthly budgets
        val budgets = mapOf(
            "2025-10" to 5_000_000,
            "2025-11" to 5_000_000,
            "2025-12" to 6_000_000,
            "2026-01" to 5_500_000,
            "2026-02" to 5_500_000,
            "2026-03" to 5_500_000,
            "2026-04" to 5_500_000,
        )

        for ((monthKey, limit) in budgets) {
            val values = ContentValues().apply {
                put("month_key", monthKey)
                put("limit_amount", limit)
            }
            db.insert("monthly_budgets", null, values)
        }

        // Seed notification count
        val prefValues = ContentValues().apply {
            put("key", "notification_count")
            put("value", "0")
        }
        db.insert("preferences", null, prefValues)
    }

    companion object {
        private const val DATABASE_NAME = "finnote.db"
        private const val DATABASE_VERSION = 1

        private const val CREATE_TABLE_TRANSACTIONS = """
            CREATE TABLE transactions (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                title       TEXT NOT NULL,
                amount      INTEGER NOT NULL,
                date        TEXT NOT NULL,
                type        TEXT NOT NULL,
                category    TEXT DEFAULT '',
                description TEXT DEFAULT ''
            )
        """

        private const val CREATE_TABLE_USERS = """
            CREATE TABLE users (
                email       TEXT PRIMARY KEY,
                name        TEXT NOT NULL,
                joined_date TEXT NOT NULL
            )
        """

        private const val CREATE_TABLE_MONTHLY_BUDGETS = """
            CREATE TABLE monthly_budgets (
                month_key    TEXT PRIMARY KEY,
                limit_amount INTEGER NOT NULL
            )
        """

        private const val CREATE_TABLE_PREFERENCES = """
            CREATE TABLE preferences (
                key   TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
        """
    }
}
