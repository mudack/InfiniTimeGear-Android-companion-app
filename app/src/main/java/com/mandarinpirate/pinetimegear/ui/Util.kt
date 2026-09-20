package com.mandarinpirate.pinetimegear.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.mandarinpirate.pinetimegear.ui.entities.IssuePermissionStatus
import com.mandarinpirate.pinetimegear.ui.entities.AppIssueType

fun getAllPermissionWhatNeedForProperAppWork(): Array<String> {
    var allRequiredPerm: Array<String> = getBluetoothPermission()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        allRequiredPerm = allRequiredPerm.plus(Manifest.permission.POST_NOTIFICATIONS)
    }
    return allRequiredPerm
}

fun getBluetoothPermission(): Array<String> = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
    arrayOf(
        Manifest.permission.BLUETOOTH, //is necessary to perform any Bluetooth classic or BLE communication, such as requesting a connection, accepting a connection, and transferring data.
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION //is necessary because, on Android 11 and lower, a Bluetooth scan could potentially be used to gather information about the location of the user.
    )
} else {
    arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
    )
}

fun Context.checkSelfAllPermissions(permissions: List<String>): Boolean {
    permissions.forEach {
        val isGranted = checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) return@checkSelfAllPermissions false
    }
    return true
}


fun permissionGrantedHandler(
    permissionMap: Map<String, @JvmSuppressWildcards Boolean>,
    gratedPermissions: (grantedPermissions: List<String>) -> Unit,
    notGrantedPermissions: (notGrantedPermissions: List<String>) -> Unit
) {
    val notGrantedPermissions =
        permissionMap.filter { !it.value }.map { it.key } //filter only granted permissions{
    val grantedPermissions =
        permissionMap.filter { it.value }.map { it.key } //filter only not granted permissions

    if (grantedPermissions.isNotEmpty()) gratedPermissions(grantedPermissions)
    if (notGrantedPermissions.isNotEmpty()) notGrantedPermissions(notGrantedPermissions)
}

fun getIssueTypeByPermission(
    notGrantedPermission: List<String>, permissionStatus: IssuePermissionStatus
): Set<AppIssueType> {
    val result = emptySet<AppIssueType>().toMutableSet()

    notGrantedPermission.forEach {
        result.add(
            when (it) {
                Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN -> AppIssueType.Permissions.Bluetooth(
                    permissionStatus
                )

                Manifest.permission.ACCESS_FINE_LOCATION -> AppIssueType.Permissions.FineLocation(
                    permissionStatus
                )

                Manifest.permission.POST_NOTIFICATIONS -> AppIssueType.Permissions.PostNotification(
                    permissionStatus
                )

                else -> throw IllegalStateException("You tried to get issue message by not expected permission, please define this new exception in code or delete it, otherwise this exception will be threw again and again")
            }
        )
    }
    return result
}

fun Context.getNotGrantedPermissions(permissionList: List<String>): List<String> {
    val notGrantedPermissions = mutableListOf<String>()
    permissionList.forEach {
        if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
            notGrantedPermissions.add(it)
        }
    }
    return notGrantedPermissions
}