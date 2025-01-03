package com.newyear.redbull.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.newyear.redbull.R
import com.newyear.redbull.ui.theme.RedBullTheme

@Composable
fun FunctionalityItem (
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    @StringRes subTitle: Int = -1,
    onItemClicked: () -> Unit = {}
) {
    Column (
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(vertical = dimensionResource(R.dimen.medium))
            .clickable(
                onClick = onItemClicked
            ),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.bodyLarge
        )
        if (subTitle != -1) {
            Text(
                text = stringResource(subTitle),
                style = MaterialTheme.typography.bodySmall
            )
        }

    }
}

@Composable
fun FunctionalityCheckableItem (
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    @StringRes subTitle: Int = -1,
    checked: Boolean = false,
    enabled: Boolean = true,
    onCheckedChanged: (Boolean) -> Unit,
    onItemClicked: () -> Unit = {}
) {
    Row (
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.medium)),
        verticalAlignment = Alignment.CenterVertically
    ){
        FunctionalityItem(
            title = title,
            subTitle = subTitle,
            modifier = Modifier.weight(1.0F),
            onItemClicked = onItemClicked
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChanged,
            enabled = enabled,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FunctionalityCheckableItemPreview() {
    RedBullTheme {
        FunctionalityCheckableItem(
            title = R.string.monitor_system_notification,
            subTitle = R.string.monitor_system_notification_description,
            modifier = Modifier.fillMaxWidth(),
            onCheckedChanged = {}
        )
    }
}