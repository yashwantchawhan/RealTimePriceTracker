package com.example.realtimepricetracker.data.repository

import com.example.realtimepricetracker.data.remote.PricingService
import com.example.realtimepricetracker.domain.models.ConnectionStatus
import com.example.realtimepricetracker.helper.TestMainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class PriceRepositoryImplTest {

    private lateinit var pricingService: PricingService
    private lateinit var incomingMessages: MutableSharedFlow<String>
    private lateinit var connectionStatusFlow: MutableStateFlow<ConnectionStatus>
    private lateinit var externalScope: TestScope

    val dispatcherRule = TestMainDispatcherRule()

    @Before
    fun setUp() {
        pricingService = mockk(relaxed = true)

        incomingMessages = MutableSharedFlow(replay = 0, extraBufferCapacity = 1)
        connectionStatusFlow = MutableStateFlow(ConnectionStatus.Disconnected)

        every { pricingService.incomingMessages } returns incomingMessages
        every { pricingService.connectionStatus } returns connectionStatusFlow

        externalScope = TestScope(dispatcherRule.dispatcher)
    }

    @Test
    fun `connect delegates to PricingService`() = runTest {
        val repo = PriceRepositoryImpl(
            pricingService = pricingService,
            externalScope = externalScope,
            random = Random(0),
            timeProvider = { 0L })

        repo.connect()

        verify { pricingService.connect() }
    }

    @Test
    fun `close delegates to PricingService`() = runTest {
        val repo = PriceRepositoryImpl(
            pricingService = pricingService,
            externalScope = externalScope,
            random = Random(0),
            timeProvider = { 0L })

        repo.close()

        verify { pricingService.close() }
    }

    @Test
    fun `connectionStatus exposes PricingService connectionStatus`() = runTest {
        val repo = PriceRepositoryImpl(
            pricingService = pricingService,
            externalScope = externalScope,
            random = Random(0),
            timeProvider = { 0L })

        assertSame(connectionStatusFlow, repo.connectionStatus)
    }

    @Test
    fun `sendRandomPrices updates prices and sends messages`() = runTest {
        val now = 1234L

        val repo = PriceRepositoryImpl(
            pricingService = pricingService,
            externalScope = externalScope,
            random = Random(0),
            timeProvider = { now })

        val symbols = listOf("AAPL", "GOOG")

        repo.sendRandomPrices(symbols)

        val prices = repo.prices.value

        assertEquals(2, prices.size)
        assertTrue(prices.keys.containsAll(symbols))

        prices.values.forEach { stock ->
            assertEquals(now, stock.lastUpdatedMillis)
            assertNotNull(stock.price)
        }

        verify(exactly = symbols.size) { pricingService.send(any()) }
        symbols.forEach { symbol ->
            verify {
                pricingService.send(
                    match { msg -> msg.startsWith("$symbol|") })
            }
        }
    }

    @Test
    fun `incoming valid message updates prices map`() = runTest {
        val now = 9999L
        val repo = PriceRepositoryImpl(
            pricingService = pricingService,
            externalScope = externalScope,
            random = Random(0),
            timeProvider = { now })

        externalScope.advanceUntilIdle()

        incomingMessages.emit("AAPL|250.5")

        externalScope.advanceUntilIdle()

        val prices = repo.prices.value
        assertEquals(1, prices.size)

        val stock = prices["AAPL"]
        assertNotNull(stock)
        stock!!

        assertEquals("AAPL", stock.symbol)
        assertEquals(250.5, stock.price, 0.0001)
        assertNull(stock.previousPrice)              // first update
        assertEquals(now, stock.lastUpdatedMillis)
    }

    @Test
    fun `invalid messages are ignored`() = runTest {
        val repo = PriceRepositoryImpl(
            pricingService = pricingService,
            externalScope = externalScope,
            random = Random(0),
            timeProvider = { 0L })

        externalScope.advanceUntilIdle()

        incomingMessages.emit("INVALID")
        incomingMessages.emit("AAPL|notANumber")

        externalScope.advanceUntilIdle()

        assertTrue(repo.prices.value.isEmpty())
    }
}

