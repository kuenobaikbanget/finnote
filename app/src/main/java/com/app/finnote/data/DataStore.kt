package com.app.finnote.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.app.finnote.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DataStore {
    private const val DEFAULT_MONTHLY_LIMIT = 5_000_000

    private lateinit var dbHelper: FinNoteDatabaseHelper

    fun init(context: Context) {
        dbHelper = FinNoteDatabaseHelper(context.applicationContext)
    }

    // ── Preferences helpers ───────────────────────────────

    private fun getPref(key: String, default: String = ""): String {
        return try {
            val cursor = dbHelper.readableDatabase.rawQuery(
                "SELECT value FROM preferences WHERE key = ?", arrayOf(key)
            )
            cursor.use {
                if (it.moveToFirst()) it.getString(it.getColumnIndexOrThrow("value")) ?: default
                else default
            }
        } catch (_: Exception) { default }
    }

    private fun setPref(key: String, value: String) {
        val values = ContentValues().apply { put("key", key); put("value", value) }
        dbHelper.writableDatabase.insertWithOnConflict(
            "preferences", null, values, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // ── User profile ──────────────────────────────────────

    fun getUserName(): String = getPref("user_name")
    fun setUserName(name: String) = setPref("user_name", name.trim())

    fun getAvatarUri(): String? = getPref("avatar_uri").ifBlank { null }
    fun setAvatarUri(uri: String) = setPref("avatar_uri", uri)

    fun getJoinedDate(): String = getPref("joined_date")

    // ── Notification ──────────────────────────────────────

    fun getNotificationCount(): Int = getPref("notification_count", "0").toIntOrNull() ?: 0
    fun setNotificationCount(count: Int) = setPref("notification_count", count.toString())

    // ── Transactions ──────────────────────────────────────

    fun getAll(): List<Transaction> {
        return try {
            val cursor = dbHelper.readableDatabase.rawQuery(
                "SELECT * FROM transactions ORDER BY date ASC", null
            )
            cursor.use {
                val list = mutableListOf<Transaction>()
                while (it.moveToNext()) list.add(Transaction.fromCursor(it))
                list
            }
        } catch (_: Exception) { emptyList() }
    }

    fun addTransaction(transaction: Transaction): Long {
        val values = ContentValues().apply {
            put("title", transaction.title)
            put("amount", transaction.amount)
            put("date", transaction.date)
            put("type", transaction.type)
            put("category", transaction.category)
            put("description", transaction.description)
        }
        return dbHelper.writableDatabase.insert("transactions", null, values)
    }

    fun getTransactionById(id: Int): Transaction? {
        return try {
            val cursor = dbHelper.readableDatabase.rawQuery(
                "SELECT * FROM transactions WHERE id = ?", arrayOf(id.toString())
            )
            cursor.use { if (it.moveToFirst()) Transaction.fromCursor(it) else null }
        } catch (_: Exception) { null }
    }

    fun updateTransaction(transaction: Transaction): Boolean {
        return try {
            val values = ContentValues().apply {
                put("title", transaction.title)
                put("amount", transaction.amount)
                put("date", transaction.date)
                put("type", transaction.type)
                put("category", transaction.category)
                put("description", transaction.description)
            }
            dbHelper.writableDatabase.update(
                "transactions", values, "id = ?", arrayOf(transaction.id.toString())
            ) > 0
        } catch (_: Exception) { false }
    }

    fun deleteTransaction(id: Int): Boolean {
        return try {
            dbHelper.writableDatabase.delete(
                "transactions", "id = ?", arrayOf(id.toString())
            ) > 0
        } catch (_: Exception) { false }
    }

    // ── Monthly Budgets ───────────────────────────────────

    fun getMonthlyLimit(monthKey: String): Int {
        return try {
            val cursor = dbHelper.readableDatabase.rawQuery(
                "SELECT limit_amount FROM monthly_budgets WHERE month_key = ?", arrayOf(monthKey)
            )
            cursor.use {
                if (it.moveToFirst()) it.getInt(it.getColumnIndexOrThrow("limit_amount"))
                else DEFAULT_MONTHLY_LIMIT
            }
        } catch (_: Exception) { DEFAULT_MONTHLY_LIMIT }
    }

    fun setMonthlyLimit(monthKey: String, limit: Int) {
        val values = ContentValues().apply {
            put("month_key", monthKey)
            put("limit_amount", limit)
        }
        dbHelper.writableDatabase.insertWithOnConflict(
            "monthly_budgets", null, values, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // ── Monthly Summaries ─────────────────────────────────

    fun getExpenseByMonth(monthKey: String): Int {
        return try {
            val cursor = dbHelper.readableDatabase.rawQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'expense' AND date LIKE ?",
                arrayOf("$monthKey%")
            )
            cursor.use { it.moveToFirst(); it.getInt(0) }
        } catch (_: Exception) { 0 }
    }

    fun getIncomeByMonth(monthKey: String): Int {
        return try {
            val cursor = dbHelper.readableDatabase.rawQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'income' AND date LIKE ?",
                arrayOf("$monthKey%")
            )
            cursor.use { it.moveToFirst(); it.getInt(0) }
        } catch (_: Exception) { 0 }
    }

    fun getLatestTransactionMonth(): String {
        val cursor = dbHelper.readableDatabase.rawQuery(
            "SELECT MAX(date) FROM transactions", null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                val date = it.getString(0)
                if (!date.isNullOrBlank() && date.length >= 7) date.substring(0, 7)
                else getCurrentMonthKey()
            } else getCurrentMonthKey()
        }
    }

    fun getCurrentMonthKey(): String =
        SimpleDateFormat("yyyy-MM", Locale.US).format(Calendar.getInstance().time)
}
