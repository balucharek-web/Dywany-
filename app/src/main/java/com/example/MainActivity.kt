package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Carpet
import com.example.data.model.TakeDownOrder
import com.example.ui.MainViewModel
import com.example.ui.components.AssignStandDialog
import com.example.ui.components.CarpetDetailSheet
import com.example.ui.components.CreateTakeDownOrderDialog
import com.example.ui.components.DiscountCalculatorView
import com.example.ui.components.EditCarpetDialog
import com.example.ui.components.PrintLabelDialog
import com.example.ui.components.ReserveCarpetDialog
import com.example.ui.components.TakeDownOrdersSheet
import com.example.ui.screens.CarpetCatalogScreen
import com.example.ui.screens.ExpoStandsScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SyncScreen
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.LeroyGreenPrimary
import com.example.ui.theme.MyApplicationTheme

enum class ExpoNavTab(val title: String, val icon: ImageVector, val tag: String) {
    STANDS("Stojaki", Icons.Default.ViewCarousel, "nav_tab_stands"),
    CATALOG("Katalog", Icons.Default.Dashboard, "nav_tab_catalog"),
    SCANNER("Skaner", Icons.Default.QrCodeScanner, "nav_tab_scanner"),
    SYNC("Sync Wi-Fi", Icons.Default.WifiTethering, "nav_tab_sync")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf(ExpoNavTab.STANDS) }
                val selectedCarpet by viewModel.selectedCarpet.collectAsStateWithLifecycle()
                val allStands by viewModel.allStands.collectAsStateWithLifecycle()
                val activeOrders by viewModel.activeOrders.collectAsStateWithLifecycle()
                val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()

                // Dialog states
                var showOrdersSheet by remember { mutableStateOf(false) }
                var showDiscountCalc by remember { mutableStateOf<Carpet?>(null) }
                var showPrintLabel by remember { mutableStateOf<Carpet?>(null) }
                var showAssignStand by remember { mutableStateOf<Carpet?>(null) }
                var showReserveDialog by remember { mutableStateOf<Carpet?>(null) }
                var showEditCarpetDialog by remember { mutableStateOf<Carpet?>(null) }
                var showCreateTakeDownDialog by remember { mutableStateOf<Carpet?>(null) }

                BackHandler(enabled = currentTab != ExpoNavTab.STANDS || selectedCarpet != null) {
                    if (selectedCarpet != null) {
                        viewModel.selectCarpet(null)
                    } else {
                        currentTab = ExpoNavTab.STANDS
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = LeroyGreenPrimary,
                                        shape = CircleShape,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "LM",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Dywany Expo",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { showOrdersSheet = true },
                                    modifier = Modifier.testTag("orders_badge_button")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (activeOrders.isNotEmpty()) {
                                                Badge(containerColor = AmberSecondary) {
                                                    Text(
                                                        text = activeOrders.size.toString(),
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerticalAlignBottom,
                                            contentDescription = "Zlecenia ściągnięcia ze stojaków",
                                            tint = if (activeOrders.isNotEmpty()) AmberSecondary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            ExpoNavTab.values().forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(imageVector = tab.icon, contentDescription = tab.title)
                                    },
                                    label = { Text(tab.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = LeroyGreenPrimary,
                                        selectedTextColor = LeroyGreenPrimary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            ExpoNavTab.STANDS -> {
                                ExpoStandsScreen(
                                    viewModel = viewModel,
                                    onNavigateToCarpet = { carpet ->
                                        viewModel.selectCarpet(carpet)
                                    }
                                )
                            }
                            ExpoNavTab.CATALOG -> {
                                CarpetCatalogScreen(
                                    viewModel = viewModel,
                                    onCarpetClick = { carpet ->
                                        viewModel.selectCarpet(carpet)
                                    }
                                )
                            }
                            ExpoNavTab.SCANNER -> {
                                ScannerScreen(
                                    viewModel = viewModel,
                                    onOpenCarpetDetail = { carpet ->
                                        viewModel.selectCarpet(carpet)
                                    }
                                )
                            }
                            ExpoNavTab.SYNC -> {
                                SyncScreen(viewModel = viewModel)
                            }
                        }

                        // Master Detail Sheet for selected carpet
                        selectedCarpet?.let { carpet ->
                            val stand = allStands.find { it.id == carpet.standId }
                            CarpetDetailSheet(
                                carpet = carpet,
                                stand = stand,
                                onDismiss = { viewModel.selectCarpet(null) },
                                onEditCarpet = { c -> showEditCarpetDialog = c },
                                onDeleteCarpet = { c -> viewModel.deleteCarpet(c) },
                                onAssignStand = { showAssignStand = carpet },
                                onReserve = { showReserveDialog = carpet },
                                onCancelReservation = { viewModel.cancelReservation(carpet.id) },
                                onRequestTakeDown = { showCreateTakeDownDialog = carpet },
                                onOpenDiscountCalc = { showDiscountCalc = carpet },
                                onPrintLabel = { showPrintLabel = carpet }
                            )
                        }

                        // Orders Sheet
                        if (showOrdersSheet) {
                            TakeDownOrdersSheet(
                                orders = allOrders,
                                onDismiss = { showOrdersSheet = false },
                                onUpdateStatus = { orderId, status ->
                                    viewModel.updateOrderStatus(orderId, status)
                                },
                                onDeleteOrder = { order ->
                                    viewModel.deleteOrder(order)
                                }
                            )
                        }

                        // Discount Calculator Dialog
                        showDiscountCalc?.let { carpet ->
                            DiscountCalculatorView(
                                carpet = carpet,
                                onDismiss = { showDiscountCalc = null },
                                onApplyDiscount = { promoPrice ->
                                    viewModel.saveCarpet(carpet.copy(discountPrice = promoPrice))
                                    showDiscountCalc = null
                                }
                            )
                        }

                        // Print Label Dialog
                        showPrintLabel?.let { carpet ->
                            val stand = allStands.find { it.id == carpet.standId }
                            PrintLabelDialog(
                                carpet = carpet,
                                stand = stand,
                                onDismiss = { showPrintLabel = null }
                            )
                        }

                        // Assign Stand / Warehouse Location Dialog
                        showAssignStand?.let { carpet ->
                            AssignStandDialog(
                                carpet = carpet,
                                stands = allStands,
                                onDismiss = { showAssignStand = null },
                                onAssignStand = { standId, slot ->
                                    viewModel.assignCarpetToStand(carpet.id, standId, slot)
                                    showAssignStand = null
                                },
                                onAssignWarehouse = { container, slot ->
                                    viewModel.assignCarpetToWarehouse(carpet.id, container, slot)
                                    showAssignStand = null
                                }
                            )
                        }

                        // Reserve Carpet Dialog
                        showReserveDialog?.let { carpet ->
                            ReserveCarpetDialog(
                                carpet = carpet,
                                onDismiss = { showReserveDialog = null },
                                onConfirm = { name, phone ->
                                    viewModel.reserveCarpet(carpet.id, name, phone)
                                    showReserveDialog = null
                                }
                            )
                        }

                        // Edit Carpet Dialog
                        showEditCarpetDialog?.let { carpet ->
                            EditCarpetDialog(
                                carpet = carpet,
                                onDismiss = { showEditCarpetDialog = null },
                                onSave = { updated ->
                                    viewModel.saveCarpet(updated)
                                    showEditCarpetDialog = null
                                }
                            )
                        }

                        // Create Take Down Order Dialog
                        showCreateTakeDownDialog?.let { carpet ->
                            val stand = allStands.find { it.id == carpet.standId }
                            CreateTakeDownOrderDialog(
                                carpet = carpet,
                                stand = stand,
                                onDismiss = { showCreateTakeDownDialog = null },
                                onSubmit = { reason, cName, cPhone, priority ->
                                    viewModel.createTakeDownOrder(carpet, reason, cName, cPhone, priority)
                                    showCreateTakeDownDialog = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
