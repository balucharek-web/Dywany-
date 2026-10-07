package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Carpet
import com.example.data.model.DisplayStand
import com.example.data.model.LeroyMerlinProduct
import com.example.ui.MainViewModel
import com.example.ui.components.CarpetPatternBadge
import com.example.ui.components.CreateTakeDownOrderDialog
import com.example.ui.scanner.CameraScannerView
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.LeroyGreenPrimary
import com.example.ui.theme.PromoRed

@Composable
fun ScannerScreen(
    viewModel: MainViewModel,
    onOpenCarpetDetail: (Carpet) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allCarpets by viewModel.allCarpets.collectAsStateWithLifecycle()
    val allStands by viewModel.allStands.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var isTorchEnabled by remember { mutableStateOf(false) }
    var manualInputCode by remember { mutableStateOf("") }
    var matchedCarpet by remember { mutableStateOf<Carpet?>(null) }
    var externalLmMatch by remember { mutableStateOf<LeroyMerlinProduct?>(null) }
    var notFoundSearched by remember { mutableStateOf(false) }
    var takeDownCarpetTarget by remember { mutableStateOf<Carpet?>(null) }

    fun processCodeScan(scannedCode: String) {
        val clean = scannedCode.trim()
        if (clean.isBlank()) return

        notFoundSearched = false
        externalLmMatch = null

        val localMatch = allCarpets.find {
            it.ean.equals(clean, ignoreCase = true) || it.lmCode.equals(clean, ignoreCase = true)
        }

        if (localMatch != null) {
            matchedCarpet = localMatch
        } else {
            matchedCarpet = null
            notFoundSearched = true
            externalLmMatch = viewModel.lookupLeroyMerlin(clean)
        }
    }

    // Automatically ask for camera permission on screen entry if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Bar info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = LeroyGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Skaner kodów EAN / LM",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row {
                IconButton(onClick = { isTorchEnabled = !isTorchEnabled }) {
                    Icon(
                        imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Latarka",
                        tint = if (isTorchEnabled) AmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!hasCameraPermission) {
                    IconButton(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Zezwól na aparat",
                            tint = LeroyGreenPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Camera Scanner Viewfinder with live ZXing frame analysis & emulator fallback
        CameraScannerView(
            hasCameraPermission = hasCameraPermission,
            isTorchEnabled = isTorchEnabled,
            onBarcodeScanned = { scannedCode ->
                manualInputCode = scannedCode
                processCodeScan(scannedCode)
            },
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Manual Input Field
        OutlinedTextField(
            value = manualInputCode,
            onValueChange = { manualInputCode = it },
            placeholder = { Text("Wpisz kod EAN (13 cyfr) lub kod LM (8 cyfr)...") },
            trailingIcon = {
                IconButton(onClick = { processCodeScan(manualInputCode) }) {
                    Icon(Icons.Default.Search, contentDescription = "Skanuj / Szukaj")
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("scanner_manual_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick simulation chips for easy testing in browser emulator
        Text(
            text = "Szybki test (przykładowe kody z ekspozycji):",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(allCarpets.take(5)) { c ->
                FilterChip(
                    selected = manualInputCode == c.lmCode,
                    onClick = {
                        manualInputCode = c.lmCode
                        processCodeScan(c.lmCode)
                    },
                    label = { Text("LM ${c.lmCode}") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scan Result Section
        if (matchedCarpet != null) {
            val c = matchedCarpet!!
            val stand = allStands.find { it.id == c.standId }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scanner_result_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ZNALEZIONO PRODUKT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "${String.format("%.2f", c.currentPrice)} zł",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = if (c.hasDiscount) PromoRed else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CarpetPatternBadge(patternStyle = c.patternStyle, size = 48.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = c.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${c.dimensionsFormatted} • ${c.material}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (c.isOnStand) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(12.dp)
                    ) {
                        if (c.isOnStand && stand != null) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = LeroyGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Lokalizacja: Stojak ${stand.code} • Ramię #${c.standSlot}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${stand.name} (${stand.section})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "Dywan znajduje się w magazynie: ${c.warehouseLocationFormatted}. Ilość: ${c.stockQuantity} szt.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (c.isOnStand) {
                            Button(
                                onClick = { takeDownCarpetTarget = c },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.VerticalAlignBottom, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ściągnij")
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.selectCarpet(c)
                                onOpenCarpetDetail(c)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Szczegóły")
                        }
                    }
                }
            }
        } else if (notFoundSearched) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Brak produktu w lokalnym stanie sklepu.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    externalLmMatch?.let { lm ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Znaleziono w centralnej bazie Leroy Merlin: ${lm.title} (${lm.dimensionsFormatted}) - ${String.format("%.2f", lm.price)} zł",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val newC = Carpet(
                                    name = lm.title,
                                    ean = lm.ean,
                                    lmCode = lm.lmCode,
                                    widthCm = lm.widthCm,
                                    lengthCm = lm.lengthCm,
                                    material = lm.material,
                                    patternStyle = lm.patternStyle,
                                    regularPrice = lm.price,
                                    discountPrice = lm.promoPrice,
                                    stockQuantity = lm.stockEstimate,
                                    warehouseContainer = "Kontener 1",
                                    warehouseSlot = "A",
                                    notes = lm.description
                                )
                                viewModel.saveCarpet(newC)
                                matchedCarpet = newC
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary)
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dodaj ten dywan do sklepu")
                        }
                    }
                }
            }
        }
    }

    takeDownCarpetTarget?.let { carpet ->
        val stand = allStands.find { it.id == carpet.standId }
        CreateTakeDownOrderDialog(
            carpet = carpet,
            stand = stand,
            onDismiss = { takeDownCarpetTarget = null },
            onSubmit = { reason, cName, cPhone, priority ->
                viewModel.createTakeDownOrder(carpet, reason, cName, cPhone, priority)
                takeDownCarpetTarget = null
            }
        )
    }
}
