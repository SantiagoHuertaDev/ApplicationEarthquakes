package com.app.appearthquakes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class User(@field:ColumnInfo(name = "emal") @field:PrimaryKey var emal: String,
           @field:ColumnInfo(name="password") var password: String,
           @field:ColumnInfo(name = "name") var name: String,
           @field:ColumnInfo(name = "lastName") var lastName: String)
