package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_documents")
data class RecentDocumentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val uriString: String?,
    val sizeBytes: Long,
    val pageCount: Int,
    val lastAccessed: Long,
    val isEncrypted: Boolean
)
