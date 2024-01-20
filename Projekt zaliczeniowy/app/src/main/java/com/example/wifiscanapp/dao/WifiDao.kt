package com.example.wifiscanapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.wifiscanapp.entity.Wifi

@Dao
interface WifiDao {
    @Query("SELECT * FROM Wifi")
    fun getAllWifiEntity(): List<Wifi>

    @Query("SELECT * FROM Wifi WHERE bssid=:bssid")
    fun loadByBssid(bssid:String): List<Wifi>

    @Query("Select signal_lvl from wifi where bssid=:bssid")
    fun loadRssiByBssid(bssid:String): List<Int>
    @Insert
    fun insert(wifi: Wifi)

    @Insert
    fun insertAll(wifiList: List<Wifi>)
    @Delete
    fun delete(wifi: Wifi)
}