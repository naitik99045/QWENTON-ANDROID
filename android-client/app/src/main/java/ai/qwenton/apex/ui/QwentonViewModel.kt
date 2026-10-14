package ai.qwenton.apex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.qwenton.apex.domain.*
import ai.qwenton.apex.network.WebSocketClient
import ai.qwenton.apex.network.WsEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

val DEFAULT_SKILLS = listOf(
    SkillInfo(
        id = "get_time",
        name = "Get Current Time",
        description = "Read local system date and time.",
        permissionRequired = "None",
        riskLevel = RiskLevel.SAFE,
        isEnabled = true
    ),
    SkillInfo(
        id = "get_battery",
        name = "Inspect Battery Status",
        description = "Query battery charge level and health via Termux:API.",
        permissionRequired = "Termux:API",
        riskLevel = RiskLevel.SAFE,
        isEnabled = true
    ),
    SkillInfo(
        id = "list_home",
        name = "List User Home Directory",
        description = "List files inside the Termux user home directory (~/).",
        permissionRequired = "Storage (Home)",
        riskLevel = RiskLevel.SAFE,
        isEnabled = true
    ),
    SkillInfo(
        id = "read_allowed_file",
        name = "Read Allowed File",
        description = "Read the contents of a safe, specified file within sandbox.",
        permissionRequired = "Approval Required",
        riskLevel = RiskLevel.MEDIUM,
        isEnabled = true
    ),
    SkillInfo(
        id = "install_python_package",
        name = "Install Python Package",
        description = "Install dependencies inside Termux virtualenv via pip.",
        permissionRequired = "Approval Required",
        riskLevel = RiskLevel.MEDIUM,
        isEnabled = true
    ),
    SkillInfo(
        id = "run_known_script",
        name = "Run Known Script",
        description = "Execute a pre-registered automation script from ~/.qwenton/scripts/.",
        permissionRequired = "Approval Required",
        riskLevel = RiskLevel.HIGH,
        isEnabled = true
    ),
    SkillInfo(
        id = "coding_agent",
        name = "Autonomous Coding Engine",
        description = "Explore directory trees, read code, write files, and apply surgical patches.",
        permissionRequired = "Approval Required",
        riskLevel = RiskLevel.MEDIUM,
        isEnabled = true
    ),
    SkillInfo(
        id = "sast_pentest",
        name = "SAST Security Pentest",
        description = "Perform automated defensive vulnerability and secret scanning on project files.",
        permissionRequired = "None",
        riskLevel = RiskLevel.SAFE,
        isEnabled = true
    ),
    SkillInfo(
        id = "adb_device_control",
        name = "Shizuku / ADB Device Automation",
        description = "Execute privileged shell commands, input taps, keystrokes, and dumpsys via ADB.",
        permissionRequired = "Shizuku / ADB (UID 2000)",
        riskLevel = RiskLevel.HIGH,
        isEnabled = true
    )
)

data class QwentonUiState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected("Termux server not checked yet"),
    val serverUrl: String = "http://127.0.0.1:8765",
    val messages: List<ChatMessage> = emptyList(),
    val isStreaming: Boolean = false,
    val pendingProposal: CommandProposal? = null,
    val activeExecution: TerminalExecution? = null,
    val ttsEnabled: Boolean = false,
    val skills: List<SkillInfo> = DEFAULT_SKILLS,
    val errorMessage: String? = null,
    val activeWorkspace: String = "~/workspace",
    val activeModel: String = "Gemini 1.5 Pro",
    val batteryInfo: String? = null,
    val aiProvider: String = "gemini",
    val apiKey: String = "",
    val shizukuAvailable: Boolean = false,
    val configSyncStatus: String? = null
)

