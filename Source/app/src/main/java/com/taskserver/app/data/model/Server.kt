package com.taskserver.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AuthType { PASSWORD, KEY }

enum class ServerStatus { UNKNOWN, ONLINE, OFFLINE, CHECKING }

@Entity(tableName = "servers")
data class Server(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: AuthType = AuthType.PASSWORD,
    // Nota: password SSH, chiave privata e password sudo non vengono salvate in Room.
    // Vengono salvate in EncryptedSharedPreferences con chiavi dedicate al server.
    val createdAt: Long = System.currentTimeMillis()
)
