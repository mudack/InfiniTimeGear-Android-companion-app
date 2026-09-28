package com.mandarinpirate.pinetimegear.ui.screens.scan

import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mandarinpirate.pinetimegear.Const.BLUETOOTH_ICON_PAINTER_RES
import com.mandarinpirate.pinetimegear.Const.ISSUE_ICON_PAINTER_RES
import com.mandarinpirate.pinetimegear.Const.URI_SCHEME_PACKAGE
import com.mandarinpirate.pinetimegear.R
import com.mandarinpirate.pinetimegear.ui.base.IssueAlertDialog
import com.mandarinpirate.pinetimegear.ui.base.IssueMessage
import com.mandarinpirate.pinetimegear.ui.base.util.LifecycleTracker
import com.mandarinpirate.pinetimegear.ui.base.util.WindowFocusTracker
import com.mandarinpirate.pinetimegear.ui.permissionGrantedHandler
import com.mandarinpirate.pinetimegear.ui.entities.AlertDialogData
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothDeviceUi
import com.mandarinpirate.pinetimegear.ui.entities.AppIssueType
import com.mandarinpirate.pinetimegear.ui.entities.IssueMessageUI
import com.mandarinpirate.pinetimegear.ui.entities.IssuePermissionStatus
import com.mandarinpirate.pinetimegear.ui.getAllPermissionWhatNeedForProperAppWork
import com.mandarinpirate.pinetimegear.ui.getBluetoothPermission
import com.mandarinpirate.pinetimegear.ui.getNotGrantedPermissions
import com.mandarinpirate.pinetimegear.ui.screens.ScreenRoute
import com.mandarinpirate.pinetimegear.ui.screens.ScreenRoute.OnboardingRoute
import com.mandarinpirate.pinetimegear.ui.theme.PineTimeGearCompanionAppTheme

@Composable
fun ScanScreen(openNextScreen: () -> Unit) {
    val viewModel: ScanViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val packageManager = context.packageManager

    LaunchedEffect(Unit) {//hardware checking, needs to be checked one time  //todo move it into viewModel
        val isBluetoothAvailable =
            packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        val isBluetoothLEAvailable =
            packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        viewModel.sendEvent(
            ScanEvent.InitBluetoothAvailability(
                isBluetoothAvailable = isBluetoothAvailable,
                isBluetoothLEAvailable = isBluetoothLEAvailable
            )
        )
        viewModel.sharedStateOpenNextScreen.collect { shouldOpenNextScreen ->
            if (shouldOpenNextScreen) openNextScreen()
        }
    }

    val allPerm = getAllPermissionWhatNeedForProperAppWork().toList()
    LifecycleTracker(
        onResume = {
            val notGrantedPermissions =
                context.getNotGrantedPermissions(allPerm)

            if (notGrantedPermissions.isNotEmpty()) {
                if (uiState.isItFirstOnResume) { //I added condition here to do not handle sendEvent every onResume cause if even user denied perm after he provides it will be first onResume again because android will recreate activity
                    viewModel.sendEvent(
                        ScanEvent.OnNotGrantedPermissions(
                            notGrantedPermissions
                        )
                    )
                    viewModel.sendEvent(ScanEvent.FirstOnResume)
                }else{
                    val res = allPerm.minus(notGrantedPermissions.toSet())
                    viewModel.sendEvent(ScanEvent.OnGrantedPermissions(res))
                }
            } else {
                viewModel.sendEvent(ScanEvent.OnGrantedPermissions(allPerm))
            }
        }
    )
    WindowFocusTracker(
        onFocusGained = {
            viewModel.sendEvent(
                ScanEvent.RequireToCheckIsBluetoothEnabled
            )
        }
    )

    PineTimeGearCompanionAppTheme {

        val bottomNavController = rememberNavController()
        val startDestination = BottomNavRoutes.PAIRED_DEVICES
        var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
        Scaffold( //https://developer.android.com/develop/ui/compose/components/navigation-bar
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                if (selectedDestination == BottomNavRoutes.SCAN_NEW_DEVICES.ordinal) {
                    ScanFAB(
                        isChipSelected = uiState.scrollToTheEnd,
                        fabState = uiState.fabState,
                        sendEvent = viewModel::sendEvent
                    )
                }
            },
            bottomBar = {
                BottomNavBar(
                    selectedDestination,
                    { newDestinationIndex: Int -> selectedDestination = newDestinationIndex })
            }
        ) { innerPadding ->
            Column(Modifier.padding(innerPadding)) {
                if (uiState.issues.isNotEmpty()) {
                    val issues = uiState.issues.map { mapIssueTypeToIssueMessageUI(it) }
                    LazyIssueContainer(issues = issues, sendEvent = viewModel::sendEvent)
                }
                ScanScreenContent(
                    uiState = uiState,
                    sendEvent = viewModel::sendEvent,
                    navController = bottomNavController,
                    selectedDestination = selectedDestination
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreenContent(
    uiState: ScanState = ScanState(),
    sendEvent: (ScanEvent) -> Unit = {},
    navController: NavHostController,
    selectedDestination: Int
) {
    when (BottomNavRoutes.entries[selectedDestination]) {
        BottomNavRoutes.PAIRED_DEVICES -> PairedDevicesContent(
            pairedDevices = uiState.pairedDevices,
            selectDevice = { deviceSelected(it, sendEvent) }
        )

        BottomNavRoutes.SCAN_NEW_DEVICES -> ScanNewDeviceContent(
            scannedDevices = uiState.scannedDevices,
            scrollToTheEnd = uiState.scrollToTheEnd,
            selectDevice = { deviceSelected(it, sendEvent) }
        )
    }

    if (uiState.alertDialogData != null) {
        IssueAlertDialog(alertDialogData = uiState.alertDialogData) {
            sendEvent(ScanEvent.DismissAlertDialog)
        }
    }
}

fun deviceSelected(
    bluetoothDeviceUi: BluetoothDeviceUi,
    sendEvent: (ScanEvent) -> Unit = {}
){
    sendEvent(ScanEvent.DeviceSelected(bluetoothDeviceUi))
}

@Composable
fun PairedDevicesContent(
    pairedDevices: Set<BluetoothDeviceUi>,
    selectDevice: (BluetoothDeviceUi) -> Unit
) {
    if (pairedDevices.isNotEmpty()) {
        val lazyListState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.ble_devices_list_space_between_items)),
            state = lazyListState
        ) {
            items(
                items = pairedDevices.toList(),
                key = { it.macAddress }
            ) { pairedDevice ->
                BleDevice(pairedDevice) { selectDevice(pairedDevice) }
            }
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.text_no_paired_devices))
        }
    }
}

