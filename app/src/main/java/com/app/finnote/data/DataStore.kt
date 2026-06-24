package com.app.finnote.data

import android.content.ContentValues
import android.content.Context
import com.app.finnote.model.Transaction
import com.app.finnote.model.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DataStore {
    private const val DEFAULT_MONTHLY_LIMIT = 5_000_000

    private lateinit var dbHelper: FinNoteDatabaseHelper

    fun init(context: Context) {
        dbHelper = FinNoteDatabaseHelper(context.applicationContext)
    }

    // ── User ──────────────────────────────────────────────

    fun isLoggedIn(): Boolean {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT value FROM preferences WHERE key = 'session_active'",
                null
            )
            cursor.use {
                it.moveToFirst() && it.getString(it.getColumnIndexOrThrow("value")) == "1"
            }
        } catch (_: Exception) {
            false
        }
    }

    fun getSessionEmail(): String? {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT value FROM preferences WHERE key = 'session_email'",
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    val value = it.getString(it.getColumnIndexOrThrow("value"))
                    if (value.isNullOrBlank()) null else value
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun logout() {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("key", "session_active")
            put("value", "0")
        }
        db.insertWithOnConflict("preferences", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun setLoggedIn(email: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("key", "session_active")
            put("value", "1")
        }
        db.insertWithOnConflict("preferences", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
        val emailValues = ContentValues().apply {
            put("key", "session_email")
            put("value", email)
        }
        db.insertWithOnConflict("preferences", null, emailValues, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun loginUser(email: String, password: String): Boolean {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT email FROM users WHERE email = ? AND password = ?",
                arrayOf(email.trim(), password)
            )
            cursor.use { it.moveToFirst() }
        } catch (_: Exception) {
            false
        }
    }

    fun registerUser(email: String, name: String, password: String): Boolean {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("email", email.trim())
                put("name", name.trim())
                put("password", password)
                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
                put("joined_date", sdf.format(Calendar.getInstance().time))
            }
            val result = db.insert("users", null, values)
            result != -1L
        } catch (_: Exception) {
            false
        }
    }

    fun isEmailExists(email: String): Boolean {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT email FROM users WHERE email = ?",
                arrayOf(email.trim())
            )
            cursor.use { it.moveToFirst() }
        } catch (_: Exception) {
            false
        }
    }

    fun getCurrentUser(): User {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT email, name, joined_date FROM users LIMIT 1", null)
            cursor.use {
                if (it.moveToFirst()) {
                    User(
                        email = it.getString(it.getColumnIndexOrThrow("email")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        joinedDate = it.getString(it.getColumnIndexOrThrow("joined_date"))
                    )
                } else {
                    User(name = "", email = "", joinedDate = "")
                }
            }
        } catch (_: Exception) {
            User(name = "", email = "", joinedDate = "")
        }
    }

    // ── Notification ──────────────────────────────────────

    fun getNotificationCount(): Int {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT value FROM preferences WHERE key = 'notification_count'",
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    it.getString(it.getColumnIndexOrThrow("value")).toIntOrNull() ?: 0
                } else {
                    0
                }
            }
        } catch (_: Exception) {
            0
        }
    }

    fun setNotificationCount(count: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("key", "notification_count")
            put("value", count.toString())
        }
        db.insertWithOnConflict("preferences", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    // ── Transactions ──────────────────────────────────────

    fun getAll(): List<Transaction> {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM transactions ORDER BY date ASC",
                null
            )
            cursor.use {
                val list = mutableListOf<Transaction>()
                while (it.moveToNext()) {
                    list.add(Transaction.fromCursor(it))
                }
                list
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addTransaction(transaction: Transaction): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", transaction.title)
            put("amount", transaction.amount)
            put("date", transaction.date)
            put("type", transaction.type)
            put("category", transaction.category)
            put("description", transaction.description)
        }
        return db.insert("transactions", null, values)
    }

    fun getTransactionById(id: Int): Transaction? {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM transactions WHERE id = ?",
                arrayOf(id.toString())
            )
            cursor.use {
                if (it.moveToFirst()) {
                    Transaction.fromCursor(it)
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun updateTransaction(transaction: Transaction): Boolean {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("title", transaction.title)
                put("amount", transaction.amount)
                put("date", transaction.date)
                put("type", transaction.type)
                put("category", transaction.category)
                put("description", transaction.description)
            }
            val affected = db.update(
                "transactions",
                values,
                "id = ?",
                arrayOf(transaction.id.toString())
            )
            affected > 0
        } catch (_: Exception) {
            false
        }
    }

    fun deleteTransaction(id: Int): Boolean {
        return try {
            val db = dbHelper.writableDatabase
            val affected = db.delete(
                "transactions",
                "id = ?",
                arrayOf(id.toString())
            )
            affected > 0
        } catch (_: Exception) {
            false
        }
    }

    // ── Monthly Budgets ───────────────────────────────────

    fun getMonthlyLimit(monthKey: String): Int {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT limit_amount FROM monthly_budgets WHERE month_key = ?",
                arrayOf(monthKey)
            )
            cursor.use {
                if (it.moveToFirst()) {
                    it.getInt(it.getColumnIndexOrThrow("limit_amount"))
                } else {
                    DEFAULT_MONTHLY_LIMIT
                }
            }
        } catch (_: Exception) {
            DEFAULT_MONTHLY_LIMIT
        }
    }

    fun setMonthlyLimit(monthKey: String, limit: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("month_key", monthKey)
            put("limit_amount", limit)
        }
        db.insertWithOnConflict("monthly_budgets", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    // ── Monthly Summaries ─────────────────────────────────

    fun getExpenseByMonth(monthKey: String): Int {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'expense' AND date LIKE ?",
                arrayOf("$monthKey%")
            )
            cursor.use {
                it.moveToFirst()
                it.getInt(0)
            }
        } catch (_: Exception) {
            0
        }
    }

    fun getIncomeByMonth(monthKey: String): Int {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'income' AND date LIKE ?",
                arrayOf("$monthKey%")
            )
            cursor.use {
                it.moveToFirst()
                it.getInt(0)
            }
        } catch (_: Exception) {
            0
        }
    }

    fun getLatestTransactionMonth(): String {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT MAX(date) FROM transactions",
            null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                val date = it.getString(0)
                if (date != null && date.length >= 7) date.substring(0, 7) else "2026-04"
            } else {
                "2026-04"
            }
        }
    }

    fun getCurrentMonthKey(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
    }
}
