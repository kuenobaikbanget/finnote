package com.app.finnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.finnote.model.Transaction

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Int,
    val date: String,
    val type: String,
    val category: String,
    val description: String
) {
    fun toModel(): Transaction {
        return Transaction(
            title = title,
            amount = amount,
            date = date,
            type = type,
            category = category,
            description = description
        )
    }

    companion object {
        fun fromModel(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                title = transaction.title,
                amount = transaction.amount,
                date = transaction.date,
                type = transaction.type,
                category = transaction.category,
                description = transaction.description
            )
        }
    }
}
