package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DisplayStand
import kotlinx.coroutines.flow.Flow

@Dao
interface DisplayStandDao {
    @Query("SELECT * FROM display_stands ORDER BY code ASC")
    fun getAllStands(): Flow<List<DisplayStand>>

    @Query("SELECT * FROM display_stands WHERE id = :id LIMIT 1")
    fun getStandById(id: Long): Flow<DisplayStand?>

    @Query("SELECT * FROM display_stands WHERE id = :id LIMIT 1")
    suspend fun getStandByIdOnce(id: Long): DisplayStand?

    @Query("SELECT * FROM display_stands WHERE code = :code LIMIT 1")
    suspend fun getStandByCode(code: String): DisplayStand?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stand: DisplayStand): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stands: List<DisplayStand>)

    @Update
    suspend fun update(stand: DisplayStand)

    @Delete
    suspend fun delete(stand: DisplayStand)

    @Query("DELETE FROM display_stands WHERE id = :id")
    suspend fun deleteById(id: Long)
}
