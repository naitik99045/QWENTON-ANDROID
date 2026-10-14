package ai.qwenton.apex.domain

import ai.qwenton.apex.data.ConversationDao
import ai.qwenton.apex.data.ExecutionDao
import ai.qwenton.apex.data.ExecutionEntity
import ai.qwenton.apex.data.MessageEntity
import ai.qwenton.apex.network.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QwentonRepository(
    private val conversationDao: ConversationDao,
    private val executionDao: ExecutionDao,
    private var baseUrl: String = "http://127.0.0.1:8765"
) {
    private val gson = Gson()
    private var api: QwentonApi = NetworkModule.getApi(baseUrl)

    fun updateBaseUrl(newUrl: String) {
        baseUrl = newUrl
        api = NetworkModule.getApi(baseUrl)
    }

    suspend fun checkHealth(): Result<HealthResponseDto> {
        return try {
            val response = api.checkHealth()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendChat(
        requestId: String,
        conversationId: String,
        message: String,
        contextPath: String? = null
    ): Result<ChatResponseDto> {
        return try {
            // Persist user message to Room
            conversationDao.insertMessage(
                MessageEntity(
                    id = "msg_${System.currentTimeMillis()}",
                    conversationId = conversationId,
                    role = "USER",
                    content = message,
                    timestamp = System.currentTimeMillis(),
                    requestId = requestId
                )
            )

            val response = api.sendChat(
                ChatRequestDto(
                    requestId = requestId,
                    conversationId = conversationId,
                    message = message,
                    contextPath = contextPath
                )
            )

            // If normal assistant response, persist it
            if (response.type == "assistant_message" && response.text != null) {
                conversationDao.insertMessage(
                    MessageEntity(
                        id = "msg_${System.currentTimeMillis()}",
                        conversationId = conversationId,
                        role = "ASSISTANT",
                        content = response.text,
                        timestamp = System.currentTimeMillis(),
                        requestId = requestId
                    )
                )
            }

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveCommand(
        commandId: String,
        approvalId: String
    ): Result<CommandApprovalResponseDto> {
        return try {
            val response = api.approveCommand(
                commandId,
                CommandApprovalRequestDto(
                    approvalId = approvalId,
                    approved = true
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelCommand(commandId: String): Result<CommandApprovalResponseDto> {
        return try {
            val response = api.cancelCommand(commandId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAiProvider(
        provider: String,
        apiKey: String,
        model: String,
        baseUrl: String = ""
    ): Result<AiConfigResponseDto> {
        return try {
            val response = api.updateAiConfig(
                AiConfigRequestDto(
                    provider = provider,
                    apiKey = apiKey,
                    model = model,
                    baseUrl = baseUrl
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAiConfig(): Result<AiConfigResponseDto> {
        return try {
            val response = api.getAiConfig()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessagesFlow(): Flow<List<ChatMessage>> {
        return conversationDao.getAllMessages().map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    conversationId = entity.conversationId,
                    role = when (entity.role.uppercase()) {
                        "USER" -> MessageRole.USER
                        "ASSISTANT" -> MessageRole.ASSISTANT
                        "TOOL" -> MessageRole.TOOL
                        else -> MessageRole.SYSTEM
                    },
                    content = entity.content,
                    timestamp = entity.timestamp
                )
            }
        }
    }

    suspend fun clearHistory() {
        conversationDao.clearAllMessages()
        executionDao.clearAllExecutions()
    }

    suspend fun insertAssistantMessage(conversationId: String, content: String) {
        conversationDao.insertMessage(
            MessageEntity(
                id = "msg_${System.currentTimeMillis()}",
                conversationId = conversationId,
                role = "ASSISTANT",
                content = content,
                timestamp = System.currentTimeMillis(),
                requestId = "client_${System.currentTimeMillis()}"
            )
        )
    }

    suspend fun deleteMessage(id: String) {
        conversationDao.deleteMessageById(id)
    }
}
