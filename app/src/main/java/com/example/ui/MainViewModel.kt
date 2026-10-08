package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Carpet
import com.example.data.model.DisplayStand
import com.example.data.model.LeroyMerlinProduct
import com.example.data.model.SyncLogEntry
import com.example.data.model.SyncPayload
import com.example.data.model.TakeDownOrder
import com.example.data.repository.ExpoRepository
import com.example.data.sync.DiscoveredPeer
import com.example.data.sync.WifiMeshSyncEngine
import com.example.data.util.LeroyMerlinParser
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CarpetFilter(val label: String) {
    ALL("Wszystkie dywany"),
    SIDE_A("Miejsce A (Dywan A)"),
    SIDE_B("Miejsce B (Dywan B)"),
    RESERVED("Zarezerwowane"),
    PROMO("W promocji / Rabat")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val syncEngine = WifiMeshSyncEngine(database, viewModelScope)
    private val repository = ExpoRepository(database, syncEngine)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val payloadAdapter = moshi.adapter(SyncPayload::class.java)

    // Raw sources
    val allCarpets: StateFlow<List<Carpet>> = repository.allCarpets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStands: StateFlow<List<DisplayStand>> = repository.allStands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeOrders: StateFlow<List<TakeDownOrder>> = repository.activeOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<TakeDownOrder>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncLogs: StateFlow<List<SyncLogEntry>> = repository.allSyncLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtering & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentFilter = MutableStateFlow(CarpetFilter.ALL)
    val currentFilter: StateFlow<CarpetFilter> = _currentFilter.asStateFlow()

    private val _selectedPatternFilter = MutableStateFlow<String?>(null)
    val selectedPatternFilter: StateFlow<String?> = _selectedPatternFilter.asStateFlow()

    private val _filterByStandId = MutableStateFlow<Long?>(null)
    val filterByStandId: StateFlow<Long?> = _filterByStandId.asStateFlow()

    // Filtered carpets stream
    private data class FilterParams(
        val query: String,
        val filter: CarpetFilter,
        val pattern: String?,
        val standId: Long?
    )

    private val filterParams = combine(
        _searchQuery,
        _currentFilter,
        _selectedPatternFilter,
        _filterByStandId
    ) { query, filter, pattern, standId ->
        FilterParams(query, filter, pattern, standId)
    }

    val filteredCarpets: StateFlow<List<Carpet>> = combine(allCarpets, filterParams) { carpets, params ->
        carpets.filter { carpet ->
            val matchesQuery = params.query.isBlank() ||
                    carpet.name.contains(params.query, ignoreCase = true) ||
                    carpet.ean.contains(params.query, ignoreCase = true) ||
                    carpet.lmCode.contains(params.query, ignoreCase = true) ||
                    carpet.material.contains(params.query, ignoreCase = true) ||
                    carpet.patternStyle.contains(params.query, ignoreCase = true) ||
                    carpet.locationSummary.contains(params.query, ignoreCase = true)

            val matchesFilter = when (params.filter) {
                CarpetFilter.ALL -> true
                CarpetFilter.SIDE_A -> carpet.slotSide.equals("A", ignoreCase = true)
                CarpetFilter.SIDE_B -> carpet.slotSide.equals("B", ignoreCase = true)
                CarpetFilter.RESERVED -> carpet.isReserved
                CarpetFilter.PROMO -> carpet.hasDiscount
            }

            val matchesPattern = params.pattern == null || carpet.patternStyle.equals(params.pattern, ignoreCase = true)
            val matchesStand = params.standId == null || carpet.standId == params.standId

            matchesQuery && matchesFilter && matchesPattern && matchesStand
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selection states
    private val _selectedCarpet = MutableStateFlow<Carpet?>(null)
    val selectedCarpet: StateFlow<Carpet?> = _selectedCarpet.asStateFlow()

    // Wi-Fi Sync state
    val isServerRunning: StateFlow<Boolean> = syncEngine.isServerRunning
    val isScanning: StateFlow<Boolean> = syncEngine.isScanning
    val discoveredPeers: StateFlow<List<DiscoveredPeer>> = syncEngine.discoveredPeers
    val syncStatusMessage: StateFlow<String?> = syncEngine.syncStatusMessage

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: CarpetFilter) {
        _currentFilter.value = filter
    }

    fun setPatternFilter(pattern: String?) {
        _selectedPatternFilter.value = pattern
    }

    fun setFilterByStandId(standId: Long?) {
        _filterByStandId.value = standId
    }

    fun selectCarpet(carpet: Carpet?) {
        _selectedCarpet.value = carpet
    }

    fun saveCarpet(carpet: Carpet) {
        viewModelScope.launch {
            if (carpet.id == 0L) {
                repository.insertCarpet(carpet)
            } else {
                repository.updateCarpet(carpet)
            }
            if (_selectedCarpet.value?.id == carpet.id) {
                _selectedCarpet.value = carpet
            }
        }
    }

    fun deleteCarpet(carpet: Carpet) {
        viewModelScope.launch {
            repository.deleteCarpet(carpet)
            if (_selectedCarpet.value?.id == carpet.id) {
                _selectedCarpet.value = null
            }
        }
    }

    fun insertCarpet(carpet: Carpet) {
        viewModelScope.launch {
            repository.insertCarpet(carpet)
        }
    }

    fun assignCarpetToStand(carpetId: Long, standId: Long?, slot: Int?, slotSide: String = "A") {
        viewModelScope.launch {
            repository.assignCarpetToStand(carpetId, standId, slot, slotSide)
            val updated = repository.getCarpetByIdOnce(carpetId)
            if (_selectedCarpet.value?.id == carpetId) {
                _selectedCarpet.value = updated
            }
        }
    }

    fun assignCarpetToWarehouse(carpetId: Long, container: String, slot: String) {
        assignCarpetToStand(carpetId, 1L, 1, "A")
    }

    fun searchLeroyMerlin(query: String): List<LeroyMerlinProduct> {
        return repository.searchLeroyMerlin(query)
    }

    fun reserveCarpet(carpetId: Long, customerName: String, customerPhone: String) {
        viewModelScope.launch {
            repository.reserveCarpet(carpetId, customerName, customerPhone)
            val updated = repository.getCarpetByIdOnce(carpetId)
            if (_selectedCarpet.value?.id == carpetId) {
                _selectedCarpet.value = updated
            }
        }
    }

    fun cancelReservation(carpetId: Long) {
        viewModelScope.launch {
            repository.cancelReservation(carpetId)
            val updated = repository.getCarpetByIdOnce(carpetId)
            if (_selectedCarpet.value?.id == carpetId) {
                _selectedCarpet.value = updated
            }
        }
    }

    fun saveStand(stand: DisplayStand) {
        viewModelScope.launch {
            if (stand.id == 0L) {
                repository.insertStand(stand)
            } else {
                repository.updateStand(stand)
            }
        }
    }

    fun deleteStand(stand: DisplayStand) {
        viewModelScope.launch {
            repository.deleteStand(stand)
        }
    }

    fun createTakeDownOrder(
        carpet: Carpet,
        reason: String,
        customerName: String?,
        customerPhone: String?,
        priority: String
    ) {
        viewModelScope.launch {
            val stand = allStands.value.find { it.id == carpet.standId }
            val standCode = stand?.code ?: "ST-?"
            val slot = carpet.standSlot ?: 1
            repository.createTakeDownOrder(
                TakeDownOrder(
                    carpetId = carpet.id,
                    carpetName = carpet.name,
                    standCode = standCode,
                    standSlot = slot,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    reason = reason,
                    priority = priority,
                    status = TakeDownOrder.STATUS_PENDING
                )
            )
        }
    }

    fun updateOrderStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    fun deleteOrder(order: TakeDownOrder) {
        viewModelScope.launch {
            repository.deleteOrder(order)
        }
    }

    // Sync Actions
    fun toggleServer(enable: Boolean) {
        if (enable) syncEngine.startServer() else syncEngine.stopServer()
    }

    fun scanLocalNetwork() {
        syncEngine.scanLocalSubnet()
    }

    fun syncWithPeer(peerIp: String, onFinished: (Boolean, String) -> Unit = { _, _ -> }) {
        syncEngine.syncWithPeer(peerIp, onFinished)
    }

    fun clearSyncHistory() {
        viewModelScope.launch {
            repository.clearSyncLogs()
        }
    }

    fun lookupLeroyMerlin(query: String): LeroyMerlinProduct? {
        return repository.lookupLeroyMerlin(query)
    }

    suspend fun exportJsonPayload(): String {
        val payload = syncEngine.createLocalPayload()
        return payloadAdapter.toJson(payload)
    }
}
