package com.example.wifiscanapp.serviecs

import java.lang.Math.pow

class EstimateRange(rssi: Int, frequencyMHz: Int) {
    private val rssi = kotlin.math.abs(rssi)
    private val freq = frequencyMHz.toDouble()
    /*
    Model propagacyjny do estymacji odleglosci do AP
    to model One Slope.
    */
    private fun tlumienieWWolnejPrzestrzeniDla1m(): Double{
        return -27.55+20* kotlin.math.log10(freq) +20* kotlin.math.log10(1.0)
    }

    fun calculateRange(): Double {
        var L0 = tlumienieWWolnejPrzestrzeniDla1m()
        val y=3.5
        val power=(rssi-L0)/(10*y)
        return pow(10.0,power)
    }
}