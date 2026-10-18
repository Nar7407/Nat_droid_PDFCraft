package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfCraftDao {
    @Query("SELECT * FROM recent_documents ORDER BY lastAccessed DESC")
    fun getAllRecentDocuments(): Flow<List<RecentDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentDocument(document: RecentDocumentEntity)

    @Query("DELETE FROM recent_documents WHERE id = :id")
    suspend fun deleteRecentDocument(id: String)

    @Query("SELECT * FROM audit_commands WHERE documentId = :documentId ORDER BY timestamp ASC")
    fun getAuditCommandsForDocument(documentId: String): Flow<List<AuditCommandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditCommand(command: AuditCommandEntity)

    @Update
    suspend fun updateAuditCommand(command: AuditCommandEntity)

    @Query("DELETE FROM audit_commands WHERE documentId = :documentId")
    suspend fun clearAuditForDocument(documentId: String)
}
