package com.example.realtimepricetracker.data.remote

import app.cash.turbine.test
import com.example.realtimepricetracker.data.dto.ConnectionStatus
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PricingServiceTest {

    private lateinit var client: OkHttpClient
    private lateinit var webSocket: WebSocket
    private lateinit var service: PricingService
    private lateinit var capturedListener: WebSocketListener

    @Before
    fun setup() {
        client = mockk(relaxed = true)
        webSocket = mockk(relaxed = true)

        every {
            client.newWebSocket(any(), any())
        } answers {
            capturedListener = secondArg()
            webSocket
        }

        service = PricingService(
            client = client,
            url = "wss://ws.postman-echo.com/raw"
        )
    }

    @Test
    fun `connect sets Connecting then Connected on onOpen`() = runTest {
        assertEquals(ConnectionStatus.Disconnected, service.connectionStatus.value)

        service.connect()

        val response = Response.Builder()
            .request(Request.Builder().url("https://example.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(101)
            .message("Switching Protocols")
            .build()

        capturedListener.onOpen(webSocket, response)

        assertEquals(ConnectionStatus.Connected, service.connectionStatus.value)
    }

    @Test
    fun `onMessage emits value to flow`() = runTest {
        service.connect()
        val expected = "TEST_MESSAGE"

        service.incomingMessages.test {
            capturedListener.onMessage(webSocket, expected)
            assertEquals(expected, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onClosing updates status and closes websocket`() = runTest {
        service.connect()

        capturedListener.onClosing(webSocket, 1000, "Closing soon")

        assertEquals(ConnectionStatus.Disconnected, service.connectionStatus.value)
        verify { webSocket.close(1000, "Closing soon") }
    }

    @Test
    fun `onFailure sets status to Disconnected`() = runTest {
        service.connect()

        capturedListener.onFailure(
            webSocket,
            RuntimeException("boom"),
            null
        )

        assertEquals(ConnectionStatus.Disconnected, service.connectionStatus.value)
    }

    @Test
    fun `send delegates text to websocket send`() {
        service.connect()

        val msg = "Hello!!"

        service.send(msg)

        verify { webSocket.send(msg) }
    }

    @Test
    fun `close closes websocket and resets status`() {
        service.connect()

        service.close()

        verify { webSocket.close(1000, "Closing") }
        assertEquals(ConnectionStatus.Disconnected, service.connectionStatus.value)
    }

    @Test
    fun `connect called twice should NOT create second socket`() {
        service.connect()
        service.connect()

        verify(exactly = 1) { client.newWebSocket(any(), any()) }
    }
}
