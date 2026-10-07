package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Carpet
import kotlinx.coroutines.flow.Flow

@Dao
interface CarpetDao {
    @Query("SELECT * FROM carpets ORDER BY name ASC")
    fun getAllCarpets(): Flow<List<Carpet>>

    @Query("SELECT * FROM carpets WHERE id = :id LIMIT 1")
    fun getCarpetById(id: Long): Flow<Carpet?>

    @Query("SELECT * FROM carpets WHERE id = :id LIMIT 1")
    suspend fun getCarpetByIdOnce(id: Long): Carpet?

    @Query("SELECT * FROM carpets WHERE ean = :ean LIMIT 1")
    suspend fun findCarpetByEan(ean: String): Carpet?

    @Query("SELECT * FROM carpets WHERE lmCode = :lmCode LIMIT 1")
    suspend fun findCarpetByLmCode(lmCode: String): Carpet?

    @Query("SELECT * FROM carpets WHERE standId = :standId ORDER BY standSlot ASC")
    fun getCarpetsByStand(standId: Long): Flow<List<Carpet>>

    @Query("SELECT * FROM carpets WHERE standId = :standId AND standSlot = :slot LIMIT 1")
    suspend fun getCarpetOnStandSlot(standId: Long, slot: Int): Carpet?

    @Query("""
        SELECT * FROM carpets 
        WHERE name LIKE '%' || :query || '%' 
           OR ean LIKE '%' || :query || '%' 
           OR lmCode LIKE '%' || :query || '%' 
           OR patternStyle LIKE '%' || :query || '%'
           OR material LIKE '%' || :query || '%'
           OR warehouseContainer LIKE '%' || :query || '%'
           OR warehouseSlot LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchCarpets(query: String): Flow<List<Carpet>>

    @Query("SELECT * FROM carpets WHERE standId IS NOT NULL ORDER BY standId ASC, standSlot ASC")
    fun getCarpetsOnStands(): Flow<List<Carpet>>

    @Query("SELECT * FROM carpets WHERE standId IS NULL ORDER BY warehouseContainer ASC, warehouseSlot ASC, name ASC")
    fun getWarehouseCarpets(): Flow<List<Carpet>>

    @Query("SELECT * FROM carpets WHERE warehouseContainer = :container ORDER BY warehouseSlot ASC, name ASC")
    fun getCarpetsByContainer(container: String): Flow<List<Carpet>>

    @Query("SELECT * FROM carpets WHERE warehouseContainer = :container AND warehouseSlot = :slot ORDER BY name ASC")
    fun getCarpetsByContainerAndSlot(container: String, slot: String): Flow<List<Carpet>>

    @Query("SELECT * FROM carpets WHERE isReserved = 1 ORDER BY name ASC")
    fun getReservedCarpets(): Flow<List<Carpet>>

    @Query("SELECT * FROM carpets WHERE discountPrice IS NOT NULL AND discountPrice < regularPrice ORDER BY name ASC")
    fun getDiscountedCarpets(): Flow<List<Carpet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(carpet: Carpet): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(carpets: List<Carpet>)

    @Update
    suspend fun update(carpet: Carpet)

    @Delete
    suspend fun delete(carpet: Carpet)

    @Query("DELETE FROM carpets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE carpets SET standId = NULL, standSlot = NULL, updatedAt = :timestamp WHERE standId = :standId")
    suspend fun clearStandAssignments(standId: Long, timestamp: Long = System.currentTimeMillis())
}
