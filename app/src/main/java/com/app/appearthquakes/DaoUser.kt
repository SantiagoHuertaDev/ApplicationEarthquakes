package com.app.appearthquakes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DaoUser {
    @Query("SELECT * FROM user")
    fun getUsers(): List<User?>?

    @Insert
    fun insertUser(vararg usuarios: User?)
}

