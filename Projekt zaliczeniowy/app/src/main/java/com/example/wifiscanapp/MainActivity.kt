package com.example.wifiscanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.wifiscanapp.database.AppDatabase
import com.example.wifiscanapp.pages.Plotting
import com.example.wifiscanapp.serviecs.WifiPermission
import com.example.wifiscanapp.ui.theme.WiFiScanAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).allowMainThreadQueries().build()


        super.onCreate(savedInstanceState)
        setContent {
            WiFiScanAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "permission_page") {
                        composable("permission_page") {
//                            WifiPermission(wifiManager = wifiManager, database = db, navController)
                            WifiPermission(context = applicationContext, database = db, navController)
                        }
                        composable("chart_page/{bssid}") { backStackEntry ->
                            val bssid = backStackEntry.arguments?.getString("bssid") ?: "Error"
                            Plotting(navController = navController, currentBssid = bssid, database = db)
                        }
                    }
                }
            }
        }
    }
}




