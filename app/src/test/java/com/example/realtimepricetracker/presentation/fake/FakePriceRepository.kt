package com.example.realtimepricetracker.presentation.fake

import com.example.realtimepricetracker.domain.models.ConnectionStatus
import com.example.realtimepricetracker.domain.models.StockPrice
import com.example.realtimepricetracker.domain.repository.PriceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePriceRepository : PriceRepository {

    private val _prices = MutableStateFlow<Map<String, StockPrice>>(emptyMap())
    override val prices: StateFlow<Map<String, StockPrice>> = _prices

    private val _status = MutableStateFlow(ConnectionStatus.Disconnected)
    override val connectionStatus: StateFlow<ConnectionStatus> = _status

    override fun connect() {
        _status.value = ConnectionStatus.Connected
    }

    override fun close() {
        _status.value = ConnectionStatus.Disconnected
    }

    fun emitPrices(newPrices: Map<String, StockPrice>) {
        _prices.value = newPrices
    }

    override fun sendRandomPrices(symbols: List<String>) { }
}
