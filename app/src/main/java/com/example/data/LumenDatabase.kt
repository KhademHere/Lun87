package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HeadshotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LumenDatabase : RoomDatabase() {
    abstract fun headshotDao(): HeadshotDao

    companion object {
        @Volatile
        private var INSTANCE: LumenDatabase? = null

        fun getInstance(context: Context): LumenDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LumenDatabase::class.java,
                    "lumen_studio.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
