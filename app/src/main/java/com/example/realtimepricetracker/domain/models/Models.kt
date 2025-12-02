package com.example.realtimepricetracker.domain.models

enum class PriceChangeDirection { Up, Down, None }
enum class PriceFlashState { None, Up, Down }

enum class ConnectionStatus {Connected, Disconnected }

data class StockPrice(
    val symbol: String,
    val price: Double,
    val previousPrice: Double?,
    val lastUpdatedMillis: Long? = null
)