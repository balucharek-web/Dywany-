package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SyncPayload(
    val version: Int = 1,
    val deviceId: String,
    val deviceName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val carpets: List<Carpet>,
    val stands: List<DisplayStand>,
    val takeDownOrders: List<TakeDownOrder>
)
