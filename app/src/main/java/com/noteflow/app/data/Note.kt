package com.noteflow.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val colorIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val reminderTime: Long? = null,
    val isPinned: Boolean = false,
    
    // AI Features
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val summary: String? = null,
    val actionItems: List<String> = emptyList(),
    val contextInfo: String? = null
)
