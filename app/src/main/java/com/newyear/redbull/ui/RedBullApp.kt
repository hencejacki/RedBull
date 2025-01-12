package com.newyear.redbull.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.newyear.redbull.R
import com.newyear.redbull.service.RobAccessibilityService
import com.newyear.redbull.ui.theme.RedBullTheme
import com.newyear.redbull.util.AccessibilityUtil

// App routes definition
enum class RedBullRoutes(@StringRes val title: Int) {
    WELCOME_SCREEN(R.string.welcome_default),
    HOME_SCREEN(R.string.app_name)
}

// App bar construction
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedBullTopBar(
    currentRoute: RedBullRoutes,
    isShow: Boolean = false
) {
    if (isShow) {
        TopAppBar(
            title = {
                Row (
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(currentRoute.title),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            colors = TopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                scrolledContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}

// Floating action bar
@Composable
fun RedBullFloatingActionButton(
    isShow: Boolean = false,
    onCloseActionButtonClicked: () -> Unit
) {
    if (isShow) {
        FloatingActionButton(
            onClick = onCloseActionButtonClicked
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null
            )
        }
    }
}

@Composable
fun RedBullApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = RedBullRoutes.valueOf(
        backStackEntry?.destination?.route ?: RedBullRoutes.WELCOME_SCREEN.name
    )

    val context = LocalContext.current

    Scaffold (
        topBar = {
            RedBullTopBar(
                currentRoute = currentScreen,
                isShow = navController.previousBackStackEntry != null
            )
        },
        floatingActionButton = {
            RedBullFloatingActionButton(
                isShow = navController.previousBackStackEntry != null,
                onCloseActionButtonClicked = {
                    navController.popBackStack()
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RedBullRoutes.WELCOME_SCREEN.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Welcome Screen
            composable(
                route = RedBullRoutes.WELCOME_SCREEN.name
            ) {
                WelcomeScn(
                    onOpenButtonClicked = {
                        // TODO: Check network connectivity
                        if (!AccessibilityUtil.isAccessibilityServiceEnabled(context, RobAccessibilityService::class.qualifiedName ?: RobAccessibilityService.SERVICE_NAME)) {
                            AccessibilityUtil.openAccessibilityServiceSetting(context)
                        } else {
                            navController.navigate(route = RedBullRoutes.HOME_SCREEN.name)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Home Screen
            composable(
                route = RedBullRoutes.HOME_SCREEN.name
            ) {
                HomeScn(
                    modifier = Modifier.fillMaxSize().padding(
                        vertical = dimensionResource(R.dimen.medium),
                        horizontal = dimensionResource(R.dimen.medium),
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RedBullAppPreview() {
    RedBullTheme (
        dynamicColor = false
    ) {
        RedBullApp()
    }
}