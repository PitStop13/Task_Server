package com.taskserver.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "folders",
    indices = [Index(value = ["name", "type"], unique = true)]
)
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "SERVER" or "TASK"
    val createdAt: Long = System.currentTimeMillis()
)
