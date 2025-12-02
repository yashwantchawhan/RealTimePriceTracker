package com.example.realtimepricetracker.domain.repository


import com.example.realtimepricetracker.data.dto.ConnectionStatus
import com.example.realtimepricetracker.data.dto.StockPrice
import kotlinx.coroutines.flow.StateFlow

interface PriceRepository {

    val prices: StateFlow<Map<String, StockPrice>>

    val connectionStatus: StateFlow<ConnectionStatus>

    fun connect()

    fun close()

    fun sendRandomPrices(symbols: List<String>)
}
