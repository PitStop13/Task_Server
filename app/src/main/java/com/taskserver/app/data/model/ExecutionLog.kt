package com.taskserver.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LogStatus { RUNNING, SUCCESS, ERROR }

@Entity(tableName = "execution_logs")
data class ExecutionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val taskName: String,
    val serverId: Long,
    val serverName: String,
    val serverHost: String,
    val status: LogStatus = LogStatus.RUNNING,
    val output: String = "",
    val exitCode: Int? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null
)
