package ai.qwenton.apex

import ai.qwenton.apex.data.ConversationDao
import ai.qwenton.apex.data.ExecutionDao
import ai.qwenton.apex.data.MessageEntity
import ai.qwenton.apex.domain.ChatMessage
import ai.qwenton.apex.domain.ConnectionStatus
import ai.qwenton.apex.domain.MessageRole
import ai.qwenton.apex.domain.QwentonRepository
import ai.qwenton.apex.network.ChatResponseDto
import ai.qwenton.apex.network.HealthResponseDto
import ai.qwenton.apex.ui.QwentonUiState
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ViewModelTest {

    @Test
    fun testInitialUiStateDefaults() {
        val state = QwentonUiState()
        assertEquals("http://127.0.0.1:8765", state.serverUrl)
        assertTrue(state.messages.isEmpty())
        assertFalse(state.isStreaming)
        assertNull(state.pendingProposal)
        assertNull(state.activeExecution)
        assertFalse(state.ttsEnabled)
        assertFalse(state.skills.isEmpty())
        assertEquals(6, state.skills.size)
    }

    @Test
    fun testConnectionStatusTransitions() {
        var status: ConnectionStatus = ConnectionStatus.Connecting
        assertTrue(status is ConnectionStatus.Connecting)

        status = ConnectionStatus.Connected
        assertTrue(status is ConnectionStatus.Connected)

        status = ConnectionStatus.Disconnected("Port closed")
        assertTrue(status is ConnectionStatus.Disconnected)
        assertEquals("Port closed", (status as ConnectionStatus.Disconnected).reason)

        status = ConnectionStatus.Error("Timeout 5000ms")
        assertTrue(status is ConnectionStatus.Error)
        assertEquals("Timeout 5000ms", (status as ConnectionStatus.Error).message)
    }

    @Test
    fun testMessageRoleHierarchy() {
        val userMsg = ChatMessage("1", "c1", MessageRole.USER, "Ping")
        val botMsg = ChatMessage("2", "c1", MessageRole.ASSISTANT, "Pong")
        val toolMsg = ChatMessage("3", "c1", MessageRole.TOOL, "Output")

        assertEquals(MessageRole.USER, userMsg.role)
        assertEquals(MessageRole.ASSISTANT, botMsg.role)
        assertEquals(MessageRole.TOOL, toolMsg.role)
    }

    @Test
    fun testNetworkModuleUrlValidation() {
        assertTrue(ai.qwenton.apex.network.NetworkModule.isValidUrl("http://127.0.0.1:8765"))
        assertTrue(ai.qwenton.apex.network.NetworkModule.isValidUrl("https://127.0.0.1:8765/"))
        assertFalse(ai.qwenton.apex.network.NetworkModule.isValidUrl("ftp://invalid"))
        assertFalse(ai.qwenton.apex.network.NetworkModule.isValidUrl("javascript:alert(1)"))
    }
}
