package com.taskserver.app.data.db

import androidx.room.*
import com.taskserver.app.data.model.Server
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {

    @Query("SELECT * FROM servers ORDER BY createdAt DESC")
    fun getAllServers(): Flow<List<Server>>

    @Query("SELECT * FROM servers WHERE id = :id")
    suspend fun getServerById(id: Long): Server?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: Server): Long

    @Update
    suspend fun updateServer(server: Server)

    @Delete
    suspend fun deleteServer(server: Server)

    @Query("SELECT COUNT(*) FROM servers")
    suspend fun getServerCount(): Int

    @Query("UPDATE servers SET folder = NULL WHERE folder = :folderName")
    suspend fun clearFolder(folderName: String)

    @Query("UPDATE servers SET folder = :newFolderName WHERE folder = :oldFolderName")
    suspend fun updateFolderName(oldFolderName: String, newFolderName: String)

    @Query("UPDATE servers SET folder = :folderName WHERE id = :serverId")
    suspend fun updateServerFolder(serverId: Long, folderName: String?)
}
