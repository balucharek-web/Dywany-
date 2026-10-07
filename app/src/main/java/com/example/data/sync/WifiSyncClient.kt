package com.example.data.sync

import com.example.data.model.SyncPayload
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class WifiSyncClient {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(SyncPayload::class.java)

    suspend fun syncWithPeer(
        peerIp: String,
        port: Int = 8988,
        outgoingPayload: SyncPayload,
        timeoutMs: Int = 8000
    ): Result<SyncPayload> = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        try {
            socket = Socket()
            socket.connect(InetSocketAddress(peerIp, port), timeoutMs)
            socket.soTimeout = timeoutMs

            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            val outgoingJson = adapter.toJson(outgoingPayload)
            writer.println(outgoingJson)
            writer.flush()

            val responseLine = reader.readLine()
                ?: return@withContext Result.failure(Exception("Brak odpowiedzi od urządzenia $peerIp"))

            val incomingPayload = adapter.fromJson(responseLine)
                ?: return@withContext Result.failure(Exception("Niepoprawny format danych od $peerIp"))

            Result.success(incomingPayload)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    suspend fun pingPeer(peerIp: String, port: Int = 8988, timeoutMs: Int = 1500): Boolean =
        withContext(Dispatchers.IO) {
            var socket: Socket? = null
            try {
                socket = Socket()
                socket.connect(InetSocketAddress(peerIp, port), timeoutMs)
                true
            } catch (e: Exception) {
                false
            } finally {
                try {
                    socket?.close()
                } catch (_: Exception) {}
            }
        }
}
