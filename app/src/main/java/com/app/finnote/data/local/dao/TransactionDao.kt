package com.app.finnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.finnote.data.local.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY id ASC")
    fun getAll(): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions")
    fun count(): Int

    @Query("DELETE FROM transactions")
    fun clear()

    @Insert
    fun insertAll(transactions: List<TransactionEntity>)
}
