package com.app.finnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.finnote.data.local.entity.UserProfileEntity

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun get(id: Int = UserProfileEntity.SINGLE_USER_ID): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(user: UserProfileEntity)
}
