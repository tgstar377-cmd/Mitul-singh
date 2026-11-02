package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assistant_notes")
data class AssistantNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isReminder: Boolean = false,
    val isCompleted: Boolean = false,
    val category: String = "General"
)
