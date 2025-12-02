package com.example.realtimepricetracker.presentation


import com.example.realtimepricetracker.core.utils.StockConfig
import com.example.realtimepricetracker.data.dto.ConnectionStatus
import com.example.realtimepricetracker.data.dto.StockPrice
import com.example.realtimepricetracker.domain.models.PriceChangeDirection
import com.example.realtimepricetracker.domain.models.PriceFlashState
import com.example.realtimepricetracker.helper.TestMainDispatcherRule
import com.example.realtimepricetracker.presentation.fake.FakePriceRepository
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PriceViewModelTest {

    @get:Rule
    val mainDispatcherRule = TestMainDispatcherRule()

    private val symbols = listOf("AAPL", "GOOG")

    private fun vm(repo: FakePriceRepository): PriceViewModel =
        PriceViewModel(
            repository = repo,
            config = StockConfig(symbols)
        )

    // ----------------------------------------------------------------------
    // TEST 1: INITIAL UI STATE
    // ----------------------------------------------------------------------
    @Test
    fun `initial state shows placeholders for all symbols`() {
        val repo = FakePriceRepository()
        val vm = vm(repo)

        val state = vm.uiState.value

        assertEquals(ConnectionStatus.Connected, state.connectionStatus)
        assertFalse(state.isFeedRunning)
        assertEquals(symbols.size, state.priceList.prices.size)

        state.priceList.prices.forEach { row ->
            assertEquals("--", row.priceText)
            assertEquals(PriceChangeDirection.None, row.changeDirection)
            assertEquals(PriceFlashState.None, row.flashState)
        }
    }

    // ----------------------------------------------------------------------
    // TEST 2: PRICE UP FLASH
    // ----------------------------------------------------------------------
    @Test
    fun `price increase triggers flash Up and direction Up`() {
        val repo = FakePriceRepository()
        val vm = vm(repo)

        val updated = StockPrice(
            symbol = "AAPL",
            price = 200.0,
            previousPrice = 150.0,
            lastUpdatedMillis = 0L
        )

        repo.emitPrices(mapOf("AAPL" to updated))

        val row = vm.uiState.value.priceList.prices.first { it.symbol == "AAPL" }

        assertEquals("$200.00", row.priceText)
        assertEquals(PriceChangeDirection.Up, row.changeDirection)
        assertEquals(PriceFlashState.Up, row.flashState)
    }

    // ----------------------------------------------------------------------
    // TEST 3: PRICE DOWN FLASH
    // ----------------------------------------------------------------------
    @Test
    fun `price decrease triggers flash Down and direction Down`() {
        val repo = FakePriceRepository()
        val vm = vm(repo)

        val updated = StockPrice(
            symbol = "AAPL",
            price = 90.0,
            previousPrice = 120.0,
            lastUpdatedMillis = 0L
        )

        repo.emitPrices(mapOf("AAPL" to updated))

        val row = vm.uiState.value.priceList.prices.first { it.symbol == "AAPL" }

        assertEquals("$90.00", row.priceText)
        assertEquals(PriceChangeDirection.Down, row.changeDirection)
        assertEquals(PriceFlashState.Down, row.flashState)
    }

    // ----------------------------------------------------------------------
    // TEST 4: FEED TOGGLE
    // ----------------------------------------------------------------------
    @Test
    fun `toggleFeed switches feed state`() {
        val repo = FakePriceRepository()
        val vm = vm(repo)

        // Initially false
        assertFalse(vm.uiState.value.isFeedRunning)

        // Turn ON
        vm.toggleFeed()
        assertTrue(vm.uiState.value.isFeedRunning)

        // Turn OFF
        vm.toggleFeed()
        assertFalse(vm.uiState.value.isFeedRunning)
    }
}
