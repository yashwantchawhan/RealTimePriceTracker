package com.example.realtimepricetracker.data.repository


import com.example.realtimepricetracker.data.dto.ConnectionStatus
import com.example.realtimepricetracker.data.dto.StockPrice
import com.example.realtimepricetracker.data.remote.PricingService
import com.example.realtimepricetracker.domain.repository.PriceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import javax.inject.Inject

class PriceRepositoryImpl @Inject constructor(
    private val pricingService: PricingService,
    externalScope: CoroutineScope,
    private val random: Random = Random,
    private val timeProvider: () -> Long = System::currentTimeMillis
) : PriceRepository {

    private val _prices = MutableStateFlow<Map<String, StockPrice>>(emptyMap())
    override val prices: StateFlow<Map<String, StockPrice>> = _prices.asStateFlow()

    override val connectionStatus: StateFlow<ConnectionStatus> =
        pricingService.connectionStatus

    init {
        externalScope.launch {
            pricingService.incomingMessages.collect { message ->
                handleIncoming(message)
            }
        }
    }

    override fun connect() = pricingService.connect()

    override fun close() = pricingService.close()

    override fun sendRandomPrices(symbols: List<String>) {
        _prices.update { current ->
            val now = timeProvider()
            val mutable = current.toMutableMap()

            symbols.forEach { symbol ->
                val old = mutable[symbol]
                val previousPrice = old?.price ?: randomInitial()
                val newPrice = randomWalk(previousPrice)

                pricingService.send("$symbol|$newPrice")

                mutable[symbol] = StockPrice(
                    symbol = symbol,
                    price = newPrice,
                    previousPrice = previousPrice,
                    lastUpdatedMillis = now
                )
            }
            mutable
        }
    }

    private fun handleIncoming(message: String) {
        val parts = message.split("|")
        if (parts.size != 2) return

        val symbol = parts[0]
        val price = parts[1].toDoubleOrNull() ?: return
        val now = timeProvider()

        _prices.update { current ->
            val mutable = current.toMutableMap()
            val old = mutable[symbol]
            mutable[symbol] = StockPrice(
                symbol = symbol,
                price = price,
                previousPrice = old?.price,
                lastUpdatedMillis = now
            )
            mutable
        }
    }

    private fun randomInitial(): Double =
        random.nextDouble(50.0, 500.0)

    private fun randomWalk(prev: Double): Double {
        val changePercent = random.nextDouble(-0.02, 0.02)
        val newPrice = prev * (1 + changePercent)
        return "%.2f".format(newPrice).toDouble()
    }
}
