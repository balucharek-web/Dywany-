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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Carpet
import com.example.data.model.DisplayStand
import com.example.ui.MainViewModel
import com.example.ui.components.CreateTakeDownOrderDialog
import com.example.ui.components.EditStandDialog
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
    var searchSectionQuery by remember { mutableStateOf("") }

    val filteredStands = stands.filter { stand ->
        searchSectionQuery.isBlank() ||
                stand.code.contains(searchSectionQuery, ignoreCase = true) ||
                stand.name.contains(searchSectionQuery, ignoreCase = true) ||
                stand.section.contains(searchSectionQuery, ignoreCase = true)
    }

    val totalSlotsAcrossStands = stands.sumOf { it.totalSlots }
    val totalOccupiedAcrossStands = allCarpets.count { it.isOnStand }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Card
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
                                    text = "Ekspozycja w sklepie",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Dział dywanów • Stojaki i wieszaki",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ViewCarousel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
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
                                    text = "Zajęte ramiona",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "$totalOccupiedAcrossStands / $totalSlotsAcrossStands",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = "Dywany w magazynie",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${allCarpets.count { !it.isOnStand }} szt.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Magazyn - Kontener 1 & Kontener 2 Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(6.dp).size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Magazyn sklepu • Kontenery",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Miejsca magazynowe: Kontener 1 i 2 (miejsca A i B)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Grid for Container 1 and Container 2
                        listOf("Kontener 1", "Kontener 2").forEach { containerName ->
                            val containerCarpets = allCarpets.filter { !it.isOnStand && it.warehouseContainer == containerName }
                            val carpetsA = containerCarpets.filter { it.warehouseSlot.equals("A", ignoreCase = true) }
                            val carpetsB = containerCarpets.filter { it.warehouseSlot.equals("B", ignoreCase = true) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = containerName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Razem: ${containerCarpets.size} dywanów",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Miejsce A
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Miejsce A",
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                    Text(
                                                        text = "${carpetsA.size} szt.",
                                                        fontWeight = FontWeight.Bold,
                                                        color = LeroyGreenPrimary,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                }
                                                if (carpetsA.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    carpetsA.take(2).forEach { c ->
                                                        Text(
                                                            text = "• ${c.name}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            maxLines = 1,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable { onNavigateToCarpet(c) }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Miejsce B
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Miejsce B",
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                    Text(
                                                        text = "${carpetsB.size} szt.",
                                                        fontWeight = FontWeight.Bold,
                                                        color = LeroyGreenPrimary,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                }
                                                if (carpetsB.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    carpetsB.take(2).forEach { c ->
                                                        Text(
                                                            text = "• ${c.name}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            maxLines = 1,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable { onNavigateToCarpet(c) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Search filter for stands
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stojaki ekspozycyjne:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchSectionQuery,
                    onValueChange = { searchSectionQuery = it },
                    placeholder = { Text("Filtruj stojaki (kod, alejka, nazwa)...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            // Stands list
            if (filteredStands.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Brak stojaków ekspozycyjnych.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredStands, key = { it.id }) { stand ->
                    val carpetsOnThisStand = allCarpets.filter { it.standId == stand.id }
                    StandCard(
                        stand = stand,
                        carpetsOnStand = carpetsOnThisStand,
                        onCarpetClick = { carpet ->
                            viewModel.selectCarpet(carpet)
                            onNavigateToCarpet(carpet)
                        },
                        onEditStand = { showEditStandDialog = it },
                        onTakeDownRequest = { carpet -> takeDownTargetCarpet = carpet }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { isCreatingNewStand = true },
            containerColor = LeroyGreenPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_stand_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Dodaj stojak")
        }
    }

    // Dialogs
    if (isCreatingNewStand) {
        EditStandDialog(
            stand = null,
            onDismiss = { isCreatingNewStand = false },
            onSave = { stand ->
                viewModel.saveStand(stand)
                isCreatingNewStand = false
            }
        )
    }

    showEditStandDialog?.let { standToEdit ->
        EditStandDialog(
            stand = standToEdit,
            onDismiss = { showEditStandDialog = null },
            onSave = { updated ->
                viewModel.saveStand(updated)
                showEditStandDialog = null
            },
            onDelete = { standToDelete ->
                viewModel.deleteStand(standToDelete)
                showEditStandDialog = null
            }
        )
    }

    takeDownTargetCarpet?.let { carpet ->
        val stand = stands.find { it.id == carpet.standId }
        CreateTakeDownOrderDialog(
            carpet = carpet,
            stand = stand,
            onDismiss = { takeDownTargetCarpet = null },
            onSubmit = { reason, cName, cPhone, priority ->
                viewModel.createTakeDownOrder(carpet, reason, cName, cPhone, priority)
                takeDownTargetCarpet = null
            }
        )
    }
}
