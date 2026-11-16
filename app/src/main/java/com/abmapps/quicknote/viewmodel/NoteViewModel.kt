package com.abmapps.quicknote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abmapps.quicknote.data.NoteDatabase
import com.abmapps.quicknote.model.Note
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val noteDao = NoteDatabase.getDatabase(application).noteDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Reactive stream from Room Database combined with search query
    val filteredNotes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                noteDao.getAllNotes()
            } else {
                noteDao.searchNotes(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    suspend fun getNoteById(id: String): Note? {
        return noteDao.getNoteById(id)
    }

    fun saveNote(id: String?, title: String, content: String, colorValue: Long, isPinned: Boolean) {
        if (title.isBlank() && content.isBlank()) return

        viewModelScope.launch {
            val noteId = id ?: UUID.randomUUID().toString()
            val note = Note(
                id = noteId,
                title = title.trim(),
                content = content.trim(),
                colorValue = colorValue,
                isPinned = isPinned,
                updatedAt = System.currentTimeMillis()
            )
            noteDao.upsertNote(note)
        }
    }

    fun togglePin(id: String, currentPinState: Boolean) {
        viewModelScope.launch {
            noteDao.updatePinStatus(id, !currentPinState)
        }
    }

    fun deleteNote(id: String) {
       viewModelScope.launch {
           noteDao.deleteNoteById(id)
       }
    }
}