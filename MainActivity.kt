// File: MainActivity.kt
package com.example.healthconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.launch
import androidx.activity.result.ActivityResultLauncher
import androidx.work.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private lateinit var mgr: HealthConnectManager
    private lateinit var permLauncher: ActivityResultLauncher<Set<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mgr = HealthConnectManager(this)
        permLauncher = registerForActivityResult(mgr.createPermContract()) { granted ->
            if (granted.containsAll(HealthConnectManager.PERMISSIONS)) {
                // Permissions granted
            }
        }

        // Schedule periodic worker
        val work = PeriodicWorkRequestBuilder<ReadWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("health_read", ExistingPeriodicWorkPolicy.KEEP, work)

        setContent {
            var steps by remember { mutableStateOf(0L) }
            var hr by remember { mutableStateOf(0.0) }

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = { lifecycleScope.launch { if(!mgr.hasPermissions()) permLauncher.launch(HealthConnectManager.PERMISSIONS) } }) {
                    Text("Permissions anfordern")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    lifecycleScope.launch {
                        steps = mgr.readStepsLast24h()
                        hr = mgr.readAvgHrLastHour()
                    }
                }) {
                    Text("Daten auslesen")
                }
                Spacer(Modifier.height(16.dp))
                Text("Schritte (24h): $steps")
                Text("Ø Herzfrequenz (1h): ${"%.1f".format(hr)}")
            }
        }
    }
}
