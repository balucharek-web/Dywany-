package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "sync_logs")
data class SyncLogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceIp: String,
    val deviceName: String,
    val direction: String,
    val itemsCount: Int,
    val status: String,
    val details: String
)
