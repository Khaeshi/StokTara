package com.ktabss.stoktara.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ktabss.stoktara.ui.activity.ActivityScreen
import com.ktabss.stoktara.ui.alerts.AlertsScreen
import com.ktabss.stoktara.ui.home.HomeScreen
import com.ktabss.stoktara.ui.placeholder.PlaceholderScreen
import com.ktabss.stoktara.ui.theme.InkSoft
import com.ktabss.stoktara.ui.theme.Line
import com.ktabss.stoktara.ui.theme.Paper
import com.ktabss.stoktara.ui.theme.Primary

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val Tabs = listOf(
    Tab(Routes.HOME, "Home", Icons.Outlined.Home),
    Tab(Routes.INVENTORY, "Inventory", Icons.Outlined.Inventory2),
    Tab(Routes.SCAN, "Scan", Icons.Outlined.QrCodeScanner),
    Tab(Routes.ALERTS, "Alerts", Icons.Outlined.NotificationsNone),
)

// Screens that show the bottom bar in the Figma. Scan and the item modals are full-screen without it.
private val BottomBarRoutes = setOf(Routes.HOME, Routes.INVENTORY, Routes.ALERTS, Routes.ACTIVITY, Routes.SETTINGS)

/** Recent Activity and Settings are reached from Home, so Home stays highlighted there. */
private fun highlightedTab(route: String?): String = Tabs.firstOrNull { it.route == route }?.route ?: Routes.HOME

private fun NavHostController.goToTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun StokTaraApp() {
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        containerColor = Paper,
        bottomBar = {
            if (route in BottomBarRoutes) StokBottomBar(highlightedTab(route)) { nav.goToTab(it) }
        },
    ) { padding ->
        // TODO(Member 1): start at onboarding / Intro when no business profile exists yet.
        NavHost(nav, startDestination = Routes.HOME, modifier = Modifier.padding(padding)) {

            // ---- Member 4 (done) ----
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                    onOpenScan = { nav.goToTab(Routes.SCAN) },
                    onOpenAlerts = { nav.goToTab(Routes.ALERTS) },
                    onAddItem = { nav.navigate(Routes.ADD_ITEM) },
                    onSeeMoreActivity = { nav.navigate(Routes.ACTIVITY) },
                )
            }
            composable(Routes.ALERTS) { AlertsScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.ACTIVITY) { ActivityScreen(onBack = { nav.popBackStack() }) }

            // ---- Placeholders: replace the body with the Figma UI, keep the route ----
            composable(Routes.INVENTORY) {
                PlaceholderScreen("Inventory", "Member 3", "Item list with search + category/status filters. Tapping a card opens Quick Stock; the + opens Add Item (Routes.ADD_ITEM); edit opens Routes.editItem(id).")
            }
            composable(Routes.SCAN) {
                PlaceholderScreen("Scan Barcode", "Member 3", "Viewfinder -> matched item -> stock update via repository.recordStockChange().", onBack = { nav.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                PlaceholderScreen("Business Profile", "Member 1", "Owner: business name, category tags, staff, change password. Staff: own nickname + password only.", onBack = { nav.popBackStack() })
            }
            composable(Routes.ADD_ITEM) {
                PlaceholderScreen("Add Item", "Member 2", "Add Item modal: form + photo + barcode capture. Saves through repository.addItem().", onBack = { nav.popBackStack() })
            }
            composable(
                Routes.EDIT_ITEM,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("itemId")
                PlaceholderScreen("Edit Item", "Member 2", "Edit Item modal for item #$id. Saves through repository.updateItem(); delete through deleteItem().", onBack = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun StokBottomBar(selected: String, onSelect: (String) -> Unit) {
    Column {
        HorizontalDivider(color = Line)
        NavigationBar(containerColor = Paper, tonalElevation = 0.dp) {
            Tabs.forEach { tab ->
                NavigationBarItem(
                    selected = tab.route == selected,
                    onClick = { onSelect(tab.route) },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = InkSoft,
                        unselectedTextColor = InkSoft,
                    ),
                )
            }
        }
    }
}
