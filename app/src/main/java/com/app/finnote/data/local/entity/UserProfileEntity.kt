package com.app.finnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.finnote.model.User

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = SINGLE_USER_ID,
    val name: String,
    val email: String,
    val joinedDate: String
) {
    fun toModel(): User {
        return User(
            name = name,
            email = email,
            joinedDate = joinedDate
        )
    }

    companion object {
        const val SINGLE_USER_ID = 1

        fun fromModel(user: User): UserProfileEntity {
            return UserProfileEntity(
                name = user.name,
                email = user.email,
                joinedDate = user.joinedDate
            )
        }
    }
}
