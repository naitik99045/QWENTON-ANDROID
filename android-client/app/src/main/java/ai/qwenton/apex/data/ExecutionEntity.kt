package ai.qwenton.apex.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "executions")
data class ExecutionEntity(
    @PrimaryKey val commandId: String,
    val requestId: String,
    val tool: String,
    val program: String,
    val argumentsJson: String,
    val workingDirectory: String,
    val risk: String,
    val status: String,
    val exitCode: Int?,
    val stdout: String,
    val stderr: String,
    val timestamp: Long
)
