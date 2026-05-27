package com.app.finnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.finnote.data.local.entity.MonthlyBudgetEntity

@Dao
interface MonthlyBudgetDao {
    @Query("SELECT * FROM monthly_budgets")
    fun getAll(): List<MonthlyBudgetEntity>

    @Query("DELETE FROM monthly_budgets")
    fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(budget: MonthlyBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(budgets: List<MonthlyBudgetEntity>)
}
