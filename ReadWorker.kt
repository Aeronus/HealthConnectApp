// File: ReadWorker.kt
package com.example.healthconnect

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReadWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val mgr = HealthConnectManager(applicationContext)
        if (!mgr.hasPermissions()) return Result.retry()
        val steps = mgr.readStepsLast24h()
        val hr = mgr.readAvgHrLastHour()
        Log.i("ReadWorker", "Steps=$steps, AvgHR=$hr")
        return Result.success()
    }
}
