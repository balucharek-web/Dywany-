package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Carpet
import com.example.data.model.DisplayStand
import com.example.ui.theme.LeroyGreenPrimary
import com.example.ui.theme.PromoRed

@Composable
fun PrintLabelDialog(
    carpet: Carpet,
    stand: DisplayStand?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = LeroyGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Podgląd etykiety ekspozycyjnej")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Printable Label Simulation Card (White with dark border)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LEROY MERLIN",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFF4D7C0F),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "DZIAŁ DYWANÓW",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = Color(0xFF1E293B), thickness = 2.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Stand / Location Badge
                        val locationText = if (stand != null && carpet.standSlot != null) {
                            "STOJAK: ${stand.code}  •  RAMIĘ: #${carpet.standSlot}"
                        } else {
                            "MAGAZYN / BEZ EKSPOZYCJI"
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = locationText,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Carpet Name
                        Text(
                            text = carpet.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black,
                            lineHeight = 18.sp
                        )

                        // Dimensions & Material
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wymiary: ${carpet.dimensionsFormatted}",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF0284C7)
                        )
                        Text(
                            text = "Materiał: ${carpet.material}",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "Styl: ${carpet.patternStyle}",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Price
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                if (carpet.hasDiscount) {
                                    Text(
                                        text = "${String.format("%.2f", carpet.regularPrice)} zł",
                                        fontSize = 13.sp,
                                        color = Color(0xFF94A3B8),
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                    Text(
                                        text = "SUPER CENA",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = PromoRed
                                    )
                                } else {
                                    Text(
                                        text = "CENA BRUTTO",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Text(
                                text = "${String.format("%.2f", carpet.currentPrice)} zł",
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = if (carpet.hasDiscount) PromoRed else Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Simulated Barcode
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(34.dp)
                            ) {
                                val barWidth = 3.dp.toPx()
                                val step = 6.dp.toPx()
                                var currentX = 0f
                                val seed = carpet.ean.hashCode()
                                var index = 0
                                while (currentX < size.width) {
                                    val isThick = ((seed shr (index % 16)) and 1) == 1
                                    val w = if (isThick) barWidth * 1.5f else barWidth
                                    drawLine(
                                        color = Color.Black,
                                        start = Offset(currentX, 0f),
                                        end = Offset(currentX, size.height),
                                        strokeWidth = w
                                    )
                                    currentX += step
                                    index++
                                }
                            }
                            Text(
                                text = "${carpet.ean}   [LM: ${carpet.lmCode}]",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "Wysłano etykietę do drukarki etykiet Zebra / Wi-Fi", Toast.LENGTH_LONG).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeroyGreenPrimary)
            ) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Drukuj etykietę")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
}
