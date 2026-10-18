package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_commands")
data class AuditCommandEntity(
    @PrimaryKey
    val id: String,
    val documentId: String,
    val type: String,
    val description: String,
    val timestamp: Long,
    val targetPageIndex: Int?,
    val isUndone: Boolean = false
)
