package com.taskserver.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconName: String = "terminal",   // nome icona Material
    val commandsJson: String = "[]",     // JSON serializzato di List<TaskCommand>
    val pinnedServerId: Long? = null,    // se null, chiede il server all'esecuzione
    val createdAt: Long = System.currentTimeMillis()
)
