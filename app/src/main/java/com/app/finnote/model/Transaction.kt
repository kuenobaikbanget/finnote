package com.app.finnote.model

import android.database.Cursor

data class Transaction(
	val title: String,
	val amount: Int,
	val date: String,
	val type: String,
	val category: String = "",
	val description: String = "",
	val id: Int = 0
) {
	companion object {
		fun fromCursor(cursor: Cursor): Transaction {
			return Transaction(
				id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
				title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
				amount = cursor.getInt(cursor.getColumnIndexOrThrow("amount")),
				date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
				type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
				category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
				description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
			)
		}
	}
}
