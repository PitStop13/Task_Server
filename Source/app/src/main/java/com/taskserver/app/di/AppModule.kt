package com.taskserver.app.di

import android.content.Context
import androidx.room.Room
import com.taskserver.app.data.db.AppDatabase
import com.taskserver.app.data.db.LogDao
import com.taskserver.app.data.db.ServerDao
import com.taskserver.app.data.db.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "taskserver.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideServerDao(db: AppDatabase): ServerDao = db.serverDao()

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideLogDao(db: AppDatabase): LogDao = db.logDao()
}
