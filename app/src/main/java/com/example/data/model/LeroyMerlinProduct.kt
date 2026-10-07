package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LeroyMerlinProduct(
    val lmCode: String,
    val ean: String,
    val title: String,
    val category: String,
    val price: Double,
    val promoPrice: Double? = null,
    val widthCm: Int,
    val lengthCm: Int,
    val material: String,
    val patternStyle: String,
    val stockEstimate: Int = 3,
    val description: String = ""
) {
    val dimensionsFormatted: String
        get() = "${widthCm} × ${lengthCm} cm"
}
