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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
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
    var warehouseContainer by remember { mutableStateOf(carpet?.warehouseContainer ?: "Kontener 1") }
    var warehouseSlot by remember { mutableStateOf(carpet?.warehouseSlot ?: "A") }
    var isWarehouseLocation by remember { mutableStateOf(carpet?.isOnStand != true) }
    var notes by remember { mutableStateOf(carpet?.notes ?: "") }

    var showLmSearchDialog by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

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
                Text(if (carpet == null) "Dodaj nowy dywan" else "Edytuj dane dywanu")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Button: Search in Leroy Merlin
                Button(
                    onClick = { showLmSearchDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pobierz dane ze strony Leroy Merlin")
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

                Spacer(modifier = Modifier.height(14.dp))

                // Warehouse Location Selector: Kontener 1 (A/B), Kontener 2 (A/B)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = LeroyGreenPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Miejsce magazynowe (Kontener / Miejsce):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Container 1 vs Container 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Kontener 1", "Kontener 2").forEach { cName ->
                                FilterChip(
                                    selected = warehouseContainer == cName,
                                    onClick = { warehouseContainer = cName },
                                    label = { Text(cName) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Slot A vs Slot B
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("A", "B").forEach { slot ->
                                FilterChip(
                                    selected = warehouseSlot == slot,
                                    onClick = { warehouseSlot = slot },
                                    label = { Text("Miejsce $slot") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                            label = { Text(size) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Styl wzoru:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Nowoczesny", "Klasyczny", "Geometryczny", "Shaggy", "Vintage", "Boho").forEach { style ->
                        FilterChip(
                            selected = patternStyle == style,
                            onClick = { patternStyle = style },
                            label = { Text(style) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = material,
                    onValueChange = { material = it },
                    label = { Text("Skład / Surowiec") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pricing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = regularPrice,
                        onValueChange = { regularPrice = it },
                        label = { Text("Cena reg. (zł)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discountPrice,
                        onValueChange = { discountPrice = it },
                        label = { Text("Cena promo (zł)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = stockQuantity,
                    onValueChange = { stockQuantity = it },
                    label = { Text("Stan magazynowy (szt.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Uwagi / Opis") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                    } else {
                        val regP = regularPrice.toDoubleOrNull() ?: 0.0
                        val discP = discountPrice.toDoubleOrNull()?.takeIf { it > 0.0 && it < regP }
                        val w = widthCm.toIntOrNull() ?: 160
                        val l = lengthCm.toIntOrNull() ?: 230
                        val qty = stockQuantity.toIntOrNull() ?: 1

                        val result = (carpet ?: Carpet(
                            name = name.trim(),
                            ean = ean.trim().ifBlank { "5900000000000" },
                            lmCode = lmCode.trim().ifBlank { "80000000" },
                            widthCm = w,
                            lengthCm = l,
                            material = material.trim(),
                            patternStyle = patternStyle,
                            regularPrice = regP,
                            discountPrice = discP,
                            stockQuantity = qty,
                            warehouseContainer = warehouseContainer,
                            warehouseSlot = warehouseSlot,
                            notes = notes.trim().ifBlank { null }
                        )).copy(
                            name = name.trim(),
                            ean = ean.trim().ifBlank { "5900000000000" },
                            lmCode = lmCode.trim().ifBlank { "80000000" },
                            widthCm = w,
                            lengthCm = l,
                            material = material.trim(),
                            patternStyle = patternStyle,
                            regularPrice = regP,
                            discountPrice = discP,
                            stockQuantity = qty,
                            warehouseContainer = warehouseContainer,
                            warehouseSlot = warehouseSlot,
                            notes = notes.trim().ifBlank { null },
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(result)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary)
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )

    if (showLmSearchDialog) {
        LeroyMerlinLookupDialog(
            onDismiss = { showLmSearchDialog = false },
            onProductSelected = { lmProduct ->
                name = lmProduct.name
                ean = lmProduct.ean
                lmCode = lmProduct.lmCode
                widthCm = lmProduct.widthCm.toString()
                lengthCm = lmProduct.lengthCm.toString()
                material = lmProduct.material
                patternStyle = lmProduct.patternStyle
                regularPrice = String.format("%.2f", lmProduct.regularPrice).replace(",", ".")
                discountPrice = lmProduct.discountPrice?.let { String.format("%.2f", it).replace(",", ".") } ?: ""
                stockQuantity = lmProduct.stockQuantity.toString()
                lmProduct.warehouseContainer?.let { warehouseContainer = it }
                lmProduct.warehouseSlot?.let { warehouseSlot = it }
                lmProduct.notes?.let { notes = it }
                showLmSearchDialog = false
            }
        )
    }
}
