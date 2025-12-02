package com.example.realtimepricetracker.data.remote

import com.example.realtimepricetracker.data.dto.ConnectionStatus
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*

class PricingService(
    private val client: OkHttpClient = OkHttpClient(),
    private val url: String = "wss://ws.postman-echo.com/raw",
) {

    private var webSocket: WebSocket? = null

    private val _incomingMessages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingMessages: SharedFlow<String> = _incomingMessages

    private val _connectionStatus =
        MutableStateFlow(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            _connectionStatus.value = ConnectionStatus.Connected
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            _incomingMessages.tryEmit(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            _connectionStatus.value = ConnectionStatus.Disconnected
            webSocket.close(code, reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            _connectionStatus.value = ConnectionStatus.Disconnected
            t.printStackTrace()
        }
    }

    fun connect() {
        if (webSocket != null) return
        _connectionStatus.value = ConnectionStatus.Connecting
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun send(text: String) {
        webSocket?.send(text)
    }

    fun close() {
        webSocket?.close(1000, "Closing")
        webSocket = null
        _connectionStatus.value = ConnectionStatus.Disconnected
    }
}
