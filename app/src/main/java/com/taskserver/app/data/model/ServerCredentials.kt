package com.taskserver.app.data.model

data class ServerCredentials(
    val sshPassword: String? = null,
    val privateKey: String? = null,
    val sudoPassword: String? = null
)
