package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "carpets")
data class Carpet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val ean: String,
    val lmCode: String,
    val widthCm: Int,
    val lengthCm: Int,
    val material: String,
    val patternStyle: String,
    val regularPrice: Double,
    val discountPrice: Double? = null,
    val stockQuantity: Int = 1,
    val standId: Long? = 1L,
    val standSlot: Int? = 1,                 // Numer pałąka (1..30)
    val slotSide: String = "A",              // "A" (Dywan A) lub "B" (Dywan B) na pałąku
    val warehouseContainer: String? = null,  // Zachowane wstecznie
    val warehouseSlot: String? = null,       // Zachowane wstecznie
    val isReserved: Boolean = false,
    val reservedFor: String? = null,
    val reservedPhone: String? = null,
    val notes: String? = null,
    val imageUrl: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val dimensionsFormatted: String
        get() = "${widthCm} × ${lengthCm} cm"

    val currentPrice: Double
        get() = discountPrice ?: regularPrice

    val hasDiscount: Boolean
        get() = discountPrice != null && discountPrice < regularPrice

    val discountPercentage: Int
        get() {
            if (discountPrice == null || regularPrice <= 0) return 0
            val pct = ((regularPrice - discountPrice) / regularPrice) * 100
            return pct.toInt().coerceIn(1, 99)
        }

    val isOnStand: Boolean
        get() = standSlot != null

    val palakTitle: String
        get() = if (standSlot != null) "Pałąk #$standSlot" else "Brak pałąka"

    val palakSideTitle: String
        get() = if (slotSide.equals("B", ignoreCase = true)) "Dywan B" else "Dywan A"

    val locationShortBadge: String
        get() = if (standSlot != null) "P$standSlot-${if (slotSide.equals("B", ignoreCase = true)) "B" else "A"}" else "Brak"

    val locationSummary: String
        get() = if (standSlot != null) "Pałąk #$standSlot • $palakSideTitle" else "Brak przypisanego pałąka"
}
