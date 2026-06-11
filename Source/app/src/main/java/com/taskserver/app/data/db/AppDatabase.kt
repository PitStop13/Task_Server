package com.taskserver.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.taskserver.app.data.model.ExecutionLog
import com.taskserver.app.data.model.Server
import com.taskserver.app.data.model.Task

@Database(
    entities = [Server::class, Task::class, ExecutionLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun taskDao(): TaskDao
    abstract fun logDao(): LogDao
}
