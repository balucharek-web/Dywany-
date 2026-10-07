package com.example.data.sync

import com.example.data.model.SyncPayload
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class WifiSyncServer(
    private val port: Int = 8988,
    private val onPayloadReceived: suspend (SyncPayload, String) -> SyncPayload
) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(SyncPayload::class.java)

    var isRunning: Boolean = false
        private set

    fun start(scope: CoroutineScope, onStarted: (Int) -> Unit = {}, onError: (String) -> Unit = {}) {
        if (isRunning) return

        serverJob = scope.launch(Dispatchers.IO) {
            try {
                val server = ServerSocket(port)
                serverSocket = server
                isRunning = true
                withContext(Dispatchers.Main) {
                    onStarted(server.localPort)
                }

                while (isActive && !server.isClosed) {
                    try {
                        val clientSocket = server.accept()
                        handleClient(clientSocket)
                    } catch (e: Exception) {
                        if (!server.isClosed) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                isRunning = false
                withContext(Dispatchers.Main) {
                    onError("Błąd serwera: ${e.message}")
                }
            }
        }
    }

    private fun handleClient(socket: Socket) {
        CoroutineScope(Dispatchers.IO).launch {
            val clientIp = socket.inetAddress.hostAddress ?: "Nieznane IP"
            try {
                socket.soTimeout = 10000
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val writer = PrintWriter(socket.getOutputStream(), true)

                val line = reader.readLine()
                if (line != null) {
                    val incoming = adapter.fromJson(line)
                    if (incoming != null) {
                        val replyPayload = onPayloadReceived(incoming, clientIp)
                        val replyJson = adapter.toJson(replyPayload)
                        writer.println(replyJson)
                        writer.flush()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    socket.close()
                } catch (_: Exception) {}
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        serverJob?.cancel()
        serverJob = null
    }
}
