package com.app.finnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.finnote.data.local.entity.AppPreferenceEntity

@Dao
interface AppPreferenceDao {
    @Query("SELECT value FROM app_preferences WHERE `key` = :key LIMIT 1")
    fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(preference: AppPreferenceEntity)
}
