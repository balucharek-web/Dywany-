package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Carpet
import com.example.data.model.DisplayStand
import com.example.ui.theme.LeroyGreenPrimary

@Composable
fun AssignStandDialog(
    carpet: Carpet,
    stands: List<DisplayStand>,
    onDismiss: () -> Unit,
    onAssignStand: (standId: Long, slot: Int) -> Unit,
    onAssignWarehouse: (container: String, slot: String) -> Unit
) {
    // 0 = Magazyn (Kontenery), 1 = Ekspozycja (Stojaki)
    var selectedTab by remember {
        mutableIntStateOf(if (carpet.isOnStand) 1 else 0)
    }

    // Warehouse state
    var selectedContainer by remember {
        mutableStateOf(carpet.warehouseContainer ?: "Kontener 1")
    }
    var selectedSlot by remember {
        mutableStateOf(carpet.warehouseSlot ?: "A")
    }

    // Stand state
    var selectedStandId by remember {
        mutableStateOf(carpet.standId ?: stands.firstOrNull()?.id ?: 1L)
    }
    var slotStr by remember {
        mutableStateOf(carpet.standSlot?.toString() ?: "1")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = LeroyGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lokalizacja dywanu")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = carpet.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Aktualnie: ${carpet.locationSummary} • ${carpet.dimensionsFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Inventory2, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Magazyn (Kontenery)")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ViewCarousel, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stojaki expo")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // WAREHOUSE CONTAINERS MODE
                    Text(
                        text = "Wybierz kontener magazynowy i miejsce:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    listOf("Kontener 1", "Kontener 2").forEach { containerName ->
                        val isContainerActive = selectedContainer == containerName
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isContainerActive) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = if (isContainerActive) {
                                CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LeroyGreenPrimary))
                            } else null,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = isContainerActive,
                                            onClick = { selectedContainer = containerName }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = containerName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = if (isContainerActive) "Wybrany" else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LeroyGreenPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 36.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    listOf("A", "B").forEach { slotLetter ->
                                        val isSlotSelected = isContainerActive && selectedSlot == slotLetter
                                        FilterChip(
                                            selected = isSlotSelected,
                                            onClick = {
                                                selectedContainer = containerName
                                                selectedSlot = slotLetter
                                            },
                                            label = {
                                                Text(
                                                    text = "Miejsce $slotLetter",
                                                    fontWeight = if (isSlotSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Wybrano: $selectedContainer • Miejsce $selectedSlot",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    // EXPO STANDS MODE
                    Text(
                        text = "Wybierz stojak z ekspozycji:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.height(160.dp)) {
                        items(stands) { stand ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedStandId = stand.id }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedStandId == stand.id,
                                    onClick = { selectedStandId = stand.id }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${stand.code} • ${stand.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${stand.section} (ramion: ${stand.totalSlots})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val currentStand = stands.find { it.id == selectedStandId }
                    val maxSlots = currentStand?.totalSlots ?: 30

                    OutlinedTextField(
                        value = slotStr,
                        onValueChange = { slotStr = it },
                        label = { Text("Numer ramienia / slotu (1 - $maxSlots)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTab == 0) {
                        onAssignWarehouse(selectedContainer, selectedSlot)
                    } else {
                        val slot = slotStr.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        onAssignStand(selectedStandId, slot)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Zatwierdź lokalizację")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
