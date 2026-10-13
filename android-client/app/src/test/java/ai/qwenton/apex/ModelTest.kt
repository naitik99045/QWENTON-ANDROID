package ai.qwenton.apex

import ai.qwenton.apex.domain.ChatMessage
import ai.qwenton.apex.domain.CommandProposal
import ai.qwenton.apex.domain.MessageRole
import ai.qwenton.apex.domain.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelTest {

    @Test
    fun testRiskLevelLabels() {
        assertEquals("Safe", RiskLevel.SAFE.label)
        assertEquals("Approval Required", RiskLevel.MEDIUM.label)
        assertEquals("High Risk", RiskLevel.HIGH.label)
        assertEquals("Blocked", RiskLevel.BLOCKED.label)
    }

    @Test
    fun testChatMessageCreation() {
        val message = ChatMessage(
            id = "test_1",
            conversationId = "conv_1",
            role = MessageRole.USER,
            content = "Hello Qwenton Apex",
            isStreaming = false
        )
        assertEquals("test_1", message.id)
        assertEquals(MessageRole.USER, message.role)
        assertEquals("Hello Qwenton Apex", message.content)
        assertFalse(message.isStreaming)
    }

    @Test
    fun testCommandProposalIntegrity() {
        val proposal = CommandProposal(
            commandId = "cmd_123",
            requestId = "req_123",
            tool = "get_battery",
            program = "termux-battery-status",
            arguments = emptyList(),
            workingDirectory = "/data/data/com.termux/files/home",
            risk = RiskLevel.SAFE,
            requiresApproval = false,
            explanation = "Read battery state"
        )
        assertEquals("cmd_123", proposal.commandId)
        assertEquals("get_battery", proposal.tool)
        assertFalse(proposal.requiresApproval)
        assertEquals(RiskLevel.SAFE, proposal.risk)
    }
}
