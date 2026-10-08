package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Carpet
import com.example.ui.theme.LeroyGreenPrimary
import com.example.ui.theme.PromoRed

@Composable
fun DiscountCalculatorView(
    carpet: Carpet,
    onDismiss: () -> Unit,
    onApplyDiscount: (Double?) -> Unit
) {
    var regularPriceStr by remember { mutableStateOf(String.format("%.2f", carpet.regularPrice).replace(",", ".")) }
    var discountPercentStr by remember { mutableStateOf(carpet.discountPercentage.let { if (it > 0) it.toString() else "15" }) }
    var promoPriceStr by remember {
        mutableStateOf(
            carpet.discountPrice?.let { String.format("%.2f", it).replace(",", ".") } ?: ""
        )
    }

    val regularPrice = regularPriceStr.toDoubleOrNull() ?: carpet.regularPrice
    val discountPercent = discountPercentStr.toIntOrNull() ?: 0

    // Calculated values
    val calculatedPromoPrice = if (discountPercent in 1..99) {
        regularPrice * (1.0 - (discountPercent / 100.0))
    } else {
        promoPriceStr.toDoubleOrNull() ?: regularPrice
    }
    val savings = (regularPrice - calculatedPromoPrice).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = LeroyGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Kalkulator rabatu")
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
                    text = "${carpet.dimensionsFormatted} • Kod LM: ${carpet.lmCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = regularPriceStr,
                    onValueChange = { regularPriceStr = it },
                    label = { Text("Cena regularna (zł)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Szybki wybór rabatu:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 15, 20, 30, 50).forEach { pct ->
                        FilterChip(
                            selected = discountPercentStr == pct.toString(),
                            onClick = {
                                discountPercentStr = pct.toString()
                                promoPriceStr = String.format("%.2f", regularPrice * (1.0 - pct / 100.0)).replace(",", ".")
                            },
                            label = { Text("-$pct%") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = discountPercentStr,
                    onValueChange = {
                        discountPercentStr = it
                        val pct = it.toIntOrNull()
                        if (pct != null && pct in 1..99) {
                            promoPriceStr = String.format("%.2f", regularPrice * (1.0 - pct / 100.0)).replace(",", ".")
                        }
                    },
                    label = { Text("Procent rabatu (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Nowa cena promocyjna:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${String.format("%.2f", calculatedPromoPrice)} zł",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PromoRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Oszczędność klienta:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "-${String.format("%.2f", savings)} zł",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = LeroyGreenPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyDiscount(calculatedPromoPrice)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Zastosuj rabat")
            }
        },
        dismissButton = {
            Row {
                if (carpet.hasDiscount) {
                    OutlinedButton(
                        onClick = {
                            onApplyDiscount(null)
                            onDismiss()
                        }
                    ) {
                        Text("Usuń rabat")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Anuluj")
                }
            }
        }
    )
}
