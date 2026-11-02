package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantNoteDao {
    @Query("SELECT * FROM assistant_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<AssistantNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: AssistantNote): Long

    @Update
    suspend fun updateNote(note: AssistantNote)

    @Delete
    suspend fun deleteNote(note: AssistantNote)

    @Query("DELETE FROM assistant_notes")
    suspend fun clearAll()
}
