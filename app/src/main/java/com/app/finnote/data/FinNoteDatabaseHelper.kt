package com.app.finnote.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinNoteDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_TRANSACTIONS)
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
        val today = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(Date())

        listOf(
            "notification_count" to "0",
            "user_name" to "",
            "avatar_uri" to "",
            "joined_date" to today
        ).forEach { (key, value) ->
            db.insertWithOnConflict(
                "preferences", null,
                ContentValues().apply { put("key", key); put("value", value) },
                SQLiteDatabase.CONFLICT_IGNORE
            )
        }
    }

    companion object {
        private const val DATABASE_NAME = "finnote.db"
        private const val DATABASE_VERSION = 3

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
