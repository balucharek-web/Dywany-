package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "display_stands")
data class DisplayStand(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val name: String,
    val section: String,
    val totalSlots: Int = 30,
    val maxDimensions: String = "160×230 cm",
    val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
