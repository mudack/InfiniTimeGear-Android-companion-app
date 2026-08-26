package com.mandarinpirate.pinetimegear.ui.screens._main_activity

import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinpirate.pinetimegear.Const.BLUETOOTH_ICON_PAINTER_RES
import com.mandarinpirate.pinetimegear.Const.ISSUE_ICON_PAINTER_RES
import com.mandarinpirate.pinetimegear.Const.URI_SCHEME_PACKAGE
import com.mandarinpirate.pinetimegear.R
import com.mandarinpirate.pinetimegear.ui.base.IssueAlertDialog
import com.mandarinpirate.pinetimegear.ui.base.IssueMessage
import com.mandarinpirate.pinetimegear.ui.base.util.LifecycleTracker
import com.mandarinpirate.pinetimegear.ui.base.util.WindowFocusTracker
import com.mandarinpirate.pinetimegear.ui.base.util.bluetoothIssueToAlertDialogMapper
import com.mandarinpirate.pinetimegear.ui.base.util.bluetoothIssueTypeToIssueMessageMapper
import com.mandarinpirate.pinetimegear.ui.bluetoothPermissionGrantedHandler
import com.mandarinpirate.pinetimegear.ui.entities.AlertDialogData
import com.mandarinpirate.pinetimegear.ui.entities.IssueMessageUI
import com.mandarinpirate.pinetimegear.ui.entities.IssuePermissionStatus
import com.mandarinpirate.pinetimegear.ui.entities.IssueType
import com.mandarinpirate.pinetimegear.ui.getBluetoothPermission
import com.mandarinpirate.pinetimegear.ui.getNotGrantedPermissions
import com.mandarinpirate.pinetimegear.ui.putUsingClassAsAKey
import com.mandarinpirate.pinetimegear.ui.theme.PineTimeGearCompanionAppTheme
import com.mandarinpirate.pinetimegear.ui.screens._main_activity.MainScreenFabState
import com.mandarinpirate.pinetimegear.ui.screens._main_activity.MainState
import dagger.hilt.android.AndroidEntryPoint
import kotlin.reflect.KClass

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PineTimeGearCompanionAppTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current
                val packageManager = context.packageManager

                val activeChipRes = android.R.drawable.checkbox_on_background
                val inactiveChipRes = android.R.drawable.checkbox_off_background

                LaunchedEffect(Unit) {//hardware checking, needs to be checked one time
                    val isBluetoothAvailable =
                        packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
                    val isBluetoothLEAvailable =
                        packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                    viewModel.sendEvent(
                        MainEvent.InitBluetoothAvailability(
                            isBluetoothAvailable = isBluetoothAvailable,
                            isBluetoothLEAvailable = isBluetoothLEAvailable
                        )
                    )
                }
                LifecycleTracker(
                    onResume = {
                        val deniedPermissions =
                            context.getNotGrantedPermissions(getBluetoothPermission().toList())
                        if (deniedPermissions.isNotEmpty()) {
                            if (uiState.isItFirstOnResume) { //i added condition here to do not handle sendEvent every onResume cause if even user denie perm after he provide it will be first onResume again because android will recreate activity
                                viewModel.sendEvent(
                                    MainEvent.OnNotGrantedPermissions(
                                        deniedPermissions
                                    )
                                )
                                viewModel.sendEvent(MainEvent.FirstOnResume)
                            }
                        } else {
                            viewModel.sendEvent(MainEvent.OnAllPermissionAreGranted)
                        }
                    }
                )
                WindowFocusTracker(
                    onFocusGained = {
                        viewModel.sendEvent(
                            MainEvent.RequireToCheckIsBluetoothEnabled
                        )
                    }
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        val fabDimension = dimensionResource(R.dimen.fab_size)
                        val fabIconDimension = dimensionResource(R.dimen.fab_icon_size)
                        val isChipSelected = uiState.scrollToTheEnd
                        Column {
                            FilterChip(
                                onClick = { viewModel.sendEvent(MainEvent.ScrollDownChipClicked) },
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
                            FilledIconButton(
                                modifier = Modifier.size(fabDimension),
                                onClick = { viewModel.sendEvent(MainEvent.OnFabPressed) }
                            ) {
                                when (uiState.fabState) {
                                    MainScreenFabState.ISSUE -> Icon(
                                        modifier = Modifier.size(fabIconDimension),
                                        painter = painterResource(ISSUE_ICON_PAINTER_RES),
                                        tint = Color.Red,
                                        contentDescription = stringResource(R.string.content_description_scan_button_requires_to_fix_issues)
                                    )

                                    MainScreenFabState.READY_FOR_SCANNING -> Icon(
                                        modifier = Modifier.size(fabIconDimension),
                                        painter = painterResource(BLUETOOTH_ICON_PAINTER_RES),
                                        tint = Color.Blue,
                                        contentDescription = stringResource(R.string.content_description_scan_button_is_ready_to_start_scanning)
                                    )

                                    MainScreenFabState.IS_SCANNING -> Box(
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
                ) { innerPadding ->
                    MainScreenContent(
                        modifier = Modifier.padding(innerPadding),
                        uiState = uiState,
                        sendEvent = viewModel::sendEvent
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    uiState: MainState = MainState(),
    sendEvent: (MainEvent) -> Unit = {}
) {
    Column(modifier) {
        if (uiState.issues.isNotEmpty()) {
            val issues =
                uiState.issues.toList().map { bluetoothIssueTypeToIssueMessageMapper(it.second) }
            LazyIssueContainer(issues = issues, sendEvent = sendEvent)
        }
        if (uiState.scannedDevices.isNotEmpty()) {
            val lazyListState = rememberLazyListState()

            LaunchedEffect(uiState.scannedDevices.size, uiState.scrollToTheEnd) {
                if (uiState.scannedDevices.isNotEmpty() && uiState.scrollToTheEnd) {
                    lazyListState.animateScrollToItem(uiState.scannedDevices.size - 1)
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.ble_devices_list_space_between_items)),
                state = lazyListState
            ) {
                items(
                    items = uiState.scannedDevices,
                    key = { it.address }
                ) { scannedDevice ->
                    Card(Modifier.fillMaxWidth()) {
                        Row {
                            Text(
                                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.scanned_device_list_item_horizontal_padding)),
                                text = scannedDevice.name + "\n" + scannedDevice.address
                            )
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.text_no_scanned_devices))
            }
        }
        if (uiState.alertDialog != null) {
            IssueAlertDialog(data = uiState.alertDialog) {
                sendEvent(MainEvent.DismissAlertDialog)
            }
        }
    }
}

@Composable
fun LazyIssueContainer(
    modifier: Modifier = Modifier,
    issues: List<IssueMessageUI>,
    sendEvent: (MainEvent) -> Unit = {}
) {
    val context = LocalContext.current
    val enableBluetoothContract = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        sendEvent(MainEvent.TryToEnableBluetooth(result.resultCode == RESULT_OK))
    }

    val bluetoothPermissionContract = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        bluetoothPermissionGrantedHandler(
            permissionMap = result,
            allGranted = { sendEvent(MainEvent.OnAllPermissionAreGranted) },
            notAllGranted = { sendEvent(MainEvent.OnDeniedPermissions(it)) },
        )
    }
    val onAlertBtnClick =
        { issueType: IssueType ->  //todo this code is hard to read and required to be simplified
            //todo btnOnClick set in another way, i don't like this way
            when (issueType) {
                IssueType.BluetoothScanIssue.BluetoothIsNotEnabled -> {
                    val requiredPermissionsToEnableBluetooth =
                        context.getNotGrantedPermissions(getBluetoothPermission().toList())
                    if (requiredPermissionsToEnableBluetooth.isEmpty()) {
                        enableBluetoothContract.launch(
                            Intent(
                                BluetoothAdapter.ACTION_REQUEST_ENABLE
                            )
                        )
                    } else {
                        sendEvent(MainEvent.EnablingBluetoothNoPermissionException)
                    }
                }

                is IssueType.HardwareIssue -> sendEvent(MainEvent.DismissAlertDialog)
                is IssueType.Permissions -> {
                    when (issueType.status) { // cause of that we have only one contract for all perm(bluetooth and location) i've decided to do not double code and have written checking in this way
                        IssuePermissionStatus.NOT_GRANTED -> bluetoothPermissionContract.launch(
                            getBluetoothPermission()
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
                    sendEvent(MainEvent.DismissAlertDialog)
                }

                is IssueType.BluetoothScanIssue -> sendEvent(MainEvent.DismissAlertDialog)
            }
        }

    IssueContainer(
        modifier,
        issues,
        onIssueBtnClick = { alertDialogData, issue ->
            sendEvent(
                MainEvent.ShowUpAlertDialog(//todo btnOnClick set in another way, i don't like this way
                    alertDialogData.copy(btnOnClick = { onAlertBtnClick(issue.type) })
                )
            )
        }
    )
}

@Composable
fun IssueContainer(
    modifier: Modifier,
    issues: List<IssueMessageUI>,
    onIssueBtnClick: (AlertDialogData, issue: IssueMessageUI) -> Unit,
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

                val alertData: AlertDialogData = bluetoothIssueToAlertDialogMapper(issueMessage)

                IssueMessage(
                    issue = issueMessage,
                    onButtonClicked = {
                        onIssueBtnClick(alertData, issueMessage)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenContentPreview() {
    PineTimeGearCompanionAppTheme() {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainScreenContent(Modifier.padding(innerPadding))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenContentPreviewBluetoothIsNotAvailable() {
    val newIssues: MutableMap<KClass<out IssueType>, IssueType> = mutableMapOf()
    newIssues.putUsingClassAsAKey(IssueType.HardwareIssue.BluetoothIsNotAvailable)
    PineTimeGearCompanionAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainScreenContent(
                Modifier.padding(innerPadding),
                uiState = MainState(issues = newIssues)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenContentPreviewBLEIsNotAvailable() {
    val newIssues: MutableMap<KClass<out IssueType>, IssueType> = mutableMapOf()
    newIssues.putUsingClassAsAKey(IssueType.HardwareIssue.BLEIsNotAvailable)
    PineTimeGearCompanionAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainScreenContent(
                Modifier.padding(innerPadding),
                uiState = MainState(issues = newIssues)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenContentPreviewBluetoothAndBLEAreNotAvailable() {
    val newIssues: MutableMap<KClass<out IssueType>, IssueType> = mutableMapOf()
    newIssues.putUsingClassAsAKey(
        IssueType.HardwareIssue.BLEIsNotAvailable,
        IssueType.HardwareIssue.BluetoothIsNotAvailable
    )
    PineTimeGearCompanionAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainScreenContent(
                Modifier.padding(innerPadding),
                uiState = MainState(issues = newIssues)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenContentPreviewFullOfIssue() {
    val newIssues: MutableMap<KClass<out IssueType>, IssueType> = mutableMapOf()
    newIssues.putUsingClassAsAKey(
        IssueType.HardwareIssue.BLEIsNotAvailable,
        IssueType.HardwareIssue.BluetoothIsNotAvailable,
        IssueType.BluetoothScanIssue.BluetoothIsNotEnabled,
    )
    PineTimeGearCompanionAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainScreenContent(
                Modifier.padding(innerPadding),
                uiState = MainState(issues = newIssues)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenContentPreviewScanningInPrecess() {
    PineTimeGearCompanionAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainScreenContent(
                Modifier.padding(innerPadding),
                uiState = MainState(fabState = MainScreenFabState.IS_SCANNING)
            )
        }
    }
}