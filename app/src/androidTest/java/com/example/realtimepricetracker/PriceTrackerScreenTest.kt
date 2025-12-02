package com.example.realtimepricetracker


import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.realtimepricetracker.data.dto.ConnectionStatus
import com.example.realtimepricetracker.domain.models.PriceChangeDirection
import com.example.realtimepricetracker.domain.models.PriceFlashState
import com.example.realtimepricetracker.presentation.PriceList
import com.example.realtimepricetracker.presentation.PriceRowUi
import com.example.realtimepricetracker.presentation.PriceTrackerScreen
import com.example.realtimepricetracker.presentation.PriceTrackerUiState
import com.example.realtimepricetracker.presentation.theme.PriceTrackerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PriceTrackerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreen(
        state: PriceTrackerUiState,
        onToggleFeed: () -> Unit = {}
    ) {
        composeRule.setContent {
            PriceTrackerTheme {
                PriceTrackerScreen(
                    state = state,
                    onToggleFeed = onToggleFeed
                )
            }
        }
    }

    private fun sampleState(
        status: ConnectionStatus = ConnectionStatus.Disconnected,
        isRunning: Boolean = false
    ): PriceTrackerUiState {
        val rows = listOf(
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
            )
        )

        return PriceTrackerUiState(
            connectionStatus = status,
            isFeedRunning = isRunning,
            priceList = PriceList(prices = rows)
        )
    }

    @Test
    fun topBar_showsTitle_andStartButton_whenFeedStopped() {
        val state = sampleState(
            status = ConnectionStatus.Disconnected,
            isRunning = false
        )

        setScreen(state)

        composeRule
            .onNodeWithText("Price Tracker")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("🔴")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Disconnected")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Start")
            .assertIsDisplayed()
    }

    @Test
    fun topBar_showsStopButton_andConnectedStatus_whenFeedRunning() {
        // Given: feed running and connected
        val state = sampleState(
            status = ConnectionStatus.Connected,
            isRunning = true
        )

        setScreen(state)

        composeRule
            .onNodeWithText("🟢")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Connected")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Stop")
            .assertIsDisplayed()
    }

    @Test
    fun clickingToggleFeed_invokesCallback() {
        var toggleCount = 0

        val state = sampleState(
            status = ConnectionStatus.Connected,
            isRunning = false
        )

        setScreen(
            state = state,
            onToggleFeed = { toggleCount++ }
        )

        composeRule
            .onNodeWithText("Start")
            .performClick()

        assert(toggleCount == 1) {
            "Expected toggleCount to be 1 but was $toggleCount"
        }
    }

    @Test
    fun priceList_displaysSymbols_andPrices() {
        val state = sampleState(
            status = ConnectionStatus.Connected,
            isRunning = true
        )

        setScreen(state)

        composeRule.onNodeWithText("AAPL").assertIsDisplayed()
        composeRule.onNodeWithText("GOOG").assertIsDisplayed()

        composeRule.onNodeWithText("$193.42").assertIsDisplayed()
        composeRule.onNodeWithText("$141.87").assertIsDisplayed()
    }
}
