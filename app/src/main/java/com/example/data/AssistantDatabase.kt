package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ChatMessage::class, AssistantNote::class],
    version = 1,
    exportSchema = false
)
abstract class AssistantDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun assistantNoteDao(): AssistantNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AssistantDatabase? = null

        fun getDatabase(context: Context): AssistantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AssistantDatabase::class.java,
                    "tg_assistant_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
