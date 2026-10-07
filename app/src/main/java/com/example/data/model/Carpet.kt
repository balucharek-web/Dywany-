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
    val standId: Long? = null,
    val standSlot: Int? = null,
    val warehouseContainer: String? = null, // "Kontener 1", "Kontener 2"
    val warehouseSlot: String? = null,      // "Miejsce A", "Miejsce B" (lub "A", "B")
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
        get() = standId != null && standSlot != null

    val isInWarehouse: Boolean
        get() = !isOnStand

    val warehouseLocationFormatted: String
        get() = when {
            warehouseContainer != null && warehouseSlot != null ->
                "$warehouseContainer • Miejsce $warehouseSlot"
            warehouseContainer != null -> warehouseContainer
            else -> "Magazyn (ogólny)"
        }

    val locationShortBadge: String
        get() = if (isOnStand) {
            "Ramię #${standSlot ?: '?'}"
        } else if (warehouseContainer != null && warehouseSlot != null) {
            val cNum = if (warehouseContainer.contains("1")) "K1" else if (warehouseContainer.contains("2")) "K2" else "K"
            "$cNum-$warehouseSlot"
        } else {
            "Magazyn"
        }

    val locationSummary: String
        get() = if (isOnStand) "Ramię #${standSlot ?: '?'}" else warehouseLocationFormatted
}
