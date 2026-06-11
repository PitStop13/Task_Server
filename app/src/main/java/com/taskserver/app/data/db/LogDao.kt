package com.taskserver.app.data.db

import androidx.room.*
import com.taskserver.app.data.model.ExecutionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    @Query("SELECT * FROM execution_logs ORDER BY startedAt DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<ExecutionLog>>

    @Query("SELECT * FROM execution_logs WHERE taskId = :taskId ORDER BY startedAt DESC LIMIT 20")
    fun getLogsForTask(taskId: Long): Flow<List<ExecutionLog>>

    @Query("SELECT * FROM execution_logs WHERE id = :id")
    suspend fun getLogById(id: Long): ExecutionLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ExecutionLog): Long

    @Update
    suspend fun updateLog(log: ExecutionLog)

    @Query("DELETE FROM execution_logs WHERE startedAt < :olderThan")
    suspend fun deleteOldLogs(olderThan: Long)

    @Query("DELETE FROM execution_logs")
    suspend fun clearAllLogs()
}
