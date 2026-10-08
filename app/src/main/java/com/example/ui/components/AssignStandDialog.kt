package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Carpet
import com.example.data.model.DisplayStand
import com.example.ui.theme.LeroyGreenPrimary

@Composable
fun AssignStandDialog(
    carpet: Carpet,
    stands: List<DisplayStand>,
    allCarpets: List<Carpet> = emptyList(),
    onDismiss: () -> Unit,
    onAssignStand: (standId: Long, slot: Int, slotSide: String) -> Unit
) {
    var selectedStandId by remember {
        mutableStateOf(carpet.standId ?: stands.firstOrNull()?.id ?: 1L)
    }
    var selectedPalak by remember {
        mutableIntStateOf(carpet.standSlot ?: 1)
    }
    var selectedSide by remember {
        mutableStateOf(if (carpet.slotSide.equals("B", ignoreCase = true)) "B" else "A")
    }

    val currentStand = stands.find { it.id == selectedStandId } ?: stands.firstOrNull()
    val maxSlots = currentStand?.totalSlots ?: 30

    // Check which carpets are currently on this pałąk
    val carpetsOnThisPalak = allCarpets.filter {
        it.standId == selectedStandId && it.standSlot == selectedPalak && it.id != carpet.id
    }
    val carpetAOnPalak = carpetsOnThisPalak.find { it.slotSide.equals("A", ignoreCase = true) }
    val carpetBOnPalak = carpetsOnThisPalak.find { it.slotSide.equals("B", ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = LeroyGreenPrimary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ViewCarousel,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Lokalizacja na ekspozycji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Wybierz stojak, pałąk oraz miejsce (Dywan A / Dywan B)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Carpet info header
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = carpet.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Aktualne położenie: ${carpet.locationSummary} • ${carpet.dimensionsFormatted}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Select Stand
                Text(
                    text = "1. Wybierz stojak / ekspozytor:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.height(110.dp)) {
                    items(stands) { stand ->
                        val isSelected = selectedStandId == stand.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { selectedStandId = stand.id }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedStandId = stand.id }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "${stand.code} • ${stand.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "${stand.section} (pałąków: ${stand.totalSlots})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Select Pałąk
                Text(
                    text = "2. Wybierz Pałąk (1 - $maxSlots):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..maxSlots).forEach { palakNum ->
                        item {
                            FilterChip(
                                selected = selectedPalak == palakNum,
                                onClick = { selectedPalak = palakNum },
                                label = { Text("Pałąk #$palakNum") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Select Dywan A vs Dywan B
                Text(
                    text = "3. Wybierz miejsce na pałąku (każdy pałąk mieści 2 dywany):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dywan A Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedSide = "A" },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedSide == "A") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedSide == "A", onClick = { selectedSide = "A" })
                                Text("Dywan A", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(
                                text = if (carpetAOnPalak != null) "Zajęte: ${carpetAOnPalak.name.take(18)}..." else "🟢 Wolne miejsce",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (carpetAOnPalak != null) MaterialTheme.colorScheme.error else LeroyGreenPrimary
                            )
                        }
                    }

                    // Dywan B Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedSide = "B" },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedSide == "B") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedSide == "B", onClick = { selectedSide = "B" })
                                Text("Dywan B", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(
                                text = if (carpetBOnPalak != null) "Zajęte: ${carpetBOnPalak.name.take(18)}..." else "🟢 Wolne miejsce",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (carpetBOnPalak != null) MaterialTheme.colorScheme.error else LeroyGreenPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAssignStand(selectedStandId, selectedPalak, selectedSide)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Zawieś na Pałąk #$selectedPalak (Dywan $selectedSide)")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
