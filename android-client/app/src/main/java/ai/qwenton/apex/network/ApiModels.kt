package ai.qwenton.apex.network

import com.google.gson.annotations.SerializedName

data class HealthResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("service") val service: String,
    @SerializedName("version") val version: String,
    @SerializedName("server_time") val serverTime: String
)

data class ChatRequestDto(
    @SerializedName("request_id") val requestId: String,
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("message") val message: String,
    @SerializedName("client_version") val clientVersion: String = "0.2.0",
    @SerializedName("context_path") val contextPath: String? = null
)

data class ChatResponseDto(
    @SerializedName("type") val type: String,
    @SerializedName("request_id") val requestId: String,
    @SerializedName("text") val text: String? = null,
    @SerializedName("tool") val tool: String? = null,
    @SerializedName("arguments") val arguments: Map<String, Any>? = null,
    @SerializedName("risk") val risk: String? = null,
    @SerializedName("requires_approval") val requiresApproval: Boolean? = false,
    @SerializedName("explanation") val explanation: String? = null,
    @SerializedName("command_id") val commandId: String? = null,
    @SerializedName("program") val program: String? = null,
    @SerializedName("working_directory") val workingDirectory: String? = null
)

data class CommandApprovalRequestDto(
    @SerializedName("approval_id") val approvalId: String,
    @SerializedName("approved") val approved: Boolean,
    @SerializedName("approved_by") val approvedBy: String = "user"
)

data class CommandApprovalResponseDto(
    @SerializedName("command_id") val commandId: String,
    @SerializedName("approved") val approved: Boolean,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

// In-App AI Provider & Key Vault Models
data class AiConfigRequestDto(
    @SerializedName("provider") val provider: String,
    @SerializedName("api_key") val apiKey: String,
    @SerializedName("model") val model: String,
    @SerializedName("base_url") val baseUrl: String = ""
)

data class AiConfigResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("provider") val provider: String,
    @SerializedName("model") val model: String,
    @SerializedName("base_url") val baseUrl: String? = null,
    @SerializedName("api_key_masked") val apiKeyMasked: String? = null
)
