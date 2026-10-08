package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "take_down_orders")
data class TakeDownOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carpetId: Long,
    val carpetName: String,
    val standCode: String,
    val standSlot: Int,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val reason: String = "Prezentacja klientowi",
    val status: String = STATUS_PENDING,
    val priority: String = PRIORITY_NORMAL,
    val assignedStaff: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "Oczekujące"
        const val STATUS_IN_PROGRESS = "W trakcie"
        const val STATUS_COMPLETED = "Zrealizowane"
        const val STATUS_CANCELLED = "Anulowane"

        const val PRIORITY_LOW = "Niski"
        const val PRIORITY_NORMAL = "Normalny"
        const val PRIORITY_URGENT = "Pilny"
    }

    val isPending: Boolean
        get() = status == STATUS_PENDING

    val isInProgress: Boolean
        get() = status == STATUS_IN_PROGRESS

    val isCompleted: Boolean
        get() = status == STATUS_COMPLETED
}
