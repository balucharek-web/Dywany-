package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Carpet
import com.example.data.model.DisplayStand
import com.example.ui.MainViewModel
import com.example.ui.components.CreateTakeDownOrderDialog
import com.example.ui.components.EditCarpetDialog
import com.example.ui.components.EditStandDialog
import com.example.ui.components.LeroyMerlinLiveViewDialog
import com.example.ui.components.LeroyMerlinLookupDialog
import com.example.ui.components.PrintLabelDialog
import com.example.ui.components.StandCard
import com.example.ui.theme.LeroyGreenPrimary

@Composable
fun ExpoStandsScreen(
    viewModel: MainViewModel,
    onNavigateToCarpet: (Carpet) -> Unit,
    modifier: Modifier = Modifier
) {
    val stands by viewModel.allStands.collectAsStateWithLifecycle()
    val allCarpets by viewModel.allCarpets.collectAsStateWithLifecycle()

    var showEditStandDialog by remember { mutableStateOf<DisplayStand?>(null) }
    var isCreatingNewStand by remember { mutableStateOf(false) }
    var takeDownTargetCarpet by remember { mutableStateOf<Carpet?>(null) }
    var printLabelTargetCarpet by remember { mutableStateOf<Carpet?>(null) }
    var searchSectionQuery by remember { mutableStateOf("") }

    // Dialogs for adding carpet to a specific pałąk
    var targetAddPalak by remember { mutableIntStateOf(1) }
    var targetAddSide by remember { mutableStateOf("A") }
    var showAddCarpetDialog by remember { mutableStateOf(false) }
    var showLiveBrowser by remember { mutableStateOf(false) }

    val filteredStands = stands.filter { stand ->
        searchSectionQuery.isBlank() ||
                stand.code.contains(searchSectionQuery, ignoreCase = true) ||
                stand.name.contains(searchSectionQuery, ignoreCase = true) ||
                stand.section.contains(searchSectionQuery, ignoreCase = true)
    }

    val totalPalakiAcrossStands = stands.sumOf { it.totalSlots }
    val totalSlotsCapacity = totalPalakiAcrossStands * 2 // Każdy pałąk mieści dokładnie 2 dywany (Dywan A i Dywan B)
    val totalOccupiedCarpets = allCarpets.count { it.isOnStand }

    if (showLiveBrowser) {
        LeroyMerlinLiveViewDialog(
            initialQuery = "",
            onDismiss = { showLiveBrowser = false },
            onProductExtracted = { extracted ->
                showLiveBrowser = false
                val assigned = extracted.copy(
                    standSlot = targetAddPalak,
                    slotSide = targetAddSide
                )
                viewModel.insertCarpet(assigned)
            }
        )
    }

    if (showAddCarpetDialog) {
        EditCarpetDialog(
            carpet = null,
            defaultPalakSlot = targetAddPalak,
            defaultSlotSide = targetAddSide,
            onDismiss = { showAddCarpetDialog = false },
            onSave = { newCarpet ->
                viewModel.insertCarpet(newCarpet)
            }
        )
    }

    takeDownTargetCarpet?.let { carpet ->
        val stand = stands.find { it.id == carpet.standId }
        CreateTakeDownOrderDialog(
            carpet = carpet,
            stand = stand,
            onDismiss = { takeDownTargetCarpet = null },
            onSubmit = { reason, customerName, customerPhone, priority ->
                viewModel.createTakeDownOrder(
                    carpet = carpet,
                    reason = reason,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    priority = priority
                )
                takeDownTargetCarpet = null
            }
        )
    }

    printLabelTargetCarpet?.let { carpet ->
        val stand = stands.find { it.id == carpet.standId }
        PrintLabelDialog(
            carpet = carpet,
            stand = stand,
            onDismiss = { printLabelTargetCarpet = null }
        )
    }

    if (isCreatingNewStand || showEditStandDialog != null) {
        EditStandDialog(
            stand = showEditStandDialog,
            onDismiss = {
                showEditStandDialog = null
                isCreatingNewStand = false
            },
            onSave = { standToSave ->
                viewModel.saveStand(standToSave)
                showEditStandDialog = null
                isCreatingNewStand = false
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Card (Ekspozycja pałąków)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Ekspozycja dywanów w sklepie",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Pałąki ekspozytora • Każdy pałąk mieści Dywan A i Dywan B",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                            Surface(
                                color = LeroyGreenPrimary,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ViewCarousel,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Stojaki",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${stands.size} szt.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = "Pałąki",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "$totalPalakiAcrossStands pałąków",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = "Zajęte dywany (A + B)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "$totalOccupiedCarpets / $totalSlotsCapacity",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live Leroy Merlin web search button
                        Button(
                            onClick = {
                                targetAddPalak = 1
                                targetAddSide = "A"
                                showLiveBrowser = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🌐 Pobierz dywan ze strony leroymerlin.pl", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Search Bar for stands
            item {
                OutlinedTextField(
                    value = searchSectionQuery,
                    onValueChange = { searchSectionQuery = it },
                    placeholder = { Text("Filtruj stojaki lub alejki...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Szukaj")
                    },
                    trailingIcon = {
                        if (searchSectionQuery.isNotBlank()) {
                            IconButton(onClick = { searchSectionQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Stands List
            items(filteredStands) { stand ->
                val carpetsOnThisStand = allCarpets.filter { it.standId == stand.id }
                StandCard(
                    stand = stand,
                    carpetsOnStand = carpetsOnThisStand,
                    onCarpetClick = { carpet -> onNavigateToCarpet(carpet) },
                    onEditStand = { s -> showEditStandDialog = s },
                    onTakeDownRequest = { carpet -> takeDownTargetCarpet = carpet },
                    onPrintLabel = { carpet -> printLabelTargetCarpet = carpet },
                    onAddCarpetToPalak = { palakSlot, slotSide ->
                        targetAddPalak = palakSlot
                        targetAddSide = slotSide
                        showAddCarpetDialog = true
                    }
                )
            }

            // Action: Add new stand
            item {
                OutlinedButton(
                    onClick = { isCreatingNewStand = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dodaj nowy stojak ekspozycyjny")
                }
            }
        }

        // Floating action button: Add new carpet to pałąk
        FloatingActionButton(
            onClick = {
                targetAddPalak = 1
                targetAddSide = "A"
                showAddCarpetDialog = true
            },
            containerColor = LeroyGreenPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_carpet_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Dodaj dywan na pałąk")
        }
    }
}
