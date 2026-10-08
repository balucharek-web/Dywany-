package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Carpet
import com.example.ui.theme.LeroyGreenPrimary

@Composable
fun EditCarpetDialog(
    carpet: Carpet?,
    defaultPalakSlot: Int = 1,
    defaultSlotSide: String = "A",
    onDismiss: () -> Unit,
    onSave: (Carpet) -> Unit
) {
    var name by remember { mutableStateOf(carpet?.name ?: "") }
    var ean by remember { mutableStateOf(carpet?.ean ?: "") }
    var lmCode by remember { mutableStateOf(carpet?.lmCode ?: "") }
    var widthCm by remember { mutableStateOf(carpet?.widthCm?.toString() ?: "160") }
    var lengthCm by remember { mutableStateOf(carpet?.lengthCm?.toString() ?: "230") }
    var material by remember { mutableStateOf(carpet?.material ?: "100% Polipropylen Heatset") }
    var patternStyle by remember { mutableStateOf(carpet?.patternStyle ?: "Nowoczesny") }
    var regularPrice by remember {
        mutableStateOf(carpet?.regularPrice?.let { String.format("%.2f", it).replace(",", ".") } ?: "299.00")
    }
    var discountPrice by remember {
        mutableStateOf(carpet?.discountPrice?.let { String.format("%.2f", it).replace(",", ".") } ?: "")
    }
    var stockQuantity by remember { mutableStateOf(carpet?.stockQuantity?.toString() ?: "3") }

    // Pałąk & Slot Side (Dywan A / Dywan B)
    var palakSlot by remember { mutableIntStateOf(carpet?.standSlot ?: defaultPalakSlot) }
    var slotSide by remember { mutableStateOf(carpet?.slotSide ?: defaultSlotSide) }

    var notes by remember { mutableStateOf(carpet?.notes ?: "") }

    var showLmSearchDialog by remember { mutableStateOf(false) }
    var showLiveBrowser by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    if (showLiveBrowser) {
        LeroyMerlinLiveViewDialog(
            initialQuery = if (lmCode.isNotBlank()) lmCode else name,
            onDismiss = { showLiveBrowser = false },
            onProductExtracted = { extracted ->
                showLiveBrowser = false
                name = extracted.name
                lmCode = extracted.lmCode
                ean = extracted.ean
                widthCm = extracted.widthCm.toString()
                lengthCm = extracted.lengthCm.toString()
                material = extracted.material
                patternStyle = extracted.patternStyle
                regularPrice = String.format("%.2f", extracted.regularPrice).replace(",", ".")
                discountPrice = extracted.discountPrice?.let { String.format("%.2f", it).replace(",", ".") } ?: ""
            }
        )
    }

    if (showLmSearchDialog) {
        LeroyMerlinLookupDialog(
            initialPalakSlot = palakSlot,
            initialSlotSide = slotSide,
            onDismiss = { showLmSearchDialog = false },
            onProductSelected = { extracted ->
                showLmSearchDialog = false
                name = extracted.name
                lmCode = extracted.lmCode
                ean = extracted.ean
                widthCm = extracted.widthCm.toString()
                lengthCm = extracted.lengthCm.toString()
                material = extracted.material
                patternStyle = extracted.patternStyle
                regularPrice = String.format("%.2f", extracted.regularPrice).replace(",", ".")
                discountPrice = extracted.discountPrice?.let { String.format("%.2f", it).replace(",", ".") } ?: ""
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = LeroyGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (carpet == null) "Dodaj dywan na pałąk" else "Edytuj dane dywanu")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Action Buttons: Live Leroy Merlin and Catalog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showLiveBrowser = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Na żywo z Leroy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showLmSearchDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Katalog LM", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pałąk & Slot side assignment card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ViewCarousel, contentDescription = null, tint = LeroyGreenPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lokalizacja na ekspozycji (Pałąk):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Pałąk number chips
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Pałąk:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                (1..30).forEach { num ->
                                    item {
                                        FilterChip(
                                            selected = palakSlot == num,
                                            onClick = { palakSlot = num },
                                            label = { Text("#$num", fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dywan A vs Dywan B selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = slotSide == "A",
                                onClick = { slotSide = "A" },
                                label = { Text("🅰️ Dywan A (przód)", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = slotSide == "B",
                                onClick = { slotSide = "B" },
                                label = { Text("🅱️ Dywan B (tył)", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; isError = false },
                    label = { Text("Nazwa dywanu / kolekcja *") },
                    isError = isError && name.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = lmCode,
                        onValueChange = { lmCode = it },
                        label = { Text("Kod LM (8 cyfr)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ean,
                        onValueChange = { ean = it },
                        label = { Text("Kod EAN-13") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dimensions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = widthCm,
                        onValueChange = { widthCm = it },
                        label = { Text("Szerokość (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lengthCm,
                        onValueChange = { lengthCm = it },
                        label = { Text("Długość (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Preset size chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("80×150", "120×170", "160×230", "200×300").forEach { size ->
                        FilterChip(
                            selected = "$widthCm×$lengthCm" == size,
                            onClick = {
                                val parts = size.split("×")
                                widthCm = parts[0]
                                lengthCm = parts[1]
                            },
                            label = { Text(size, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Material & Pattern Style
                OutlinedTextField(
                    value = material,
                    onValueChange = { material = it },
                    label = { Text("Skład / Materiał") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Styl / Wzornictwo:", style = MaterialTheme.typography.labelSmall)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Nowoczesny", "Klasyczny", "Shaggy", "Boho", "Geometryczny", "Vintage", "Dziecięcy").forEach { s ->
                        item {
                            FilterChip(
                                selected = patternStyle == s,
                                onClick = { patternStyle = s },
                                label = { Text(s, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Prices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = regularPrice,
                        onValueChange = { regularPrice = it },
                        label = { Text("Cena regularna (zł)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discountPrice,
                        onValueChange = { discountPrice = it },
                        label = { Text("Cena promo (zł)") },
                        placeholder = { Text("Opcjonalnie") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notatki / Opis") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                        return@Button
                    }

                    val regP = regularPrice.toDoubleOrNull() ?: 299.00
                    val discP = discountPrice.toDoubleOrNull()
                    val w = widthCm.toIntOrNull() ?: 160
                    val l = lengthCm.toIntOrNull() ?: 230
                    val qty = stockQuantity.toIntOrNull() ?: 1

                    val updatedCarpet = (carpet ?: Carpet(name = name, ean = ean, lmCode = lmCode, widthCm = w, lengthCm = l, material = material, patternStyle = patternStyle, regularPrice = regP)).copy(
                        name = name.trim(),
                        ean = if (ean.isNotBlank()) ean.trim() else "5901234" + (lmCode.takeLast(6).ifBlank { "000001" }),
                        lmCode = if (lmCode.isNotBlank()) lmCode.trim() else (82000000 + (name.hashCode().coerceAtLeast(0) % 999999)).toString(),
                        widthCm = w,
                        lengthCm = l,
                        material = material.trim(),
                        patternStyle = patternStyle,
                        regularPrice = regP,
                        discountPrice = if (discP != null && discP < regP) discP else null,
                        stockQuantity = qty,
                        standSlot = palakSlot,
                        slotSide = slotSide,
                        notes = notes.trim().ifBlank { null }
                    )

                    onSave(updatedCarpet)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary)
            ) {
                Text(if (carpet == null) "Dodaj na Pałąk #$palakSlot ($slotSide)" else "Zapisz zmiany")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
