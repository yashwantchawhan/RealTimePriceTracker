package com.example.realtimepricetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimepricetracker.core.utils.StockConfig
import com.example.realtimepricetracker.data.dto.ConnectionStatus
import com.example.realtimepricetracker.data.dto.StockPrice
import com.example.realtimepricetracker.domain.models.PriceChangeDirection
import com.example.realtimepricetracker.domain.models.PriceFlashState
import com.example.realtimepricetracker.domain.repository.PriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriceViewModel @Inject constructor(
    private val repository: PriceRepository,
    private val config: StockConfig,
) : ViewModel() {

    private val _isFeedRunning = MutableStateFlow(false)
    private val _flashStates = MutableStateFlow<Map<String, PriceFlashState>>(emptyMap())

    private val stockSymbols = config.symbols
    private val priceFeedDelayMillis = 2_000L
    private val flashDurationMillis = 1_000L

    val uiState: StateFlow<PriceTrackerUiState> =
        combine(
            repository.prices,
            repository.connectionStatus,
            _isFeedRunning,
            _flashStates
        ) { pricesMap, connectionStatus, isRunning, flashStates ->
            buildUiState(
                pricesMap = pricesMap,
                connectionStatus = connectionStatus,
                isRunning = isRunning,
                flashStates = flashStates
            )
        }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = initialUiState()
            )

    private var feedJob: Job? = null

    init {
        repository.connect()
        observeFlashes()
    }

    fun toggleFeed() {
        val newValue = !_isFeedRunning.value
        _isFeedRunning.value = newValue
        if (newValue) startFeed() else stopFeed()
    }

    private fun startFeed() {
        feedJob?.cancel()
        feedJob = viewModelScope.launch {
            while (_isFeedRunning.value) {
                repository.sendRandomPrices(stockSymbols)
                delay(priceFeedDelayMillis)
            }
        }
    }

    private fun stopFeed() {
        feedJob?.cancel()
        feedJob = null
    }
    private val flashClearJobs = mutableMapOf<String, Job>()

    private fun observeFlashes() {
        viewModelScope.launch {
            repository.prices.collect { current ->
                current.values.forEach { stock ->
                    val prevPrice = stock.previousPrice ?: return@forEach

                    val flashState = when {
                        stock.price > prevPrice -> PriceFlashState.Up
                        stock.price < prevPrice -> PriceFlashState.Down
                        else -> null
                    }

                    if (flashState != null) {
                        _flashStates.update { it + (stock.symbol to flashState) }

                        flashClearJobs[stock.symbol]?.cancel()

                        flashClearJobs[stock.symbol] = viewModelScope.launch {
                            delay(flashDurationMillis)
                            _flashStates.update { map ->
                                if (map[stock.symbol] == flashState) map - stock.symbol else map
                            }
                        }
                    }
                }
            }
        }
    }

    private fun buildUiState(
        pricesMap: Map<String, StockPrice>,
        connectionStatus: ConnectionStatus,
        isRunning: Boolean,
        flashStates: Map<String, PriceFlashState>
    ): PriceTrackerUiState {
        val priced = pricesMap.values.sortedByDescending { it.price }

        val pricedRows = priced.map { stock ->
            val direction = when {
                stock.previousPrice == null -> PriceChangeDirection.None
                stock.price > stock.previousPrice -> PriceChangeDirection.Up
                stock.price < stock.previousPrice -> PriceChangeDirection.Down
                else -> PriceChangeDirection.None
            }

            PriceRowUi(
                symbol = stock.symbol,
                priceText = "$${"%.2f".format(stock.price)}",
                changeDirection = direction,
                flashState = flashStates[stock.symbol] ?: PriceFlashState.None
            )
        }

        val notSeen = stockSymbols.filter { it !in pricesMap.keys }
        val placeholderRows = notSeen.map { symbol ->
            PriceRowUi(
                symbol = symbol,
                priceText = "--",
                changeDirection = PriceChangeDirection.None,
                flashState = PriceFlashState.None
            )
        }

        return PriceTrackerUiState(
            connectionStatus = connectionStatus,
            isFeedRunning = isRunning,
            priceList = PriceList(prices = pricedRows + placeholderRows)
        )
    }

    private fun initialUiState(): PriceTrackerUiState =
        PriceTrackerUiState(
            connectionStatus = ConnectionStatus.Disconnected,
            isFeedRunning = false,
            priceList = PriceList(
                prices = stockSymbols.map { symbol ->
                    PriceRowUi(
                        symbol = symbol,
                        priceText = "--",
                        changeDirection = PriceChangeDirection.None,
                        flashState = PriceFlashState.None
                    )
                }
            )
        )

    override fun onCleared() {
        super.onCleared()
        repository.close()
        flashClearJobs.clear()
    }
}
