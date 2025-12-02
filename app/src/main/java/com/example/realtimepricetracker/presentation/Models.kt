package com.example.realtimepricetracker.presentation

import androidx.compose.runtime.Immutable
import com.example.realtimepricetracker.data.dto.ConnectionStatus
import com.example.realtimepricetracker.domain.models.PriceChangeDirection
import com.example.realtimepricetracker.domain.models.PriceFlashState

@Immutable
data class PriceTrackerUiState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val isFeedRunning: Boolean = false,
    val priceList: PriceList
)


@Immutable
data class PriceRowUi(
    val symbol: String,
    val priceText: String,
    val changeDirection: PriceChangeDirection,
    val flashState: PriceFlashState
)

@Immutable
data class PriceList(val prices: List<PriceRowUi> = emptyList())
