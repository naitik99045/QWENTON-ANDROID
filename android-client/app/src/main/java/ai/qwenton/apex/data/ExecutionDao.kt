package ai.qwenton.apex.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExecutionDao {

    @Query("SELECT * FROM executions ORDER BY timestamp DESC")
    fun getAllExecutions(): Flow<List<ExecutionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecution(execution: ExecutionEntity)

    @Query("SELECT * FROM executions WHERE commandId = :cmdId LIMIT 1")
    suspend fun getExecutionById(cmdId: String): ExecutionEntity?

    @Query("DELETE FROM executions WHERE commandId = :cmdId")
    suspend fun deleteExecution(cmdId: String)

    @Query("DELETE FROM executions")
    suspend fun clearAllExecutions()
}
