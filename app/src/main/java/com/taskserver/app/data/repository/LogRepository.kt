package com.taskserver.app.data.repository

import com.taskserver.app.data.db.LogDao
import com.taskserver.app.data.model.ExecutionLog
import com.taskserver.app.data.model.LogStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(
    private val dao: LogDao
) {
    fun getRecentLogs(): Flow<List<ExecutionLog>> = dao.getRecentLogs()

    fun getLogsForTask(taskId: Long): Flow<List<ExecutionLog>> =
        dao.getLogsForTask(taskId)

    suspend fun getLogById(id: Long): ExecutionLog? = dao.getLogById(id)

    suspend fun startLog(log: ExecutionLog): Long = dao.insertLog(log)

    suspend fun updateLog(log: ExecutionLog) = dao.updateLog(log)

    suspend fun completeLog(
        logId: Long,
        output: String,
        exitCode: Int,
        status: LogStatus
    ) {
        val existing = dao.getLogById(logId) ?: return
        dao.updateLog(
            existing.copy(
                output = output,
                exitCode = exitCode,
                status = status,
                finishedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearAllLogs() = dao.clearAllLogs()
}
