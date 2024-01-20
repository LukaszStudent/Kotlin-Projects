package com.example.wifiscanapp.serviecs

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.compose.ui.Alignment
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wifiscanapp.R
import com.example.wifiscanapp.database.AppDatabase
import com.example.wifiscanapp.entity.Wifi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun WifiItem(wifi: Wifi, info: WifiInfo, navController: NavHostController) {
    var rssi by remember {
        mutableIntStateOf(wifi.signalLvl)
    }
    val estimateRange by remember {
        mutableStateOf(EstimateRange(rssi = rssi, wifi.frequency))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = if (info.bssid == wifi.bssid) {
            CardDefaults.cardColors(
                containerColor = Color.Green,
            )
        } else {
            CardDefaults.cardColors(
                containerColor = Color.LightGray,
            )
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        Row() {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = "SSID: ${wifi.ssid}")
                Text(text = "BSSID: ${wifi.bssid}")
                Text(text = "RSSI: $rssi")
                if (info.bssid == wifi.bssid) {
                    Text(text = "Link Speed: ${info.linkSpeed}${WifiInfo.LINK_SPEED_UNITS}")
                }
                Text(text = "Częstotliwość: ${wifi.frequency} MHz")
                Text(text = "Estymowana odległość: ${"%.2f".format(estimateRange.calculateRange())} m")
            }
            Spacer(modifier = Modifier.width(8.dp))
            ShowMoreButton(navController = navController, wifiBssid = wifi.bssid)
        }
    }
}

@Composable
fun ShowMoreButton(
    navController: NavHostController,
    wifiBssid: String,
) {

    IconButton(
        onClick = {

            navController.navigate("chart_page/${wifiBssid}")
        },
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = null
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiList(
    wifiList: List<Wifi>,
    wifiInfo: WifiInfo,
    navController: NavHostController,
    nextScanTime: Int
) {

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }) {
        it
        LazyColumn(modifier = Modifier.padding(it)) {
            item {
                Text(text = "Liczba zeskanowanych sieci: ${wifiList.size}")
                Text(text = "Czas do następnego skanowania: ${nextScanTime.toDouble()}")
            }
            items(wifiList) {
                WifiItem(wifi = it, info = wifiInfo, navController = navController)
            }
        }
    }

}


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WifiPermission(
    context: Context,
    database: AppDatabase,
    navController: NavHostController
) {
    var wifiManager =
        context.getSystemService(ComponentActivity.WIFI_SERVICE) as WifiManager
    val wifiPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE
        )
    )

    /*
    Uzycie metody connectionInfo z klasy WifiManager, pomimo ze jest oznaczona
    jako deprecated ze wzgledu na poprawnosc jej wynikow.

    Uzycie klasy ConnectivityManager w celu uzyskania informacji o sieci
    daje błędne wyniki.
    */

    if (wifiPermissionState.allPermissionsGranted) {
        val wifiInfo: WifiInfo = wifiManager.connectionInfo
        var isScanning by remember {
            mutableStateOf(true)
        }
        var wifiScanResults by remember {
            mutableStateOf(wifiManager.scanResults)
        }
        var listOfWifi by rememberSaveable {
            mutableStateOf(emptyList<Wifi>())
        }

        var nextScanTime by rememberSaveable {
            mutableIntStateOf(10)
        }

        var progrssBarText by rememberSaveable {
            mutableStateOf("Zaczynam skanować...")
        }

        suspend fun decrementTime(periodScan: Int) {
            for (i in periodScan downTo 0) {
                delay(1000)
                nextScanTime--
                if (nextScanTime == 0) nextScanTime = 10
            }

        }

        suspend fun updateLinearProgressBarText() {
            delay(2500)
            progrssBarText = "Jeszcze chwilka..."
            delay(2500)
            progrssBarText = "Już prawie gotowe..."
            delay(2500)
            progrssBarText = "Zaczynamy"
            delay(2500)
        }

        LaunchedEffect(key1 = wifiScanResults) {
            while (isScanning) {
                coroutineScope {
                    launch { decrementTime(10) }
                    launch { updateLinearProgressBarText() }
                }

                val wifiScanReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val success =
                            intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                        if (success) {
                            listOfWifi = scanSuccess(wifiManager = wifiManager, db = database)
                        } else {
                            scanFailure(wifiManager = wifiManager)
                        }
                    }
                }
                val intentFilter = IntentFilter()
                intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                context.registerReceiver(wifiScanReceiver, intentFilter)
                val success = wifiManager.startScan()
                if (!success) {
                    scanFailure(wifiManager = wifiManager)
                }
            }
        }
        DisposableEffect(Unit) {//przy usuwaniu Composa, czyli w momencie przejscia do wykresow, zatrzymuje sie petla
            onDispose { isScanning = false }
        }
        if (listOfWifi.isEmpty()) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator()
                Text(text = progrssBarText)
            }

        } else {
            WifiList(
                wifiList = listOfWifi,
                wifiInfo = wifiInfo,
                navController = navController,
                nextScanTime = nextScanTime
            )
        }


    } else {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val textToShow = if (wifiPermissionState.shouldShowRationale) {
                "Brak zezwolenia na korzystanie z lokalizacji. Aplikacja nie będzie działać popranie"
            } else {
                "Korzystanie z aplikacji wymaga zezowlenia na dostęp do lokalizacji"
            }
            Text(textToShow)
            Button(onClick = { wifiPermissionState.launchMultiplePermissionRequest() }) {
                Text("Nadaj pozwolenie")
            }
        }
    }
}


@SuppressLint("MissingPermission")
private fun scanSuccess(wifiManager: WifiManager, db: AppDatabase): List<Wifi> {
    val results = wifiManager.scanResults
    val wifiDao = db.wifiDao()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val currentDateTime = LocalDateTime.now().format(formatter)
    var listOfWifi = results.mapIndexed { index, value ->
        Wifi(
            0,
            value.BSSID,
            value.SSID, //korzystanie z SSID ze wzgledu na poprawnosc wynikow
            value.level,
            value.frequency,
            currentDateTime
        )
    }
    wifiDao.insertAll(listOfWifi)
    return listOfWifi
}

@SuppressLint("MissingPermission")
private fun scanFailure(wifiManager: WifiManager) {
    // handle failure: new scan did NOT succeed
    // consider using old scan results: these are the OLD results!
    val results = wifiManager.scanResults
}
