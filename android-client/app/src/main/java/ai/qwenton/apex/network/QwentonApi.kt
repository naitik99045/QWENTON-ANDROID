package ai.qwenton.apex.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface QwentonApi {

    @GET("health")
    suspend fun checkHealth(): HealthResponseDto

    @POST("chat")
    suspend fun sendChat(
        @Body request: ChatRequestDto
    ): ChatResponseDto

    @POST("commands/{command_id}/approve")
    suspend fun approveCommand(
        @Path("command_id") commandId: String,
        @Body request: CommandApprovalRequestDto
    ): CommandApprovalResponseDto

    @POST("commands/{command_id}/cancel")
    suspend fun cancelCommand(
        @Path("command_id") commandId: String
    ): CommandApprovalResponseDto
}
