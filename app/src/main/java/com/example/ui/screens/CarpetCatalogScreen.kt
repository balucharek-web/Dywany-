package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Carpet
import com.example.ui.CarpetFilter
import com.example.ui.MainViewModel
import com.example.ui.components.CarpetPatternBadge
import com.example.ui.components.EditCarpetDialog
import com.example.ui.components.LeroyMerlinLiveViewDialog
import com.example.ui.components.LeroyMerlinLookupDialog
import com.example.ui.theme.LeroyGreenPrimary
import com.example.ui.theme.PromoRed
import com.example.ui.theme.StatusReservedColor

@Composable
fun CarpetCatalogScreen(
    viewModel: MainViewModel,
    onCarpetClick: (Carpet) -> Unit,
    modifier: Modifier = Modifier
) {
    val carpets by viewModel.filteredCarpets.collectAsStateWithLifecycle()
    val allStands by viewModel.allStands.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentFilter by viewModel.currentFilter.collectAsStateWithLifecycle()
    val selectedPattern by viewModel.selectedPatternFilter.collectAsStateWithLifecycle()

    var showEditCarpetDialog by remember { mutableStateOf(false) }
    var showLmLookupDialog by remember { mutableStateOf(false) }
    var showLiveBrowser by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Szukaj dywanu, pałąka, kodu LM, EAN...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Szukaj",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("catalog_search_bar"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Primary Filter Chips (Wszystkie, Miejsce A, Miejsce B, Zarezerwowane, Promocja)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CarpetFilter.values()) { filter ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.label) }
                    )
                }
            }

            // Pattern Style Secondary Filter
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedPattern == null,
                        onClick = { viewModel.setPatternFilter(null) },
                        label = { Text("Wszystkie style") }
                    )
                }
                items(listOf("Klasyczny", "Nowoczesny", "Geometryczny", "Shaggy", "Boho", "Vintage", "Dziecięcy")) { style ->
                    FilterChip(
                        selected = selectedPattern == style,
                        onClick = {
                            viewModel.setPatternFilter(if (selectedPattern == style) null else style)
                        },
                        label = { Text(style) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Carpet Cards List
            if (carpets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Brak dywanów spełniających kryteria.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zmień filtry lub dodaj nowy dywan z bazy Leroy Merlin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(carpets, key = { it.id }) { carpet ->
                        val stand = allStands.find { it.id == carpet.standId }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCarpetClick(carpet) }
                                .testTag("carpet_item_${carpet.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CarpetPatternBadge(
                                    patternStyle = carpet.patternStyle,
                                    size = 54.dp
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = carpet.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "${carpet.dimensionsFormatted} • LM: ${carpet.lmCode}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Location: Pałąk & Dywan A / B Badges
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Pałąk #${carpet.standSlot ?: '?'}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Surface(
                                            color = if (carpet.slotSide.equals("B", ignoreCase = true)) MaterialTheme.colorScheme.secondaryContainer else LeroyGreenPrimary.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Dywan ${if (carpet.slotSide.equals("B", ignoreCase = true)) "B (tył)" else "A (przód)"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (carpet.slotSide.equals("B", ignoreCase = true)) MaterialTheme.colorScheme.onSecondaryContainer else LeroyGreenPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (carpet.isReserved) {
                                            Surface(
                                                color = StatusReservedColor.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Rezerwacja",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StatusReservedColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Price column
                                Column(horizontalAlignment = Alignment.End) {
                                    if (carpet.hasDiscount) {
                                        Text(
                                            text = "${String.format("%.2f", carpet.regularPrice)} zł",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        Text(
                                            text = "${String.format("%.2f", carpet.currentPrice)} zł",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = PromoRed
                                        )
                                        Surface(
                                            color = PromoRed,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "-${carpet.discountPercentage}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "${String.format("%.2f", carpet.regularPrice)} zł",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action FABs
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(
                onClick = { showLiveBrowser = true },
                containerColor = LeroyGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(Icons.Default.Language, contentDescription = "Na żywo leroymerlin.pl", modifier = Modifier.size(22.dp))
            }

            FloatingActionButton(
                onClick = { showLmLookupDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(Icons.Default.Storefront, contentDescription = "Katalog Leroy Merlin", modifier = Modifier.size(22.dp))
            }

            FloatingActionButton(
                onClick = { showEditCarpetDialog = true },
                containerColor = LeroyGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_carpet_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj dywan na pałąk")
            }
        }
    }

    if (showLiveBrowser) {
        LeroyMerlinLiveViewDialog(
            initialQuery = "",
            onDismiss = { showLiveBrowser = false },
            onProductExtracted = { extracted ->
                showLiveBrowser = false
                viewModel.saveCarpet(extracted)
            }
        )
    }

    if (showEditCarpetDialog) {
        EditCarpetDialog(
            carpet = null,
            onDismiss = { showEditCarpetDialog = false },
            onSave = { newCarpet ->
                viewModel.saveCarpet(newCarpet)
                showEditCarpetDialog = false
            }
        )
    }

    if (showLmLookupDialog) {
        LeroyMerlinLookupDialog(
            onDismiss = { showLmLookupDialog = false },
            onProductSelected = { importedCarpet ->
                viewModel.saveCarpet(importedCarpet)
                showLmLookupDialog = false
            }
        )
    }
}
