package com.example.angocooking.Data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson

class UserConverter {
    @TypeConverter
    fun userToString(user: User?): String? {
        return user?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun stringToUser(string: String?): User? {
        return string?.let { Gson().fromJson(it, User::class.java) }
    }
}

@Entity(tableName = "auth_data")
data class AuthData(
    @PrimaryKey
    val id: Int = 1,
    val token: String?,
    val user: User?
)