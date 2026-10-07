package com.example.data.sync

data class DiscoveredPeer(
    val ip: String,
    val name: String,
    val port: Int = 8988,
    val lastSeen: Long = System.currentTimeMillis(),
    val isOnline: Boolean = true
)
