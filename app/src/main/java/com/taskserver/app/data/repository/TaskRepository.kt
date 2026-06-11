package com.taskserver.app.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.taskserver.app.data.db.TaskDao
import com.taskserver.app.data.model.Task
import com.taskserver.app.data.model.TaskCommand
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val dao: TaskDao
) {
    private val gson = Gson()

    fun getAllTasks(): Flow<List<Task>> = dao.getAllTasks()

    suspend fun getTaskById(id: Long): Task? = dao.getTaskById(id)

    suspend fun saveTask(task: Task): Long = dao.insertTask(task)

    suspend fun updateTask(task: Task) = dao.updateTask(task)

    suspend fun deleteTask(task: Task) = dao.deleteTask(task)

    fun serializeCommands(commands: List<TaskCommand>): String =
        gson.toJson(commands)

    fun deserializeCommands(json: String): List<TaskCommand> {
        if (json.isBlank() || json == "null") return emptyList()
        val type = object : TypeToken<List<TaskCommand>>() {}.type
        return try {
            gson.fromJson<List<TaskCommand>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
