package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RecentDocumentEntity::class, AuditCommandEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PdfCraftDatabase : RoomDatabase() {
    abstract fun pdfCraftDao(): PdfCraftDao

    companion object {
        @Volatile
        private var INSTANCE: PdfCraftDatabase? = null

        fun getDatabase(context: Context): PdfCraftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PdfCraftDatabase::class.java,
                    "pdfcraft_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
