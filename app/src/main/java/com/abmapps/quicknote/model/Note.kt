package com.abmapps.quicknote.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.ui.graphics.Color

@Entity(tableName = "notes")
data class Note (
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val colorValue: Long,
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

// Preset pastel palette for note cards
val NoteColors = listOf(
    0xFFFFFFFF, // White
    0xFFF28B82, // Coral Red
    0xFFFBBC04, // Warm Yellow
    0xFFFFF475, // Soft Lemon
    0xFFCCFF90, // Mint Green
    0xFFA7FFEB, // Teal
    0xFFCBF0F8, // Sky Blue
    0xFFD7AEFB  // Lavender
)

fun Note.getColor(): Color = Color(colorValue)