class QwentonViewModel(
    private val repository: QwentonRepository,
    private val wsClient: WebSocketClient = WebSocketClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow(QwentonUiState())
    val uiState: StateFlow<QwentonUiState> = _uiState.asStateFlow()

    private var activeConversationId = "conv_default"

    init {
        // Observe Room DB messages
        viewModelScope.launch {
            repository.getMessagesFlow().collect { msgList ->
                _uiState.update { it.copy(messages = msgList) }
            }
        }

        // Observe WebSocket events
        viewModelScope.launch {
            wsClient.events.collect { event ->
                handleWsEvent(event)
            }
        }

        // Initial health check probe
        testConnection()
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.Connecting) }
            val result = repository.checkHealth()
            if (result.isSuccess) {
                _uiState.update { it.copy(connectionStatus = ConnectionStatus.Connected) }
                // Connect WebSocket
                val wsUrl = _uiState.value.serverUrl.replace("http://", "ws://").replace("https://", "wss://") + "/ws/events"
                wsClient.connect(wsUrl)
                fetchAiConfig()
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Connection refused"
                _uiState.update { it.copy(connectionStatus = ConnectionStatus.Disconnected(err)) }
            }
        }
    }

    fun setServerUrl(url: String) {
        _uiState.update { it.copy(serverUrl = url) }
        repository.updateBaseUrl(url)
        testConnection()
    }

    fun setWorkspace(path: String) {
        _uiState.update { it.copy(activeWorkspace = path) }
    }

    fun fetchAiConfig() {
        viewModelScope.launch {
            val res = repository.getAiConfig()
            res.onSuccess { cfg ->
                _uiState.update {
                    it.copy(
                        aiProvider = cfg.provider,
                        activeModel = cfg.model,
                        configSyncStatus = "Synced with Termux server"
                    )
                }
            }
        }
    }

    fun setAiConfig(provider: String, apiKey: String, model: String) {
        _uiState.update {
            it.copy(
                aiProvider = provider,
                activeModel = model,
                configSyncStatus = "Saving to Termux .env..."
            )
        }
        viewModelScope.launch {
            val res = repository.updateAiProvider(provider, apiKey, model)
            res.onSuccess { resp ->
                _uiState.update {
                    it.copy(configSyncStatus = "Key Vault synchronized: ${resp.provider} (${resp.model})")
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(configSyncStatus = "Sync failed: ${err.localizedMessage}")
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _uiState.value.isStreaming) return

        val requestId = "req_${UUID.randomUUID().toString().take(8)}"
        _uiState.update { it.copy(isStreaming = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.sendChat(
                requestId = requestId,
                conversationId = activeConversationId,
                message = text,
                contextPath = _uiState.value.activeWorkspace
            )

            _uiState.update { it.copy(isStreaming = false) }

            result.onSuccess { resp ->
                if (resp.type == "command_request" && resp.commandId != null) {
                    val proposal = CommandProposal(
                        commandId = resp.commandId,
                        requestId = resp.requestId,
                        tool = resp.tool ?: resp.program ?: "command",
                        program = resp.program ?: "sh",
                        arguments = (resp.arguments?.get("args") as? List<*>)?.map { it.toString() } ?: emptyList(),
                        workingDirectory = resp.workingDirectory ?: _uiState.value.activeWorkspace,
                        risk = when (resp.risk?.lowercase()) {
                            "high" -> RiskLevel.HIGH
                            "blocked" -> RiskLevel.BLOCKED
                            else -> RiskLevel.MEDIUM
                        },
                        requiresApproval = resp.requiresApproval ?: true,
                        explanation = resp.explanation ?: "Execute system operation"
                    )
                    _uiState.update { it.copy(pendingProposal = proposal) }
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        errorMessage = err.localizedMessage,
                        connectionStatus = ConnectionStatus.Error(err.localizedMessage ?: "Network error")
                    )
                }
            }
        }
    }

    fun approveCommand(commandId: String) {
        val proposal = _uiState.value.pendingProposal ?: return
        _uiState.update {
            it.copy(
                pendingProposal = null,
                activeExecution = TerminalExecution(
                    commandId = commandId,
                    program = proposal.program,
                    arguments = proposal.arguments,
                    stdout = "Executing with cryptographic token...",
                    stderr = "",
                    exitCode = null,
                    isRunning = true,
                    startTime = System.currentTimeMillis(),
                    durationMs = null
                )
            )
        }

        viewModelScope.launch {
            val approvalId = "appr_${UUID.randomUUID().toString().take(8)}"
            val res = repository.approveCommand(commandId, approvalId)
            res.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        activeExecution = current.activeExecution?.copy(
                            isRunning = false,
                            exitCode = 0,
                            durationMs = System.currentTimeMillis() - (current.activeExecution.startTime)
                        )
                    )
                }
            }.onFailure { err ->
                _uiState.update { current ->
                    current.copy(
                        activeExecution = current.activeExecution?.copy(
                            isRunning = false,
                            stderr = err.localizedMessage ?: "Execution failed",
                            exitCode = -1
                        )
                    )
                }
            }
        }
    }

    fun rejectCommand(commandId: String) {
        _uiState.update { it.copy(pendingProposal = null) }
        viewModelScope.launch {
            repository.cancelCommand(commandId)
        }
    }

    fun stopRequest() {
        _uiState.update { it.copy(isStreaming = false) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteMessage(id: String) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun toggleTts(enabled: Boolean) {
        _uiState.update { it.copy(ttsEnabled = enabled) }
    }

    fun toggleSkill(id: String, enabled: Boolean) {
        _uiState.update { current ->
            current.copy(
                skills = current.skills.map { if (it.id == id) it.copy(isEnabled = enabled) else it }
            )
        }
    }

    private fun handleWsEvent(event: WsEvent) {
        when (event.type) {
            "connected" -> {
                _uiState.update { it.copy(connectionStatus = ConnectionStatus.Connected) }
            }
            "command_output" -> {
                _uiState.update { current ->
                    val exec = current.activeExecution
                    if (exec != null && exec.commandId == event.commandId) {
                        val text = event.text ?: ""
                        if (event.stream == "stderr") {
                            current.copy(activeExecution = exec.copy(stderr = exec.stderr + text))
                        } else {
                            current.copy(activeExecution = exec.copy(stdout = exec.stdout + text))
                        }
                    } else current
                }
            }
            "command_completed" -> {
                _uiState.update { current ->
                    val exec = current.activeExecution
                    if (exec != null && exec.commandId == event.commandId) {
                        current.copy(
                            activeExecution = exec.copy(
                                isRunning = false,
                                exitCode = event.exitCode ?: 0,
                                durationMs = System.currentTimeMillis() - exec.startTime
                            )
                        )
                    } else current
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsClient.disconnect()
    }
}
