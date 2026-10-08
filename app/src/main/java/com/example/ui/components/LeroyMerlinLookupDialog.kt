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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewCarousel
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    initialPalakSlot: Int = 1,
    initialSlotSide: String = "A",
    onDismiss: () -> Unit,
    onProductSelected: (Carpet) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedProductPreview by remember { mutableStateOf<LeroyMerlinProduct?>(null) }
    var isOnlineFetching by remember { mutableStateOf(false) }
    var onlineFetchMessage by remember { mutableStateOf<String?>(null) }
    var showLiveWebBrowser by remember { mutableStateOf(false) }

    // Target Pałąk and Side (Dywan A / Dywan B)
    var targetPalak by remember { mutableIntStateOf(initialPalakSlot) }
    var targetSide by remember { mutableStateOf(if (initialSlotSide.equals("B", ignoreCase = true)) "B" else "A") }

    val searchResults = remember(searchQuery) {
        LeroyMerlinParser.searchProducts(searchQuery)
    }

    if (showLiveWebBrowser) {
        LeroyMerlinLiveViewDialog(
            initialQuery = searchQuery,
            onDismiss = { showLiveWebBrowser = false },
            onProductExtracted = { carpet ->
                showLiveWebBrowser = false
                val assignedCarpet = carpet.copy(
                    standSlot = targetPalak,
                    slotSide = targetSide
                )
                onProductSelected(assignedCarpet)
                onDismiss()
            }
        )
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
                    onlineFetchMessage = "Pobrano dane produktu z leroymerlin.pl!"
                } else {
                    onlineFetchMessage = "Brak bezpośredniej odpowiedzi HTTP. Użyj przycisku 'Otwórz na żywo w sklepie' poniżej."
                }
            } catch (e: Exception) {
                onlineFetchMessage = "Użyj wbudowanej przeglądarki na żywo, aby pobrać dowolny dywan."
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
                    Text("Pobierz dane dywanu ze strony leroymerlin.pl", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Live Browser Action Banner
                Button(
                    onClick = { showLiveWebBrowser = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🌐 Otwórz i szukaj na żywo na leroymerlin.pl", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        selectedProductPreview = null
                        onlineFetchMessage = null
                    },
                    placeholder = { Text("Kod LM (np. 82345001), nazwa, link...", fontSize = 12.sp) },
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
                                label = { Text(phrase, fontSize = 11.sp) }
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

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            // Storage location: Pałąk & Dywan A / Dywan B
                            Text(
                                text = "Wybierz miejsce na ekspozycji:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Pałąk selection chips (1..10)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Pałąk:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    (1..20).forEach { palakNum ->
                                        item {
                                            FilterChip(
                                                selected = targetPalak == palakNum,
                                                onClick = { targetPalak = palakNum },
                                                label = { Text("#$palakNum", fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Dywan A vs Dywan B selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = targetSide == "A",
                                    onClick = { targetSide = "A" },
                                    label = { Text("🅰️ Dywan A (przód pałąka)", fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = targetSide == "B",
                                    onClick = { targetSide = "B" },
                                    label = { Text("🅱️ Dywan B (tył pałąka)", fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.weight(1f)
                                )
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
                                        standSlot = targetPalak,
                                        slotSide = targetSide,
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
                                Text("Zawieś na Pałąk #$targetPalak (Dywan $targetSide)")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = "Katalog produktów Leroy Merlin (${searchResults.size}):",
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
