package com.taskserver.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.taskserver.app.data.db.AppDatabase
import com.taskserver.app.data.db.FolderDao
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

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE servers ADD COLUMN folder TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE tasks ADD COLUMN folder TEXT DEFAULT NULL")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS folders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_folders_name_type ON folders(name, type)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "taskserver.db"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideServerDao(db: AppDatabase): ServerDao = db.serverDao()

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideLogDao(db: AppDatabase): LogDao = db.logDao()

    @Provides
    fun provideFolderDao(db: AppDatabase): FolderDao = db.folderDao()
}
