package com.noteflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.noteflow.app.BuildConfig
import com.noteflow.app.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    data class ChatMessage(val text: String, val isUser: Boolean)

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("مرحباً! أنا المساعد الذكي. اسألني أي شيء عن ملاحظاتك وسأجيبك بناءً عليها فقط ✨", false)
    ))
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(query: String) {
        if (query.isBlank()) return

        val userMsg = ChatMessage(query, true)
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val notes = repository.allNotes.first()
                val notesContext = notes.joinToString("\n---\n") { 
                    "Title: ${it.title}\nContent: ${it.content}\nTags: ${it.tags.joinToString()}"
                }

                val prompt = """
                    You are NoteFlow AI, a helpful assistant. Answer the user's question based ONLY on the provided notes below.
                    Answer in Arabic since the user is speaking Arabic. Use Markdown for formatting.
                    If the answer is not in the notes, politely say "لم أجد معلومات متعلقة بهذا السؤال في ملاحظاتك."
                    
                    Notes Context:
                    $notesContext
                    
                    User's Question:
                    $query
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val aiMsg = ChatMessage(response.text ?: "عذراً، لم أتمكن من الإجابة.", false)
                _messages.value = _messages.value + aiMsg
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("حدث خطأ: ${e.message}", false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