@Composable
fun ScanNewDeviceContent(
    scannedDevices: Set<BluetoothDeviceUi>,
    scrollToTheEnd: Boolean,
    selectDevice: (BluetoothDeviceUi) -> Unit
) {
    if (scannedDevices.isNotEmpty()) {
        val lazyListState = rememberLazyListState()

        LaunchedEffect(scannedDevices.size, scrollToTheEnd) {
            if (scannedDevices.isNotEmpty() && scrollToTheEnd) {
                lazyListState.animateScrollToItem(scannedDevices.size - 1)
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.ble_devices_list_space_between_items)),
            state = lazyListState
        ) {
            items(
                items = scannedDevices.toList(),
                key = { it.macAddress }
            ) { scannedDevice ->
                BleDevice(scannedDevice) { selectDevice(scannedDevice) }
            }
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.text_no_scanned_devices))
        }
    }
}

@Composable
fun BleDevice(device: BluetoothDeviceUi, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.align(alignment = Alignment.CenterHorizontally)) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.scanned_device_list_item_horizontal_padding))
                    .clickable(enabled = true, onClick = onClick),
                text = device.name + "\n" + device.macAddress
            )
        }
    }
}

@Composable
fun LazyIssueContainer(
    modifier: Modifier = Modifier,
    issues: List<IssueMessageUI>,
    sendEvent: (ScanEvent) -> Unit = {}
) {
    val context = LocalContext.current
    val enableBluetoothContract = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        sendEvent(ScanEvent.TryToEnableBluetooth(result.resultCode == RESULT_OK))
    }

    val permissionContract = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionGrantedHandler(
            permissionMap = result,
            gratedPermissions = { sendEvent(ScanEvent.OnGrantedPermissions(it)) },
            notGrantedPermissions = { sendEvent(ScanEvent.OnDeniedPermissions(it)) },
        )
    }

    val onAlertBtnClick =
        { issueType: AppIssueType ->
            when (issueType) {
                AppIssueType.BluetoothIsNotEnabled -> {
                    val requiredPermissionsToEnableBluetooth =
                        context.getNotGrantedPermissions(getBluetoothPermission().toList())
                    if (requiredPermissionsToEnableBluetooth.isEmpty()) {
                        enableBluetoothContract.launch(
                            Intent(
                                BluetoothAdapter.ACTION_REQUEST_ENABLE
                            )
                        )
                    } else {
                        sendEvent(ScanEvent.EnablingBluetoothNoPermissionException)
                    }
                }

                is AppIssueType.HardwareIssue -> sendEvent(ScanEvent.DismissAlertDialog)
                is AppIssueType.Permissions -> {
                    when (issueType.status) { // cause of that we have only one contract for all perm(bluetooth and location) I've decided to do not double code and have written checking in this way
                        IssuePermissionStatus.NOT_GRANTED -> permissionContract.launch(
                            getAllPermissionWhatNeedForProperAppWork()
                        )

                        IssuePermissionStatus.DENIED -> {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data =
                                        Uri.fromParts(URI_SCHEME_PACKAGE, context.packageName, null)
                                }
                            context.startActivity(intent)
                        }
                    }
                    sendEvent(ScanEvent.DismissAlertDialog)
                }

                is AppIssueType.BluetoothScanIssue -> sendEvent(ScanEvent.DismissAlertDialog)
            }
        }

    IssueContainer(
        modifier,
        issues,
        onIssueBtnClick = { issue ->
            val alertDialogData = mapIssueToAlertDialogData(context, issue.type, btnOnClick = {
                onAlertBtnClick(issue.type)
            })
            sendEvent(
                ScanEvent.ShowUpAlertDialog(alertDialogData)
            )
        }
    )
}

