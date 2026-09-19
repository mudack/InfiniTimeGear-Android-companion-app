package com.mandarinpirate.pinetimegear.ui.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.mandarinpirate.pinetimegear.R
import com.mandarinpirate.pinetimegear.ui.entities.AlertDialogData
import com.mandarinpirate.pinetimegear.ui.theme.PineTimeGearCompanionAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueAlertDialog(
    modifier: Modifier = Modifier,
    alertDialogData: AlertDialogData,
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier
                .width(dimensionResource(R.dimen.issue_alert_dialog_default_width))
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.issue_alert_dialog_default_content_padding))
            ) {
                Text(text = alertDialogData.title, style = MaterialTheme.typography.titleLarge)
                Text(text = alertDialogData.message, style = MaterialTheme.typography.bodyLarge)
                Button(
                    modifier = Modifier.align(Alignment.End),
                    onClick = alertDialogData.btnOnClick
                        ?: throw RuntimeException("lambda was not provided for alertDialog, please don't forget to make alertDialog state null to hide the alertDialog")
                ) {
                    Text(alertDialogData.btnText)
                }
            }
        }
    }
}

@Preview
@Composable
fun IssueAlertDialogPreview() {
    PineTimeGearCompanionAppTheme() {
        var showDialog by remember { mutableStateOf(false) }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = {
                showDialog = true
            }) { Text("show dialog") }
        }
        if (showDialog) {
            IssueAlertDialog(
                alertDialogData = AlertDialogData(
                    title = "Title",
                    message = "Message",
                    btnText = "Button Text",
                    btnOnClick = {}
                ),
                onDismissRequest = { showDialog = false }
            )
        }
    }
}