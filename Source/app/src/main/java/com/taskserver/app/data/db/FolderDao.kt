package com.taskserver.app.data.db

import androidx.room.*
import com.taskserver.app.data.model.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders WHERE type = :type ORDER BY name ASC")
    fun getFoldersByType(type: String): Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: Folder): Long

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolderById(id: Long)
}
