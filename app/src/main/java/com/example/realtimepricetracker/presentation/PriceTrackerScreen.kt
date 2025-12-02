@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)

package com.example.realtimepricetracker.presentation

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.realtimepricetracker.data.dto.ConnectionStatus
import com.example.realtimepricetracker.domain.models.PriceChangeDirection
import com.example.realtimepricetracker.domain.models.PriceFlashState
import com.example.realtimepricetracker.presentation.theme.PriceTrackerTheme

@Composable
fun PriceTrackerRoute(
    viewModel: PriceViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PriceTrackerTheme {
        PriceTrackerScreen(
            state = state,
            onToggleFeed = viewModel::toggleFeed
        )
    }
}

@Composable
fun PriceTrackerScreen(
    state: PriceTrackerUiState,
    onToggleFeed: () -> Unit
) {
    Scaffold(
        topBar = {
            PriceTrackerTopBar(
                status = state.connectionStatus,
                isRunning = state.isFeedRunning,
                onToggleFeed = onToggleFeed
            )
        }
    ) { padding ->
        PriceList(
            prices = state.priceList,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@Composable
private fun PriceTrackerTopBar(
    status: ConnectionStatus,
    isRunning: Boolean,
    onToggleFeed: () -> Unit
) {
    TopAppBar(
        title = { Text("Price Tracker") },
        navigationIcon = {
            ConnectionStatusIndicator(
                status = status,
                isRunning = isRunning
            )
        },
        actions = {
            Spacer(Modifier.width(8.dp))
            FeedToggleButton(
                isRunning = isRunning,
                onClick = onToggleFeed
            )
            Spacer(Modifier.width(8.dp))
        }
    )
}

@Composable
fun ConnectionStatusIndicator(
    status: ConnectionStatus,
    isRunning: Boolean
) {
    val (icon, label) = remember(status, isRunning) {
        when {
            !isRunning -> "🔴" to "Disconnected"
            status == ConnectionStatus.Connecting -> "🟡" to "Connecting"
            status == ConnectionStatus.Connected -> "🟢" to "Connected"
            else -> "🔴" to "Disconnected"
        }
    }

    Row(
        modifier = Modifier.padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon)
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun FeedToggleButton(
    isRunning: Boolean,
    onClick: () -> Unit
) {
    val text = if (isRunning) "Stop" else "Start"
    val containerColor =
        if (isRunning) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary

    val contentColor =
        if (isRunning) MaterialTheme.colorScheme.onError
        else MaterialTheme.colorScheme.onPrimary

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text)
    }
}

@Composable
fun PriceList(
    prices: PriceList,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = prices.prices,
            key = { it.symbol } // stable key → stable subcomposition
        ) { row ->
            PriceRow(
                item = row,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
            )
        }
    }
}

@Composable
fun PriceRow(
    item: PriceRowUi,
    modifier: Modifier = Modifier
) {
    val targetFlashColor = when (item.flashState) {
        PriceFlashState.Up ->
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        PriceFlashState.Down ->
            MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
        PriceFlashState.None ->
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f)
    }

    // smooth flash in/out
    val flashBackgroundColor by animateColorAsState(
        targetValue = targetFlashColor,
        label = "priceFlashBackground"
    )

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Symbol
            Text(
                text = item.symbol,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            // Price chip + arrow
            Box(
                modifier = Modifier
                    .background(
                        color = flashBackgroundColor,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.priceText,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (item.changeDirection) {
                            PriceChangeDirection.Up -> "↑"
                            PriceChangeDirection.Down -> "↓"
                            PriceChangeDirection.None -> ""
                        },
                        color = when (item.changeDirection) {
                            PriceChangeDirection.Up -> MaterialTheme.colorScheme.primary
                            PriceChangeDirection.Down -> MaterialTheme.colorScheme.error
                            PriceChangeDirection.None -> MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

private val previewRows = listOf(
    PriceRowUi(
        symbol = "AAPL",
        priceText = "$193.42",
        changeDirection = PriceChangeDirection.Up,
        flashState = PriceFlashState.Up
    ),
    PriceRowUi(
        symbol = "GOOG",
        priceText = "$141.87",
        changeDirection = PriceChangeDirection.Down,
        flashState = PriceFlashState.Down
    ),
    PriceRowUi(
        symbol = "NVDA",
        priceText = "$1,120.10",
        changeDirection = PriceChangeDirection.None,
        flashState = PriceFlashState.None
    )
)

private val previewState = PriceTrackerUiState(
    connectionStatus = ConnectionStatus.Connected,
    isFeedRunning = true,
    priceList = PriceList(prices = previewRows)
)

@Preview(
    name = "Price Tracker – Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Price Tracker – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PriceTrackerScreenPreview() {
    PriceTrackerTheme {
        PriceTrackerScreen(
            state = previewState,
            onToggleFeed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PriceRowPreview() {
    PriceTrackerTheme {
        PriceRow(
            item = previewRows.first(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectionStatusIndicatorPreview_Connected() {
    PriceTrackerTheme {
        ConnectionStatusIndicator(
            status = ConnectionStatus.Connected,
            isRunning = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectionStatusIndicatorPreview_Disconnected() {
    PriceTrackerTheme {
        ConnectionStatusIndicator(
            status = ConnectionStatus.Disconnected,
            isRunning = false
        )
    }
}
