package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.DisplayStand
import com.example.ui.theme.LeroyGreenPrimary

@Composable
fun EditStandDialog(
    stand: DisplayStand?,
    onDismiss: () -> Unit,
    onSave: (DisplayStand) -> Unit,
    onDelete: ((DisplayStand) -> Unit)? = null
) {
    var code by remember { mutableStateOf(stand?.code ?: "ST-") }
    var name by remember { mutableStateOf(stand?.name ?: "") }
    var section by remember { mutableStateOf(stand?.section ?: "Alejka ") }
    var totalSlots by remember { mutableStateOf(stand?.totalSlots?.toString() ?: "30") }
    var maxDimensions by remember { mutableStateOf(stand?.maxDimensions ?: "160×230 cm") }
    var notes by remember { mutableStateOf(stand?.notes ?: "") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ViewCarousel,
                    contentDescription = null,
                    tint = LeroyGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (stand == null) "Dodaj stojak ekspozycyjny" else "Edytuj stojak",
                    modifier = Modifier.weight(1f)
                )
                if (stand != null && onDelete != null) {
                    IconButton(onClick = { onDelete(stand); onDismiss() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń stojak",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it; isError = false },
                    label = { Text("Kod stojaka (np. ST-A1, WIESZAK-1) *") },
                    isError = isError && code.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa stojaka / Opis *") },
                    isError = isError && name.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Lokalizacja w sklepie / Alejka") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = totalSlots,
                        onValueChange = { totalSlots = it },
                        label = { Text("Liczba ramion") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = maxDimensions,
                        onValueChange = { maxDimensions = it },
                        label = { Text("Maks. rozmiar") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Uwagi") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isBlank() || name.isBlank()) {
                        isError = true
                    } else {
                        val slots = totalSlots.toIntOrNull()?.coerceAtLeast(1) ?: 30
                        val result = (stand ?: DisplayStand(
                            code = code.trim(),
                            name = name.trim(),
                            section = section.trim(),
                            totalSlots = slots,
                            maxDimensions = maxDimensions.trim(),
                            notes = notes.trim().ifBlank { null }
                        )).copy(
                            code = code.trim(),
                            name = name.trim(),
                            section = section.trim(),
                            totalSlots = slots,
                            maxDimensions = maxDimensions.trim(),
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
}
