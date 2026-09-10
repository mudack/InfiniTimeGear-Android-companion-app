package com.mandarinpirate.pinetimegear.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.mandarinpirate.pinetimegear.ui.entities.IssuePermissionStatus
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothIssueType
import kotlin.reflect.KClass

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

fun getPermissionRequiredForBleScan() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    Manifest.permission.BLUETOOTH_SCAN
} else {
    Manifest.permission.ACCESS_FINE_LOCATION
}


fun bluetoothPermissionGrantedHandler(
    permissionMap: Map<String, @JvmSuppressWildcards Boolean>,
    allGranted: () -> Unit,
    notAllGranted: (notGrantedPermissions: List<String>) -> Unit
) {
    val notGrantedPermissions =
        permissionMap.filter { !it.value }.map { it.key } //filter only not granted permissions

    if (notGrantedPermissions.isEmpty()) allGranted()
    else notAllGranted(notGrantedPermissions)
}

fun getIssueTypeByPermission(
    notGrantedPermission: List<String>, permissionStatus: IssuePermissionStatus
): Map<KClass<out BluetoothIssueType>, BluetoothIssueType> {
    val result = emptyMap<KClass<out BluetoothIssueType>, BluetoothIssueType>().toMutableMap()

    notGrantedPermission.forEach {
        result.putUsingClassAsAKey(
            when (it) {
                Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN -> BluetoothIssueType.Permissions.Bluetooth(
                    permissionStatus
                )

                Manifest.permission.ACCESS_FINE_LOCATION -> BluetoothIssueType.Permissions.FineLocation(
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
        if (ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED) {
            notGrantedPermissions.add(it)
        }
    }
    return notGrantedPermissions
}

/**
 * The extension func below need for cases when we want to put or remove issue
 * using their ::class as the key
 * */
fun MutableMap<KClass<out BluetoothIssueType>, BluetoothIssueType>.putUsingClassAsAKey(value: BluetoothIssueType) {
    this[value::class] = value
}

fun MutableMap<KClass<out BluetoothIssueType>, BluetoothIssueType>.putUsingClassAsAKey(valueList: List<BluetoothIssueType>) {
    valueList.forEach { value ->
        this[value::class] = value
    }
}

fun MutableMap<KClass<out BluetoothIssueType>, BluetoothIssueType>.putUsingClassAsAKey(vararg values: BluetoothIssueType) {
    values.forEach { value ->
        this[value::class] = value
    }
}

fun MutableMap<KClass<out BluetoothIssueType>, BluetoothIssueType>.removeUsingClassAsAKey(value: BluetoothIssueType) {
    this.remove(value::class)
}