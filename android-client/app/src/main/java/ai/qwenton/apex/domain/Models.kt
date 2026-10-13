package ai.qwenton.apex.domain

enum class RiskLevel(val label: String) {
    SAFE("Safe"),
    MEDIUM("Approval Required"),
    HIGH("High Risk"),
    BLOCKED("Blocked")
}

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL
}

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val toolProposal: CommandProposal? = null
)

data class CommandProposal(
    val commandId: String,
    val requestId: String,
    val tool: String,
    val program: String,
    val arguments: List<String>,
    val workingDirectory: String,
    val risk: RiskLevel,
    val requiresApproval: Boolean,
    val explanation: String
)

data class TerminalExecution(
    val commandId: String,
    val program: String,
    val arguments: List<String>,
    val stdout: String,
    val stderr: String,
    val exitCode: Int?,
    val isRunning: Boolean,
    val startTime: Long,
    val durationMs: Long?
)

data class SkillInfo(
    val id: String,
    val name: String,
    val description: String,
    val permissionRequired: String,
    val riskLevel: RiskLevel,
    val isEnabled: Boolean
)

sealed class ConnectionStatus {
    object Connected : ConnectionStatus()
    object Connecting : ConnectionStatus()
    data class Disconnected(val reason: String = "Server Offline") : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}
