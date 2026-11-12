package com.example.openvoctra

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey val id: Long,
    val title: String,
    val content: String
)