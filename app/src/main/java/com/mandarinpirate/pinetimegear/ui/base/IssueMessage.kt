package com.mandarinpirate.pinetimegear.ui.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandarinpirate.pinetimegear.Const.ISSUE_ICON_PAINTER_RES
import com.mandarinpirate.pinetimegear.R
import com.mandarinpirate.pinetimegear.ui.entities.IssueLevel
import com.mandarinpirate.pinetimegear.ui.entities.IssueMessageUI
import com.mandarinpirate.pinetimegear.ui.entities.IssuePermissionStatus
import com.mandarinpirate.pinetimegear.ui.entities.AppIssueType
import com.mandarinpirate.pinetimegear.ui.theme.PineTimeGearCompanionAppTheme

@Composable
fun IssueMessage(
    modifier: Modifier = Modifier,
    issue: IssueMessageUI,
    onButtonClicked: () -> Unit,
    innerContentPadding: Dp = dimensionResource(R.dimen.issue_message_default_inner_content_padding)
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(innerContentPadding)
    ) {
        Row {
            Icon(
                modifier = Modifier.align(Alignment.CenterVertically),
                painter = painterResource(ISSUE_ICON_PAINTER_RES),
                contentDescription = null,
                tint = when(issue.type.level) {
                    IssueLevel.NOT_FIXABLE -> Color.Red
                    IssueLevel.FIXABLE, IssueLevel.WARNING -> Color.Yellow
                }
            )
            Spacer(Modifier.width(dimensionResource(R.dimen.issue_message_space_between_contents)))
            Text(
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                text = issue.message,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.issue_message_space_between_contents)))
            Button(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = onButtonClicked
            ) {
                val buttonText = when(issue.type.level){
                    IssueLevel.NOT_FIXABLE -> stringResource(R.string.issue_message_btn_show)
                    IssueLevel.FIXABLE -> stringResource(R.string.issue_message_btn_fix)
                    IssueLevel.WARNING -> stringResource(R.string.issue_message_btn_show)
                }
                Text(buttonText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IssueMessagePreview() {
    PineTimeGearCompanionAppTheme() {
        Scaffold { paddingValues ->
            val newIssues = mutableSetOf(
                IssueMessageUI(
                    message = stringResource(R.string.issue_bluetooth_is_not_supported),
                    type = AppIssueType.HardwareIssue.BluetoothIsNotAvailable
                ),
                IssueMessageUI(
                    message = stringResource(R.string.issue_ble_is_not_supported),
                    type = AppIssueType.HardwareIssue.BLEIsNotAvailable
                ),
                IssueMessageUI(
                    message = stringResource(R.string.issue_title_location_permission_is_not_granted),
                    type =  AppIssueType.Permissions.FineLocation(IssuePermissionStatus.NOT_GRANTED)
                ),
                IssueMessageUI(
                    message = stringResource(R.string.issue_title_bluetooth_permission_is_not_granted),
                    type =  AppIssueType.Permissions.Bluetooth(IssuePermissionStatus.NOT_GRANTED)
                )

            )
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(newIssues.toList()){ issue ->
                    IssueMessage(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth(),
                        issue = issue,
                        onButtonClicked = {},
                    )
                }
            }
        }
    }
}