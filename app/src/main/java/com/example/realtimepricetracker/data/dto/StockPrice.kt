package com.example.realtimepricetracker.data.dto

data class StockPrice(
    val symbol: String,
    val price: Double,
    val previousPrice: Double?,
    val lastUpdatedMillis: Long? = null
)

enum class ConnectionStatus { Connecting, Connected, Disconnected }
