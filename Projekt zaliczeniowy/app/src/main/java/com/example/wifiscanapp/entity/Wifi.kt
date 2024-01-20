package com.example.wifiscanapp.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Wifi(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name= "bssid") val bssid: String,
    @ColumnInfo(name = "ssid") val ssid: String,
    @ColumnInfo(name = "signal_lvl") val signalLvl: Int,
    @ColumnInfo(name = "frequency") val frequency: Int,
    @ColumnInfo(name="date") val date: String
)
