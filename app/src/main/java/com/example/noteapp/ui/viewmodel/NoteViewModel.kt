package com.example.noteapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.Note
import com.example.noteapp.data.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    val notes = repository.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertNote(title: String, content: String) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content,
                createdAt = Date(),
                updatedAt = Date()
            )
            repository.insertNote(note)
        }
    }

    fun updateNote(id: Long, title: String, content: String, originalCreatedAt: Date) {
        viewModelScope.launch {
            val updatedNote = Note(
                id = id,
                title = title,
                content = content,
                createdAt = originalCreatedAt, // Сохраняем оригинальную дату создания
                updatedAt = Date() // Обновляем только дату изменения
            )
            repository.updateNote(updatedNote)
        }
    }

    fun deleteNoteById(id: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}