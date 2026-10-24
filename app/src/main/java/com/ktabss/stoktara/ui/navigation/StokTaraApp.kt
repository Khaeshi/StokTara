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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ktabss.stoktara.data.AppGraph.repository
import com.ktabss.stoktara.ui.activity.ActivityScreen
import com.ktabss.stoktara.ui.alerts.AlertsScreen
import com.ktabss.stoktara.ui.home.HomeScreen
import com.ktabss.stoktara.ui.intro.IntroScreen
import com.ktabss.stoktara.ui.inventory.InventoryScreen
import com.ktabss.stoktara.ui.itemforms.AddItemScreen
import com.ktabss.stoktara.ui.itemforms.EditItemScreen
import com.ktabss.stoktara.ui.itemforms.EditItemViewModel
import com.ktabss.stoktara.ui.scan.BarcodeMatchedScreen
import com.ktabss.stoktara.ui.scan.ScanBg
import com.ktabss.stoktara.ui.scan.ScanScreen
import com.ktabss.stoktara.ui.placeholder.PlaceholderScreen
import com.ktabss.stoktara.ui.theme.InkSoft
import com.ktabss.stoktara.ui.theme.Line
import com.ktabss.stoktara.ui.theme.Paper
import com.ktabss.stoktara.ui.theme.Primary
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import com.ktabss.stoktara.data.InventoryRepository
import com.ktabss.stoktara.ui.itemforms.AddItemViewModel
import com.ktabss.stoktara.ui.itemforms.ItemPhotoScreen
import com.ktabss.stoktara.ui.itemforms.ItemScanScreen

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
    popUpTo(Routes.HOME) { saveState = true } // Intro is popped after Continue, so anchor tabs on Home
    launchSingleTop = true
    restoreState = true
}

@Composable
fun StokTaraApp() {
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route

    val onIntro = route == null || route == Routes.INTRO

    Scaffold(
        containerColor = when {
            onIntro -> Primary
            route == Routes.SCAN -> ScanBg // dark viewfinder screen, so the status-bar strip matches
            else -> Paper
        },
        bottomBar = {
            if (route in BottomBarRoutes) StokBottomBar(highlightedTab(route)) { nav.goToTab(it) }
        },
    ) { padding ->
        // TODO(Member 1): when no business profile exists yet, start at the onboarding forms instead of Intro.
        NavHost(nav, startDestination = Routes.INTRO, modifier = Modifier.padding(padding)) {

            composable(Routes.INTRO) {
                IntroScreen(
                    onContinue = {
                        nav.navigate(Routes.HOME) { popUpTo(Routes.INTRO) { inclusive = true } }
                    },
                )
            }

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
            // ---- Member 3 (done) ----
            composable(Routes.INVENTORY) {
                InventoryScreen(
                    onAddItem = { nav.navigate(Routes.ADD_ITEM) },
                    onEditItem = { id -> nav.navigate(Routes.editItem(id)) },
                )
            }
            composable(Routes.SCAN) {
                ScanScreen(
                    onBack = { nav.popBackStack() },
                    onMatched = { id -> nav.navigate(Routes.scanMatched(id)) },
                )
            }
            composable(
                Routes.SCAN_MATCHED,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType }),
            ) { entry ->
                BarcodeMatchedScreen(
                    itemId = entry.arguments?.getLong("itemId") ?: -1L,
                    onBack = { nav.popBackStack() }, // back to the viewfinder, ready for the next scan
                    onEditDetails = { id -> nav.navigate(Routes.editItem(id)) },
                )
            }

            // ---- Placeholders: replace the body with the Figma UI, keep the route ----
            composable(Routes.SETTINGS) {
                PlaceholderScreen("Business Profile", "Member 1", "Owner: business name, category tags, staff, change password. Staff: own nickname + password only.", onBack = { nav.popBackStack() })
            }
            composable(Routes.ADD_ITEM) { entry ->
                val viewModel: AddItemViewModel = viewModel()

                val scannedSku = entry.savedStateHandle.get<String>("scanned_sku")
                if (scannedSku != null) {
                    viewModel.setSku(scannedSku)
                    entry.savedStateHandle.remove<String>("scanned_sku")
                }

                val capturedPhoto = entry.savedStateHandle.get<String>("captured_photo")
                if (capturedPhoto != null) {
                    viewModel.setPhotoUri(capturedPhoto)
                    entry.savedStateHandle.remove<String>("captured_photo")
                }

                AddItemScreen(
                    onBack = { nav.popBackStack() },
                    onScanClick = { nav.navigate(Routes.SCAN_BARCODE) },
                    onPhotoClick = { nav.navigate(Routes.CAPTURE_PHOTO) },
                    viewModel = viewModel
                )
            }

            composable(
                Routes.EDIT_ITEM,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType }),
            ) { entry ->
                val viewModel: EditItemViewModel = viewModel(
                    factory = EditItemViewModelFactory(repository, entry)
                )

                val scannedSku = entry.savedStateHandle.get<String>("scanned_sku")
                if (scannedSku != null) {
                    viewModel.setScannedSku(scannedSku)
                    entry.savedStateHandle.remove<String>("scanned_sku")
                }

                val capturedPhoto = entry.savedStateHandle.get<String>("captured_photo")
                if (capturedPhoto != null) {
                    viewModel.setCapturedPhoto(capturedPhoto)
                    entry.savedStateHandle.remove<String>("captured_photo")
                }

                EditItemScreen(
                    viewModel = viewModel,
                    onBack = { nav.popBackStack() },
                    onScanClick = { nav.navigate(Routes.SCAN_BARCODE) },
                    onPhotoClick = { nav.navigate(Routes.CAPTURE_PHOTO) }
                )
            }

            composable(Routes.SCAN_BARCODE) {
                ItemScanScreen(
                    onBack = { nav.popBackStack() },
                    onSkuScanned = { scannedSku ->
                        nav.previousBackStackEntry?.savedStateHandle?.set("scanned_sku", scannedSku)
                    }
                )
            }

            composable(Routes.CAPTURE_PHOTO) {
                ItemPhotoScreen(
                    onBack = { nav.popBackStack() },
                    onPhotoCaptured = { photoUri ->
                        nav.previousBackStackEntry?.savedStateHandle?.set(
                            "captured_photo",
                            photoUri
                        )
                    }
                )
            }
        }
    }
}

class EditItemViewModelFactory(
    private val repository: InventoryRepository,
    private val entry: NavBackStackEntry
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val itemId = entry.arguments?.getLong("itemId") ?: 0L
        return EditItemViewModel(itemId, repository) as T
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
