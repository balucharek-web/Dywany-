package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Carpet
import com.example.data.model.DisplayStand
import com.example.data.model.TakeDownOrder
import com.example.ui.theme.AmberSecondary

@Composable
fun CreateTakeDownOrderDialog(
    carpet: Carpet,
    stand: DisplayStand?,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, customerName: String?, customerPhone: String?, priority: String) -> Unit
) {
    var reason by remember { mutableStateOf("Prezentacja klientowi") }
    var customerName by remember { mutableStateOf(carpet.reservedFor ?: "") }
    var customerPhone by remember { mutableStateOf(carpet.reservedPhone ?: "") }
    var priority by remember { mutableStateOf(TakeDownOrder.PRIORITY_NORMAL) }

    val standCode = stand?.code ?: "ST-?"
    val slot = carpet.standSlot ?: 1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VerticalAlignBottom,
                    contentDescription = null,
                    tint = AmberSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Zlecenie ściągnięcia dywanu")
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
                    text = "Lokalizacja: Stojak $standCode • Ramię #$slot (${carpet.dimensionsFormatted})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Powód ściągnięcia:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Prezentacja", "Zakup / Kasa", "Wymiana ekspozycji").forEach { r ->
                        FilterChip(
                            selected = reason == r,
                            onClick = { reason = r },
                            label = { Text(r) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Priorytet:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        TakeDownOrder.PRIORITY_NORMAL,
                        TakeDownOrder.PRIORITY_URGENT
                    ).forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Klient (opcjonalnie)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Telefon klienta (opcjonalnie)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        reason,
                        customerName.takeIf { it.isNotBlank() },
                        customerPhone.takeIf { it.isNotBlank() },
                        priority
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary)
            ) {
                Text("Utwórz zlecenie")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
