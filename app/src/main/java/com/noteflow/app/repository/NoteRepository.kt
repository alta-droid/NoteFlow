package com.noteflow.app.repository

import com.noteflow.app.data.Note
import com.noteflow.app.data.NoteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import com.noteflow.app.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val settingsRepository: SettingsRepository
) {

    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes("%$query%")
    }

    suspend fun getNoteById(id: Long): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun insertNote(note: Note): Long {
        val id = noteDao.insertNote(note)
        enrichAndSave(note.copy(id = id))
        return id
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
        enrichAndSave(note)
    }

    private fun enrichAndSave(note: Note) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val enrichedNote = enrichNoteWithAI(note)
            if (enrichedNote != note) {
                noteDao.updateNote(enrichedNote)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    private suspend fun enrichNoteWithAI(note: Note): Note = withContext(Dispatchers.IO) {
        if (note.content.isBlank()) return@withContext note
        
        val apiKey = settingsRepository.apiKeyFlow.first()
        if (apiKey.isBlank()) return@withContext note

        val generativeModel = com.google.ai.client.generativeai.GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )

        val needsTitle = note.title.isBlank()
        val titleInstruction = if (needsTitle) "\"title\": A catchy, short title (max 5 words)," else ""
        
        val prompt = """
            Analyze the following note. Return ONLY a valid JSON object with these keys:
            $titleInstruction
            "category": A short category name (e.g. Work, Personal, Tech, Idea).
            "tags": A list of up to 5 relevant keyword strings.
            
            Note Content:
            ${note.content}
        """.trimIndent()
        
        val response = generativeModel.generateContent(prompt)
        val text = response.text ?: return@withContext note
        
        val jsonStr = text.substringAfter("{").substringBeforeLast("}")
        val fullJson = "{$jsonStr}"
        
        try {
            val gson = com.google.gson.Gson()
            val result = gson.fromJson(fullJson, AiAnalysisResult::class.java)
            val newTitle = if (needsTitle && !result.title.isNullOrBlank()) result.title else note.title
            note.copy(category = result.category, tags = result.tags ?: emptyList(), title = newTitle)
        } catch (e: Exception) {
            note
        }
    }
    
    suspend fun extractEssence(content: String): EssenceResult = withContext(Dispatchers.IO) {
        if (content.isBlank()) return@withContext EssenceResult(null, null)

        val apiKey = settingsRepository.apiKeyFlow.first()
        if (apiKey.isBlank()) return@withContext EssenceResult("يرجى إدخال مفتاح API في الإعدادات.", null)

        val generativeModel = com.google.ai.client.generativeai.GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
        val prompt = """
            Extract the essence of the following note. Return ONLY a valid JSON object with:
            "summary": "الزبدة" (A concise executive summary in Arabic).
            "actionItems": ["القراطس", "Task 1", "Task 2"] (A list of actionable tasks in Arabic).
            
            Note:
            $content
        """.trimIndent()
        
        try {
            val response = generativeModel.generateContent(prompt)
            val jsonStr = response.text?.substringAfter("{")?.substringBeforeLast("}") ?: return@withContext EssenceResult("تعذر تحليل الملاحظة.", null)
            val fullJson = "{$jsonStr}"
            com.google.gson.Gson().fromJson(fullJson, EssenceResult::class.java) ?: EssenceResult("تعذر تحليل الملاحظة.", null)
        } catch (e: Exception) {
            EssenceResult("حدث خطأ: ${e.message}", null)
        }
    }
}

data class AiAnalysisResult(val title: String?, val category: String?, val tags: List<String>?)
data class EssenceResult(val summary: String?, val actionItems: List<String>?)