@Composable
fun IssueContainer(
    modifier: Modifier,
    issues: List<IssueMessageUI>,
    onIssueBtnClick: (issue: IssueMessageUI) -> Unit,
) {
    val errorBorderColor: Color = MaterialTheme.colorScheme.errorContainer
    val shape = RoundedCornerShape(dimensionResource(R.dimen.issue_message_default_corner_size))

    Column(
        modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.issue_message_padding_size))
            .background(shape = shape, color = errorBorderColor)
    ) {
        LazyColumn(
            Modifier
                .padding(dimensionResource(R.dimen.issue_message_default_border_width))
                .background(shape = shape, color = MaterialTheme.colorScheme.background)
        ) {
            items(issues) { issueMessage ->

                IssueMessage(
                    issue = issueMessage,
                    onButtonClicked = {
                        onIssueBtnClick(issueMessage)
                    },
                )
            }
        }
    }
}

@Composable
fun ScanFAB(
    isChipSelected: Boolean,
    fabState: ScanScreenFabState,
    sendEvent: (ScanEvent) -> Unit,
) {
    val fabDimension = dimensionResource(R.dimen.fab_size)
    val fabIconDimension = dimensionResource(R.dimen.fab_icon_size)
    val activeChipRes = android.R.drawable.checkbox_on_background
    val inactiveChipRes = android.R.drawable.checkbox_off_background
    Column {
        FilterChip(
            onClick = { sendEvent(ScanEvent.ScrollDownChipClicked) },
            label = {
                Text(stringResource(R.string.chip_text_scroll_to_the_end))
            },
            selected = isChipSelected,
            leadingIcon = {
                Icon(
                    painter = painterResource(if (isChipSelected) activeChipRes else inactiveChipRes),
                    contentDescription = stringResource(R.string.scroll_down_chip_content_description_icon),
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.space_in_fab_container_between_chip_scroll_down_and_scan_button)))
        val issueExistDialogData = AlertDialogData(
            title = stringResource(R.string.alert_title_issue_exists),
            message = stringResource(R.string.alert_message_issue_exists),
            btnText = stringResource(android.R.string.ok),
            btnOnClick = { sendEvent(ScanEvent.DismissAlertDialog) }
        )
        FilledIconButton(
            modifier = Modifier.size(fabDimension),
            onClick = { sendEvent(ScanEvent.OnFabPressed(issueExistDialogData)) }
        ) {
            when (fabState) {
                ScanScreenFabState.ISSUE -> Icon(
                    modifier = Modifier.size(fabIconDimension),
                    painter = painterResource(ISSUE_ICON_PAINTER_RES),
                    tint = Color.Red,
                    contentDescription = stringResource(R.string.content_description_scan_button_requires_to_fix_issues)
                )

                ScanScreenFabState.READY_FOR_SCANNING -> Icon(
                    modifier = Modifier.size(fabIconDimension),
                    painter = painterResource(BLUETOOTH_ICON_PAINTER_RES),
                    tint = Color.Blue,
                    contentDescription = stringResource(R.string.content_description_scan_button_is_ready_to_start_scanning)
                )

                ScanScreenFabState.IS_SCANNING -> Box(
                    Modifier.size(fabIconDimension),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        Modifier.size(fabIconDimension),
                        color = Color.Blue
                    )
                    Icon(
                        painterResource(android.R.drawable.ic_media_pause),
                        contentDescription = stringResource(R.string.fab_icon_pause_description)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    selectedDestination: Int,
    selectNewDestination: (Int) -> Unit
) {
    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        BottomNavRoutes.entries.forEachIndexed { index, destination ->
            NavigationBarItem(
                selected = selectedDestination == index,
                onClick = {
                    selectNewDestination(index)
                },
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(destination.icon),
                        contentDescription = ""
                    )
                },
                label = { Text(stringResource(destination.label)) }
            )
        }
    }

}

//@Preview(showBackground = true)
//@Composable
//fun ScanScreenContentPreview() {
//    PineTimeGearCompanionAppTheme {
//        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//            ScanScreenContent(
//                Modifier.padding(innerPadding),
//                navController = navController,
//                startDestination = startDestination
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ScanScreenContentPreviewBluetoothIsNotAvailable() {
//    val newIssues: MutableSet<BluetoothIssueType> = mutableSetOf()
//    newIssues.add(BluetoothIssueType.HardwareIssue.BluetoothIsNotAvailable)
//    PineTimeGearCompanionAppTheme {
//        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//            ScanScreenContent(
//                Modifier.padding(innerPadding),
//                uiState = ScanState(issues = newIssues),
//                navController = navController,
//                startDestination = startDestination
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ScanScreenContentPreviewBLEIsNotAvailable() {
//    val newIssues: MutableSet<BluetoothIssueType> = mutableSetOf()
//    newIssues.add(BluetoothIssueType.HardwareIssue.BLEIsNotAvailable)
//    PineTimeGearCompanionAppTheme {
//        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//            ScanScreenContent(
//                Modifier.padding(innerPadding),
//                uiState = ScanState(issues = newIssues),
//                navController = navController,
//                startDestination = startDestination
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ScanScreenContentPreviewBluetoothAndBLEAreNotAvailable() {
//    val newIssues: MutableSet<BluetoothIssueType> = mutableSetOf()
//    newIssues.add(BluetoothIssueType.HardwareIssue.BLEIsNotAvailable)
//    newIssues.add(BluetoothIssueType.HardwareIssue.BluetoothIsNotAvailable)
//    PineTimeGearCompanionAppTheme {
//        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//            ScanScreenContent(
//                Modifier.padding(innerPadding),
//                uiState = ScanState(issues = newIssues),
//                navController = navController,
//                startDestination = startDestination
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ScanScreenContentPreviewFullOfIssue() {
//    val newIssues: MutableSet<BluetoothIssueType> = mutableSetOf()
//    newIssues.add(BluetoothIssueType.HardwareIssue.BLEIsNotAvailable)
//    newIssues.add(BluetoothIssueType.HardwareIssue.BluetoothIsNotAvailable)
//    newIssues.add(BluetoothIssueType.BluetoothIsNotEnabled)
//    PineTimeGearCompanionAppTheme {
//        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//            ScanScreenContent(
//                Modifier.padding(innerPadding),
//                uiState = ScanState(issues = newIssues),
//                navController = navController,
//                startDestination = startDestination
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ScanScreenContentPreviewScanningInPrecess() {
//    PineTimeGearCompanionAppTheme {
//        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//            ScanScreenContent(
//                Modifier.padding(innerPadding),
//                uiState = ScanState(fabState = ScanScreenFabState.IS_SCANNING),
//                navController = navController,
//                startDestination = startDestination
//            )
//        }
//    }
//}