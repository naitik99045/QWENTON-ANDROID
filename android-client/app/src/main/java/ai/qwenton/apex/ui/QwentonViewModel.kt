package ai.qwenton.apex.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.qwenton.apex.domain.*
import ai.qwenton.apex.network.ChatResponseDto
import ai.qwenton.apex.network.WebSocketClient
import ai.qwenton.apex.network.WsEvent
import ai.qwenton.apex.service.AppLauncherHelper
import ai.qwenton.apex.service.ContactsHelper
import ai.qwenton.apex.service.QwentonNotificationListener
import ai.qwenton.apex.service.ShizukuManager
import ai.qwenton.apex.service.WakeWordService
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
        id = "open_app",
        name = "Universal App Launcher",
        description = "Open any installed Android application (Spotify, YouTube, WhatsApp, Settings).",
        permissionRequired = "None",
        riskLevel = RiskLevel.SAFE,
        isEnabled = true
    ),
    SkillInfo(
        id = "call_contact",
        name = "Contact Search & Calling",
        description = "Search contacts by name and initiate phone calls with confirmation.",
        permissionRequired = "Contacts & Phone",
        riskLevel = RiskLevel.MEDIUM,
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
    val wakeWordEnabled: Boolean = false,
    val skills: List<SkillInfo> = DEFAULT_SKILLS,
    val errorMessage: String? = null,
    val activeWorkspace: String = "~/workspace",
    val activeModel: String = "Gemini 1.5 Pro",
    val batteryInfo: String? = null,
    val aiProvider: String = "gemini",
    val apiKey: String = "",
    val customBaseUrl: String = "",
    val shizukuAvailable: Boolean = false,
    val configSyncStatus: String? = null
)

class QwentonViewModel(
    private val repository: QwentonRepository,
    private val wsClient: WebSocketClient = WebSocketClient(),
    private val appContext: Context? = null
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

        // Shizuku check
        _uiState.update { it.copy(shizukuAvailable = ShizukuManager.isShizukuAvailable()) }

        // Initial health check probe
        testConnection()
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.Connecting) }
            val result = repository.checkHealth()
            if (result.isSuccess) {
                _uiState.update { it.copy(connectionStatus = ConnectionStatus.Connected) }
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
                        customBaseUrl = cfg.baseUrl ?: "",
                        configSyncStatus = "Synced with Termux server"
                    )
                }
            }
        }
    }

    fun setAiConfig(provider: String, apiKey: String, model: String, baseUrl: String = "") {
        _uiState.update {
            it.copy(
                aiProvider = provider,
                activeModel = model,
                customBaseUrl = baseUrl,
                configSyncStatus = "Saving to Termux .env..."
            )
        }
        viewModelScope.launch {
            val res = repository.updateAiProvider(provider, apiKey, model, baseUrl)
            res.onSuccess { resp ->
                _uiState.update {
                    it.copy(
                        customBaseUrl = resp.baseUrl ?: baseUrl,
                        configSyncStatus = "Key Vault synchronized: ${resp.provider} (${resp.model})"
                    )
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
                if (resp.type == "tool_request" && resp.tool != null) {
                    handleClientSideTool(resp)
                } else if (resp.type == "command_request" && resp.commandId != null) {
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

    private fun handleClientSideTool(resp: ChatResponseDto) {
        val tool = resp.tool ?: return
        when (tool) {
            "open_app" -> {
                val appQuery = resp.arguments?.get("app_name")?.toString() ?: ""
                if (appContext != null) {
                    val found = AppLauncherHelper.findPackage(appContext, appQuery)
                    if (found != null) {
                        val launchRes = AppLauncherHelper.launchApp(appContext, found.packageName)
                        if (launchRes.isSuccess) {
                            insertAssistantNotice("🚀 Launching ${found.appName} (${found.packageName})...")
                        } else {
                            insertAssistantNotice("⚠️ Could not open ${found.appName}: ${launchRes.exceptionOrNull()?.localizedMessage}")
                        }
                    } else {
                        insertAssistantNotice("⚠️ Could not locate application matching '$appQuery' on device.")
                    }
                }
            }
            "call_contact" -> {
                val query = resp.arguments?.get("contact_name")?.toString() ?: ""
                if (appContext != null) {
                    val contacts = ContactsHelper.findContacts(appContext, query)
                    if (contacts.isNotEmpty()) {
                        val contact = contacts.first()
                        val proposal = CommandProposal(
                            commandId = "call_${UUID.randomUUID().toString().take(8)}",
                            requestId = resp.requestId,
                            tool = "call_contact",
                            program = "telephony",
                            arguments = listOf(contact.number),
                            workingDirectory = "Android OS",
                            risk = RiskLevel.MEDIUM,
                            requiresApproval = true,
                            explanation = "Place voice call to ${contact.name} (${contact.number})?"
                        )
                        _uiState.update { it.copy(pendingProposal = proposal) }
                    } else {
                        insertAssistantNotice("⚠️ No contacts found matching '$query'. Make sure Contacts permission is granted in Settings.")
                    }
                }
            }
            "pair_wireless_debugging" -> {
                val info = QwentonNotificationListener.lastPairingInfo
                if (info != null) {
                    ShizukuManager.executePrivilegedCommand(arrayOf("adb", "pair", "127.0.0.1:${info.port}", info.code))
                    insertAssistantNotice("✅ Intercepted Wireless Debugging Code: ${info.code} on port ${info.port}. Auto-paired!")
                } else {
                    insertAssistantNotice("ℹ️ Notification Listener active. Turn on 'Wireless debugging' in Developer Options to capture pairing code.")
                }
            }
        }
    }

    fun approveCommand(commandId: String) {
        val proposal = _uiState.value.pendingProposal ?: return

        // 1. Client-Side Telephony / Calling execution
        if (proposal.tool == "call_contact" && proposal.arguments.isNotEmpty()) {
            val number = proposal.arguments[0]
            _uiState.update { it.copy(pendingProposal = null) }
            if (appContext != null) {
                val callRes = ContactsHelper.initiateCall(appContext, number)
                if (callRes.isSuccess) {
                    insertAssistantNotice("📞 Initiating call to $number...")
                } else {
                    insertAssistantNotice("⚠️ Call failed: ${callRes.exceptionOrNull()?.localizedMessage}")
                }
            }
            return
        }

        // 2. Standard Server Subprocess Execution
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

    fun toggleWakeWord(enabled: Boolean) {
        _uiState.update { it.copy(wakeWordEnabled = enabled) }
        if (appContext != null) {
            if (enabled) {
                WakeWordService.start(appContext)
            } else {
                WakeWordService.stop(appContext)
            }
        }
    }

    fun toggleSkill(id: String, enabled: Boolean) {
        _uiState.update { current ->
            current.copy(
                skills = current.skills.map { if (it.id == id) it.copy(isEnabled = enabled) else it }
            )
        }
    }

    private fun insertAssistantNotice(text: String) {
        viewModelScope.launch {
            repository.insertAssistantMessage(
                conversationId = activeConversationId,
                content = text
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
