package com.abmapps.quicknote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.abmapps.quicknote.model.Note
import com.abmapps.quicknote.model.NoteColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                    .addCallback(NoteDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class NoteDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.noteDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: NoteDao) {
                val sampleNotes = listOf(
                    Note(
                        id = "1",
                        title = "Architecture Checklist",
                        content = "- Unidirectional Data Flow\n- Immutable StateFlow\n- Separate UI from business logic",
                        colorValue = NoteColors[5],
                        isPinned = true,
                        updatedAt = System.currentTimeMillis()
                    ),
                    Note(
                        id = "2",
                        title = "Grocery List",
                        content = "Coffee beans, oat milk, sourdough bread, avocados",
                        colorValue = NoteColors[2],
                        updatedAt = System.currentTimeMillis()
                    ),
                    Note(
                        id = "3",
                        title = "Compose Modifiers",
                        content = "Remember: Modifier order matters! Padding before background acts like margin; padding after background acts like internal padding.",
                        colorValue = NoteColors[7],
                        updatedAt = System.currentTimeMillis()
                    )
                )
                sampleNotes.forEach { dao.upsertNote(it) }
            }
        }
    }
}
