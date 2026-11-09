package com.taskserver.app.data.repository

import com.taskserver.app.data.db.FolderDao
import com.taskserver.app.data.db.ServerDao
import com.taskserver.app.data.db.TaskDao
import com.taskserver.app.data.model.Folder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepository @Inject constructor(
    private val folderDao: FolderDao,
    private val serverDao: ServerDao,
    private val taskDao: TaskDao
) {
    fun getServerFolders(): Flow<List<Folder>> = folderDao.getFoldersByType("SERVER")
    fun getTaskFolders(): Flow<List<Folder>> = folderDao.getFoldersByType("TASK")

    suspend fun createFolder(name: String, type: String): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return -1
        return folderDao.insertFolder(Folder(name = trimmed, type = type))
    }

    suspend fun deleteServerFolder(folder: Folder) {
        folderDao.deleteFolder(folder)
        serverDao.clearFolder(folder.name)
    }

    suspend fun deleteTaskFolder(folder: Folder) {
        folderDao.deleteFolder(folder)
        taskDao.clearFolder(folder.name)
    }

    suspend fun renameServerFolder(folder: Folder, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || trimmed == folder.name) return
        folderDao.updateFolder(folder.copy(name = trimmed))
        serverDao.updateFolderName(folder.name, trimmed)
    }

    suspend fun renameTaskFolder(folder: Folder, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || trimmed == folder.name) return
        folderDao.updateFolder(folder.copy(name = trimmed))
        taskDao.updateFolderName(folder.name, trimmed)
    }
}
