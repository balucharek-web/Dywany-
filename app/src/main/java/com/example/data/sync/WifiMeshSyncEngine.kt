package com.example.data.sync

import com.example.data.local.AppDatabase
import com.example.data.model.Carpet
import com.example.data.model.DisplayStand
import com.example.data.model.SyncLogEntry
import com.example.data.model.SyncPayload
import com.example.data.model.TakeDownOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class WifiMeshSyncEngine(
    private val database: AppDatabase,
    private val scope: CoroutineScope
) {
    private val deviceId = UUID.randomUUID().toString().take(8)
    private val deviceName = NetworkUtils.getDeviceName()
    private val client = WifiSyncClient()
    private var scanJob: Job? = null

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<DiscoveredPeer>>(emptyList())
    val discoveredPeers: StateFlow<List<DiscoveredPeer>> = _discoveredPeers.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    private val server = WifiSyncServer(port = 8988) { incoming, clientIp ->
        handleIncomingPayload(incoming, clientIp)
    }

    init {
        startServer()
    }

    fun startServer() {
        if (!_isServerRunning.value) {
            server.start(
                scope = scope,
                onStarted = {
                    _isServerRunning.value = true
                },
                onError = { err ->
                    _isServerRunning.value = false
                    _syncStatusMessage.value = err
                }
            )
        }
    }

    fun stopServer() {
        server.stop()
        _isServerRunning.value = false
    }

    suspend fun createLocalPayload(): SyncPayload {
        val carpets = database.carpetDao().getAllCarpets().first()
        val stands = database.displayStandDao().getAllStands().first()
        val orders = database.takeDownOrderDao().getAllOrders().first()
        return SyncPayload(
            deviceId = deviceId,
            deviceName = deviceName,
            carpets = carpets,
            stands = stands,
            takeDownOrders = orders
        )
    }

    private suspend fun handleIncomingPayload(payload: SyncPayload, peerIp: String): SyncPayload {
        val mergedCount = mergePayload(payload)
        database.syncLogDao().insert(
            SyncLogEntry(
                deviceIp = peerIp,
                deviceName = payload.deviceName,
                direction = "Odebrano",
                itemsCount = mergedCount,
                status = "Sukces",
                details = "Odebrano i scalono bazę z ${payload.deviceName} ($peerIp)"
            )
        )
        return createLocalPayload()
    }

    private suspend fun mergePayload(payload: SyncPayload): Int {
        var count = 0
        val carpetDao = database.carpetDao()
        val standDao = database.displayStandDao()
        val orderDao = database.takeDownOrderDao()

        // Merge stands
        for (incomingStand in payload.stands) {
            val existing = standDao.getStandByCode(incomingStand.code)
            if (existing == null) {
                standDao.insert(incomingStand.copy(id = 0))
                count++
            } else if (incomingStand.updatedAt > existing.updatedAt) {
                standDao.update(incomingStand.copy(id = existing.id))
                count++
            }
        }

        // Merge carpets
        for (incomingCarpet in payload.carpets) {
            val existing = carpetDao.findCarpetByEan(incomingCarpet.ean)
                ?: carpetDao.findCarpetByLmCode(incomingCarpet.lmCode)
            if (existing == null) {
                carpetDao.insert(incomingCarpet.copy(id = 0))
                count++
            } else if (incomingCarpet.updatedAt > existing.updatedAt) {
                carpetDao.update(incomingCarpet.copy(id = existing.id))
                count++
            }
        }

        // Merge orders
        for (incomingOrder in payload.takeDownOrders) {
            val existingOrders = orderDao.getOrdersForCarpet(incomingOrder.carpetId).first()
            val match = existingOrders.find {
                it.createdAt == incomingOrder.createdAt || (it.carpetName == incomingOrder.carpetName && it.customerName == incomingOrder.customerName)
            }
            if (match == null) {
                orderDao.insert(incomingOrder.copy(id = 0))
                count++
            } else if (incomingOrder.updatedAt > match.updatedAt) {
                orderDao.update(incomingOrder.copy(id = match.id))
                count++
            }
        }

        return count
    }

    fun syncWithPeer(peerIp: String, onFinished: (Boolean, String) -> Unit) {
        scope.launch {
            try {
                _syncStatusMessage.value = "Synchronizacja z $peerIp..."
                val localPayload = createLocalPayload()
                val result = client.syncWithPeer(peerIp = peerIp, outgoingPayload = localPayload)

                result.onSuccess { remotePayload ->
                    val merged = mergePayload(remotePayload)
                    database.syncLogDao().insert(
                        SyncLogEntry(
                            deviceIp = peerIp,
                            deviceName = remotePayload.deviceName,
                            direction = "Wysłano/Odebrano",
                            itemsCount = merged,
                            status = "Sukces",
                            details = "Zsynchronizowano z ${remotePayload.deviceName}. Zaktualizowano elementów: $merged"
                        )
                    )
                    val msg = "Zsynchronizowano pomyślnie z ${remotePayload.deviceName}! ($merged zmian)"
                    _syncStatusMessage.value = msg
                    withContext(Dispatchers.Main) { onFinished(true, msg) }
                }.onFailure { ex ->
                    val errorMsg = ex.message ?: "Błąd połączenia z $peerIp"
                    database.syncLogDao().insert(
                        SyncLogEntry(
                            deviceIp = peerIp,
                            deviceName = "Nieznane",
                            direction = "Błąd",
                            itemsCount = 0,
                            status = "Błąd",
                            details = errorMsg
                        )
                    )
                    _syncStatusMessage.value = errorMsg
                    withContext(Dispatchers.Main) { onFinished(false, errorMsg) }
                }
            } catch (e: Exception) {
                val errorMsg = "Błąd: ${e.message}"
                _syncStatusMessage.value = errorMsg
                withContext(Dispatchers.Main) { onFinished(false, errorMsg) }
            }
        }
    }

    fun scanLocalSubnet() {
        if (_isScanning.value) return
        _isScanning.value = true
        _syncStatusMessage.value = "Skanowanie sieci Wi-Fi w poszukiwaniu urządzeń..."

        scanJob?.cancel()
        scanJob = scope.launch(Dispatchers.IO) {
            val localIp = NetworkUtils.getLocalIpAddress()
            val prefix = NetworkUtils.getSubnetPrefix(localIp)
            val currentLastOctet = localIp.split(".").getOrNull(3)?.toIntOrNull() ?: 100

            val peersFound = mutableListOf<DiscoveredPeer>()

            // Scan nearby IPs in parallel batches
            val range = (1..254).filter { it != currentLastOctet }
            val batches = range.chunked(25)

            for (batch in batches) {
                val deferreds = batch.map { octet ->
                    async {
                        val targetIp = "$prefix.$octet"
                        val isOnline = client.pingPeer(targetIp, port = 8988, timeoutMs = 400)
                        if (isOnline) {
                            DiscoveredPeer(
                                ip = targetIp,
                                name = "Terminal Dywany #$octet",
                                isOnline = true
                            )
                        } else null
                    }
                }
                val batchResults = deferreds.awaitAll().filterNotNull()
                peersFound.addAll(batchResults)
                if (peersFound.isNotEmpty()) {
                    _discoveredPeers.value = peersFound.toList()
                }
            }

            _discoveredPeers.value = peersFound.toList()
            _isScanning.value = false
            _syncStatusMessage.value = if (peersFound.isEmpty()) {
                "Nie wykryto innych aktywnych terminali w sieci $prefix.x"
            } else {
                "Znaleziono terminali: ${peersFound.size}"
            }
        }
    }
}
