package ai.qwenton.apex.network

import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import java.util.concurrent.atomic.AtomicBoolean

data class WsEvent(
    val type: String,
    val requestId: String? = null,
    val commandId: String? = null,
    val stream: String? = null,
    val text: String? = null,
    val exitCode: Int? = null,
    val code: String? = null,
    val message: String? = null
)

class WebSocketClient(
    private val client: OkHttpClient = NetworkModule.getOkHttpClient(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private val isRunning = AtomicBoolean(false)
    private var retryDelayMs = 1000L
    private val maxRetryDelayMs = 16000L

    private val _events = MutableSharedFlow<WsEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<WsEvent> = _events

    fun connect(wsUrl: String = "ws://127.0.0.1:8765/ws/events") {
        if (isRunning.getAndSet(true)) return

        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                retryDelayMs = 1000L // Reset backoff on success
                scope.launch {
                    _events.emit(WsEvent(type = "connected"))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val event = gson.fromJson(text, WsEvent::class.java)
                    scope.launch {
                        _events.emit(event)
                    }
                } catch (e: Exception) {
                    // Ignore malformed frame
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                scope.launch {
                    _events.emit(WsEvent(type = "error", message = t.localizedMessage ?: "Connection failure"))
                }
                reconnectWithBackoff(wsUrl)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                scope.launch {
                    _events.emit(WsEvent(type = "disconnected", message = reason))
                }
            }
        })
    }

    private fun reconnectWithBackoff(wsUrl: String) {
        if (!isRunning.get()) return
        scope.launch {
            delay(retryDelayMs)
            retryDelayMs = (retryDelayMs * 2).coerceAtMost(maxRetryDelayMs)
            isRunning.set(false)
            connect(wsUrl)
        }
    }

    fun disconnect() {
        isRunning.set(false)
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
    }
}
