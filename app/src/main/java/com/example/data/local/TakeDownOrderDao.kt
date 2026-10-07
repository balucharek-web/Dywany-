package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TakeDownOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface TakeDownOrderDao {
    @Query("SELECT * FROM take_down_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<TakeDownOrder>>

    @Query("SELECT * FROM take_down_orders WHERE status IN ('Oczekujące', 'W trakcie') ORDER BY priority DESC, createdAt ASC")
    fun getActiveOrders(): Flow<List<TakeDownOrder>>

    @Query("SELECT * FROM take_down_orders WHERE carpetId = :carpetId ORDER BY createdAt DESC")
    fun getOrdersForCarpet(carpetId: Long): Flow<List<TakeDownOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: TakeDownOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<TakeDownOrder>)

    @Update
    suspend fun update(order: TakeDownOrder)

    @Delete
    suspend fun delete(order: TakeDownOrder)

    @Query("UPDATE take_down_orders SET status = :status, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, completedAt: Long?, updatedAt: Long = System.currentTimeMillis())
}
