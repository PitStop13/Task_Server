package com.taskserver.app.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.taskserver.app.data.model.AuthType
import com.taskserver.app.data.model.LogStatus
import com.taskserver.app.data.model.TaskCommand

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromAuthType(value: AuthType): String = value.name

    @TypeConverter
    fun toAuthType(value: String): AuthType = AuthType.valueOf(value)

    @TypeConverter
    fun fromLogStatus(value: LogStatus): String = value.name

    @TypeConverter
    fun toLogStatus(value: String): LogStatus = LogStatus.valueOf(value)

    @TypeConverter
    fun fromCommandList(commands: List<TaskCommand>): String =
        gson.toJson(commands)

    @TypeConverter
    fun toCommandList(json: String): List<TaskCommand> {
        val type = object : TypeToken<List<TaskCommand>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
