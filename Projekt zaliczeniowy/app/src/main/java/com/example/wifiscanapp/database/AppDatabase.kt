package com.example.wifiscanapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wifiscanapp.dao.WifiDao
import com.example.wifiscanapp.entity.Wifi

@Database(entities = [Wifi::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wifiDao(): WifiDao
}
