// File: HealthConnectManager.kt
package com.example.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.permission.PermissionController
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.request.ReadRecordsRequest
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectManager(val context: Context) {
    private val client = HealthConnectClient.getOrCreate(context)

    companion object {
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class)
        )
    }

    fun createPermContract() = PermissionController.createRequestPermissionResultContract()

    suspend fun hasPermissions(): Boolean =
        client.permissionController.getGrantedPermissions().containsAll(PERMISSIONS)

    suspend fun readStepsLast24h(): Long {
        val now = Instant.now()
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(now.minus(24, ChronoUnit.HOURS), now)
            )
        )
        return response.records.sumOf { it.count }
    }

    suspend fun readAvgHrLastHour(): Double {
        val now = Instant.now()
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(now.minus(1, ChronoUnit.HOURS), now)
            )
        )
        val samples = response.records.flatMap { it.samples }
        return if (samples.isEmpty()) 0.0 else samples.map { it.beatsPerMinute }.average()
    }
