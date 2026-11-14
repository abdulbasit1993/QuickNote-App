package com.abmapps.quicknote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abmapps.quicknote.model.Note
import com.abmapps.quicknote.model.NoteColors
import kotlinx.coroutines.flow.*
import java.util.UUID

class NoteViewModel : ViewModel() {
    // Seed with sample data so the staggered grid looks rich immediately
    private val _notes = MutableStateFlow(
        listOf(
            Note(
                id = "1",
                title = "Architecture Checklist",
                content = "- Unidirectional Data Flow\n- Immutable StateFlow\n- Separate UI from business logic",
                colorValue = NoteColors[5],
                isPinned = true
            ),
            Note(
                id = "2",
                title = "Grocery List",
                content = "Coffee beans, oat milk, sourdough bread, avocados",
                colorValue = NoteColors[2]
            ),
            Note(
                id = "3",
                title = "Compose Modifiers",
                content = "Remember: Modifier order matters! Padding before background acts like margin; padding after background acts like internal padding.",
                colorValue = NoteColors[7]
            )
        )
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Reactive derived state: filters by query and sorts pinned notes first
    val filteredNotes: StateFlow<List<Note>> = combine(_notes, _searchQuery) { notes, query ->
        var matching = if (query.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }
        matching.sortedWith(
            compareByDescending<Note> { it.isPinned }
                .thenByDescending { it.updatedAt }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _notes.value
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun getNoteById(id: String): Note? {
        return _notes.value.find { it.id == id }
    }

    fun saveNote(id: String?, title: String, content: String, colorValue: Long, isPinned: Boolean) {
        if (title.isBlank() && content.isBlank()) return

        _notes.update { currentList ->
            if (id == null) {
                // Create new note
                val newNote = Note(
                    id = UUID.randomUUID().toString(),
                    title = title.trim(),
                    content = content.trim(),
                    colorValue = colorValue,
                    isPinned = isPinned
                )
                listOf(newNote) + currentList
            } else {
                // Update existing note
                currentList.map { note ->
                    if (note.id == id) {
                        note.copy(
                            title = title.trim(),
                            content = content.trim(),
                            colorValue = colorValue,
                            isPinned = isPinned,
                            updatedAt = System.currentTimeMillis()
                        )
                    } else note
                }
            }
        }
    }

    fun togglePin(id: String) {
        _notes.update { currentList ->
            currentList.map { note ->
                if (note.id == id) note.copy(isPinned = !note.isPinned) else note
            }
        }
    }

    fun deleteNote(id: String) {
        _notes.update { currentList ->
            currentList.filterNot { it.id == id }
        }
    }
}