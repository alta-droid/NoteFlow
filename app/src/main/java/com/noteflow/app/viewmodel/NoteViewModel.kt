package com.noteflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteflow.app.data.Note
import com.noteflow.app.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()

    val notes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allNotes
            else repository.searchNotes(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectNote(note: Note?) {
        _selectedNote.value = note
    }

    fun insertNote(note: Note, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertNote(note)
            onResult(id)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch { repository.updateNote(note) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    suspend fun getNoteById(id: Long): Note? = repository.getNoteById(id)

    private val _essenceResult = MutableStateFlow<com.noteflow.app.repository.EssenceResult?>(null)
    val essenceResult: StateFlow<com.noteflow.app.repository.EssenceResult?> = _essenceResult.asStateFlow()

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting.asStateFlow()

    fun extractEssence(noteContent: String) {
        viewModelScope.launch {
            _isExtracting.value = true
            _essenceResult.value = repository.extractEssence(noteContent)
            _isExtracting.value = false
        }
    }
    
    fun clearEssence() {
        _essenceResult.value = null
    }
}
