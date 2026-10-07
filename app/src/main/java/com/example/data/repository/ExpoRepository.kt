package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Carpet
import com.example.data.model.DisplayStand
import com.example.data.model.SyncLogEntry
import com.example.data.model.TakeDownOrder
import com.example.data.sync.WifiMeshSyncEngine
import com.example.data.util.LeroyMerlinParser
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

class ExpoRepository(
    private val database: AppDatabase,
    val syncEngine: WifiMeshSyncEngine
) {
    private val carpetDao = database.carpetDao()
    private val standDao = database.displayStandDao()
    private val orderDao = database.takeDownOrderDao()
    private val syncLogDao = database.syncLogDao()

    // Carpets
    val allCarpets: Flow<List<Carpet>> = carpetDao.getAllCarpets()
    val carpetsOnStands: Flow<List<Carpet>> = carpetDao.getCarpetsOnStands()
    val warehouseCarpets: Flow<List<Carpet>> = carpetDao.getWarehouseCarpets()
    val reservedCarpets: Flow<List<Carpet>> = carpetDao.getReservedCarpets()
    val discountedCarpets: Flow<List<Carpet>> = carpetDao.getDiscountedCarpets()

    fun getCarpetById(id: Long): Flow<Carpet?> = carpetDao.getCarpetById(id)

    suspend fun getCarpetByIdOnce(id: Long): Carpet? = carpetDao.getCarpetByIdOnce(id)

    fun getCarpetsByStand(standId: Long): Flow<List<Carpet>> = carpetDao.getCarpetsByStand(standId)

    fun searchCarpets(query: String): Flow<List<Carpet>> = carpetDao.searchCarpets(query)

    suspend fun findCarpetByEan(ean: String): Carpet? = carpetDao.findCarpetByEan(ean)

    suspend fun findCarpetByLmCode(lmCode: String): Carpet? = carpetDao.findCarpetByLmCode(lmCode)

    suspend fun insertCarpet(carpet: Carpet): Long = carpetDao.insert(carpet)

    suspend fun updateCarpet(carpet: Carpet) {
        carpetDao.update(carpet.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteCarpet(carpet: Carpet) = carpetDao.delete(carpet)

    suspend fun assignCarpetToStand(carpetId: Long, standId: Long?, slot: Int?) {
        val carpet = carpetDao.getCarpetByIdOnce(carpetId) ?: return
        carpetDao.update(
            carpet.copy(
                standId = standId,
                standSlot = slot,
                warehouseContainer = if (standId != null) null else carpet.warehouseContainer,
                warehouseSlot = if (standId != null) null else carpet.warehouseSlot,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun assignCarpetToWarehouse(carpetId: Long, container: String, slot: String) {
        val carpet = carpetDao.getCarpetByIdOnce(carpetId) ?: return
        carpetDao.update(
            carpet.copy(
                standId = null,
                standSlot = null,
                warehouseContainer = container,
                warehouseSlot = slot,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun searchLeroyMerlin(query: String) = LeroyMerlinParser.searchProducts(query)

    suspend fun reserveCarpet(carpetId: Long, customerName: String, customerPhone: String) {
        val carpet = carpetDao.getCarpetByIdOnce(carpetId) ?: return
        carpetDao.update(
            carpet.copy(
                isReserved = true,
                reservedFor = customerName,
                reservedPhone = customerPhone,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun cancelReservation(carpetId: Long) {
        val carpet = carpetDao.getCarpetByIdOnce(carpetId) ?: return
        carpetDao.update(
            carpet.copy(
                isReserved = false,
                reservedFor = null,
                reservedPhone = null,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // Stands
    val allStands: Flow<List<DisplayStand>> = standDao.getAllStands()

    fun getStandById(id: Long): Flow<DisplayStand?> = standDao.getStandById(id)

    suspend fun getStandByIdOnce(id: Long): DisplayStand? = standDao.getStandByIdOnce(id)

    suspend fun insertStand(stand: DisplayStand): Long = standDao.insert(stand)

    suspend fun updateStand(stand: DisplayStand) {
        standDao.update(stand.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteStand(stand: DisplayStand) {
        carpetDao.clearStandAssignments(stand.id)
        standDao.delete(stand)
    }

    // Take-down orders
    val allOrders: Flow<List<TakeDownOrder>> = orderDao.getAllOrders()
    val activeOrders: Flow<List<TakeDownOrder>> = orderDao.getActiveOrders()

    suspend fun createTakeDownOrder(order: TakeDownOrder): Long = orderDao.insert(order)

    suspend fun updateOrderStatus(orderId: Long, status: String) {
        val completedAt = if (status == TakeDownOrder.STATUS_COMPLETED) System.currentTimeMillis() else null
        orderDao.updateStatus(orderId, status, completedAt)
    }

    suspend fun deleteOrder(order: TakeDownOrder) = orderDao.delete(order)

    // Sync logs
    val allSyncLogs: Flow<List<SyncLogEntry>> = syncLogDao.getAllLogs()

    suspend fun clearSyncLogs() = syncLogDao.clearLogs()

    // Leroy Merlin parser
    fun lookupLeroyMerlin(code: String) = LeroyMerlinParser.lookupProduct(code)
}
