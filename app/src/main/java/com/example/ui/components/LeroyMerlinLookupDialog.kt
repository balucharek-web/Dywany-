package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Carpet
import com.example.data.model.LeroyMerlinProduct
import com.example.data.util.LeroyMerlinParser
import com.example.ui.theme.LeroyGreenPrimary
import com.example.ui.theme.PromoRed
import kotlinx.coroutines.launch

@Composable
fun LeroyMerlinLookupDialog(
    onDismiss: () -> Unit,
    onProductSelected: (Carpet) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedProductPreview by remember { mutableStateOf<LeroyMerlinProduct?>(null) }
    var isOnlineFetching by remember { mutableStateOf(false) }
    var onlineFetchMessage by remember { mutableStateOf<String?>(null) }

    // Pre-select warehouse container & slot for adding
    var targetContainer by remember { mutableStateOf("Kontener 1") }
    var targetSlot by remember { mutableStateOf("A") }

    val searchResults = remember(searchQuery) {
        LeroyMerlinParser.searchProducts(searchQuery)
    }

    fun executeOnlineFetch() {
        if (searchQuery.isBlank()) return
        isOnlineFetching = true
        onlineFetchMessage = null
        coroutineScope.launch {
            try {
                val result = LeroyMerlinParser.fetchProductOnline(searchQuery)
                if (result != null) {
                    selectedProductPreview = result
                    onlineFetchMessage = "Pobrano aktualne dane produktu ze sklepu Leroy Merlin!"
                } else {
                    onlineFetchMessage = "Przeszukano bazę asortymentu Leroy Merlin."
                }
            } catch (e: Exception) {
                onlineFetchMessage = "Błąd połączenia. Użyto danych z lokalnej bazy Leroy Merlin."
            } finally {
                isOnlineFetching = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = LeroyGreenPrimary,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("LM", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Wyszukiwarka Leroy Merlin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Pobierz dane produktu ze strony leroymerlin.pl", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        selectedProductPreview = null
                        onlineFetchMessage = null
                    },
                    placeholder = { Text("Wpisz kod LM (np. 82345001), nazwę lub wklej link leroymerlin.pl...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Szukaj", tint = LeroyGreenPrimary)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isOnlineFetching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = LeroyGreenPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    selectedProductPreview = null
                                    onlineFetchMessage = null
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Online fetch trigger button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { executeOnlineFetch() },
                        enabled = searchQuery.isNotBlank() && !isOnlineFetching,
                        colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isOnlineFetching) "Łączenie ze stroną Leroy..." else "Pobierz dane ze strony Leroy Merlin", fontSize = 12.sp)
                    }
                }

                onlineFetchMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelSmall,
                        color = LeroyGreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestion chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("82345001", "Rabbit", "Agnella", "Wełna", "Boho Juta", "160x230", "200x300", "Kids Safari").forEach { phrase ->
                        item {
                            FilterChip(
                                selected = searchQuery.equals(phrase, ignoreCase = true),
                                onClick = {
                                    searchQuery = phrase
                                    selectedProductPreview = null
                                    onlineFetchMessage = null
                                },
                                label = { Text(phrase) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Product Preview Card if clicked or fetched
                if (selectedProductPreview != null) {
                    val p = selectedProductPreview!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = LeroyGreenPrimary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "KOD LM: ${p.lmCode}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "${String.format("%.2f", p.price)} zł",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = p.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Wymiary: ${p.dimensionsFormatted} | Materiał: ${p.material} | Styl: ${p.patternStyle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "Kod kreskowy EAN: ${p.ean}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )

                            if (p.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = p.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    maxLines = 2
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Storage location picker: Kontener 1 (A/B), Kontener 2 (A/B)
                            Text(
                                text = "Wybierz miejsce w magazynie:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Kontener 1", "Kontener 2").forEach { cName ->
                                    FilterChip(
                                        selected = targetContainer == cName,
                                        onClick = { targetContainer = cName },
                                        label = { Text(cName, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("A", "B").forEach { slot ->
                                    FilterChip(
                                        selected = targetSlot == slot,
                                        onClick = { targetSlot = slot },
                                        label = { Text("Miejsce $slot", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    val carpet = Carpet(
                                        name = p.title,
                                        ean = p.ean,
                                        lmCode = p.lmCode,
                                        widthCm = p.widthCm,
                                        lengthCm = p.lengthCm,
                                        material = p.material,
                                        patternStyle = p.patternStyle,
                                        regularPrice = p.price,
                                        discountPrice = p.promoPrice,
                                        stockQuantity = p.stockEstimate,
                                        warehouseContainer = targetContainer,
                                        warehouseSlot = targetSlot,
                                        notes = p.description
                                    )
                                    onProductSelected(carpet)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Wstaw dane do formularza ($targetContainer - Miejsce $targetSlot)")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = "Wyniki wyszukiwania w katalogu Leroy Merlin (${searchResults.size}):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Results list
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(searchResults) { item ->
                        val isSelected = selectedProductPreview?.lmCode == item.lmCode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .clickable { selectedProductPreview = item }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CarpetPatternBadge(patternStyle = item.patternStyle, size = 36.dp)

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "LM: ${item.lmCode} • ${item.dimensionsFormatted} • ${item.material}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format("%.2f", item.price)} zł",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = LeroyGreenPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
}
