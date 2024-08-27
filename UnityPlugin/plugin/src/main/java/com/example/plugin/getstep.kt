package com.example.plugin

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.records.StepsRecord
import kotlinx.coroutines.runBlocking
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.PermissionController
import androidx.activity.result.contract.ActivityResultContract

class getstep(context: Context) {

    //private val healthConnectClient = HealthConnectClient.getOrCreate(context)
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
    )

    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    fun getTodaySteps(): Int = runBlocking {
        val today = java.time.LocalDate.now()
        val startOfDay = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        val endOfDay = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()

        val steps = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = androidx.health.connect.client.time.TimeRangeFilter.between(startOfDay, endOfDay)
            )
        ).records

        return@runBlocking steps.sumOf { it.count.toInt() }
    }

}

/*
package com.example.plugin

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.permission.PermissionRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.records.StepsRecord
import kotlinx.coroutines.runBlocking
import android.util.Log

class getstep(private val context: Context) {

    private val healthConnectClient = HealthConnectClient.getOrCreate(context)

    suspend fun checkPermissions(): Boolean {
        val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
        val requiredPermissions = setOf(
            HealthPermission.getReadPermission(StepsRecord::class)
        )
        return requiredPermissions.all { it in grantedPermissions }
    }

    suspend fun requestPermissions() {
        val requiredPermissions = setOf(
            HealthPermission.getReadPermission(StepsRecord::class)
        )
        val request = PermissionRequest(requiredPermissions)
        healthConnectClient.permissionController.requestPermissions(request)
    }

    fun getTodaySteps(): Int = runBlocking {
        // 権限の確認とリクエスト
        if (!checkPermissions()) {
            Log.e("HealthConnect", "Required permissions are not granted.")
            requestPermissions()
            return@runBlocking 0
        }

        val today = java.time.LocalDate.now()
        val startOfDay = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        val endOfDay = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()

        val steps = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = androidx.health.connect.client.time.TimeRangeFilter.between(startOfDay, endOfDay)
            )
        ).records

        // デバッグログの追加
        steps.forEach {
            Log.d("HealthConnect", "Step count: ${it.count}")
        }

        return@runBlocking steps.sumOf { it.count.toInt() }
    }
}
*/