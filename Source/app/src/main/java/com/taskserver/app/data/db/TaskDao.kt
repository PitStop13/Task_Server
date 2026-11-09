package com.taskserver.app.data.db

import androidx.room.*
import com.taskserver.app.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE tasks SET folder = NULL WHERE folder = :folderName")
    suspend fun clearFolder(folderName: String)

    @Query("UPDATE tasks SET folder = :newFolderName WHERE folder = :oldFolderName")
    suspend fun updateFolderName(oldFolderName: String, newFolderName: String)

    @Query("UPDATE tasks SET folder = :folderName WHERE id = :taskId")
    suspend fun updateTaskFolder(taskId: Long, folderName: String?)
}
