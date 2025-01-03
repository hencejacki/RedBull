package com.newyear.redbull.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.newyear.redbull.R
import com.newyear.redbull.model.AppViewModel
import com.newyear.redbull.ui.components.FunctionalityCheckableItem
import com.newyear.redbull.ui.components.FunctionalityItem
import com.newyear.redbull.ui.theme.RedBullTheme

// Basic functionality
@Composable
private fun BasicFunctionalityContainer (
    modifier: Modifier = Modifier,
    viewModel: AppViewModel
) {
    val viewState by viewModel.basicFunctionalityState.collectAsState()

    Column (
        modifier = modifier.padding(dimensionResource(R.dimen.medium)),
    ) {
        Text(
            text = stringResource(R.string.basic_functionality_option_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        FunctionalityCheckableItem(
            title = R.string.automatically_open_packet,
            checked = viewState.autoOpenRedPacket,
            onItemClicked = {
                viewModel.updateAutoOpenRedPaket(!viewState.autoOpenRedPacket)
            },
            onCheckedChanged = {
                viewModel.updateAutoOpenRedPaket(it)
            }
        )
        FunctionalityItem(
            title = R.string.delay_open_packet,
            onItemClicked = {
                // TODO: Open dialog
            }
        )
        FunctionalityCheckableItem(
            title = R.string.open_packet_by_self,
            checked = viewState.openRedPacketMySelf,
            onItemClicked = {
                viewModel.updateOpenRedPacketMySelf(!viewState.openRedPacketMySelf)
            },
            onCheckedChanged = {
                viewModel.updateOpenRedPacketMySelf(it)
            }
        )
        FunctionalityItem(
            title = R.string.shield_packet_text,
            subTitle = R.string.shield_packet_text_description,
            onItemClicked = {
                // TODO: Open dialog
            }
        )
    }
}


// Monitor options
@Composable
private fun MonitorOptionsContainer(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel
){
    val viewState by viewModel.monitorOptionState.collectAsState()

    Column (
        modifier = modifier.padding(dimensionResource(R.dimen.medium))
    ) {
        Text(
            text = stringResource(R.string.monitor_option_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        FunctionalityCheckableItem(
            title = R.string.monitor_system_notification,
            subTitle = R.string.monitor_system_notification_description,
            checked = viewState.monitorSystemNotification,
            onItemClicked = {
                viewModel.updateMonitorSystemNotification(!viewState.monitorSystemNotification)
            },
            onCheckedChanged = {
                viewModel.updateMonitorSystemNotification(it)
            }
        )
        FunctionalityCheckableItem(
            title = R.string.monitor_chat_list,
            subTitle = R.string.monitor_char_list_description,
            checked = viewState.monitorChatListNotification,
            onItemClicked = {
                viewModel.updateMonitorChatListNotification(!viewState.monitorChatListNotification)
            },
            onCheckedChanged = {
                viewModel.updateMonitorChatListNotification(it)
            }
        )
    }
}

// Experimental features
@Composable
private fun ExperimentalFeatures (
    modifier: Modifier = Modifier,
    viewModel: AppViewModel
) {
    val viewState by viewModel.experimentalFunctionalityState.collectAsState()

    Column (
        modifier = modifier.padding(dimensionResource(R.dimen.medium))
    ) {
        Text(
            text = stringResource(R.string.experimental_option_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        FunctionalityCheckableItem(
            title = R.string.open_packet_in_breathing,
            subTitle = R.string.open_packet_in_breathing_description,
            checked = viewState.openReadPacketInBreathMode,
            onItemClicked = {
                viewModel.updateOpenRedPacketInBreathMode(!viewState.openReadPacketInBreathMode)
            },
            onCheckedChanged = {
                viewModel.updateOpenRedPacketInBreathMode(it)
            }
        )
    }
}


// About
@Composable
private fun AboutContainer (
    modifier: Modifier = Modifier
) {
    Column (
        modifier = modifier.padding(dimensionResource(R.dimen.medium))
    ) {
        Text(
            text = stringResource(R.string.about_option_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        FunctionalityItem(
            title = R.string.check_latest_version,
            subTitle = R.string.check_latest_version_url,
            onItemClicked = {
                // TODO: Open browser
            }
        )
    }
}

@Composable
fun HomeScn(
    modifier: Modifier = Modifier
) {

    val appViewModel: AppViewModel = viewModel()

    Column (
        modifier = modifier.verticalScroll(
            state = rememberScrollState()
        )
    ) {
        BasicFunctionalityContainer(
            viewModel = appViewModel
        )
        MonitorOptionsContainer(
            viewModel = appViewModel
        )
        ExperimentalFeatures(
            viewModel = appViewModel
        )
        AboutContainer()
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScnPreview() {
    RedBullTheme (
        dynamicColor = false
    ) {
        HomeScn()
    }
}