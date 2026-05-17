package com.noteflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.noteflow.app.repository.NoteRepository
import com.noteflow.app.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvisorViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _advisorContent = MutableStateFlow<String?>(null)
    val advisorContent: StateFlow<String?> = _advisorContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun generateAdvice() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _advisorContent.value = null

            try {
                val apiKey = settingsRepository.apiKeyFlow.first()
                if (apiKey.isBlank()) {
                    _errorMessage.value = "يرجى إضافة مفتاح API الخاص بك في الإعدادات أولاً للاستفادة من هذه الميزة."
                    _isLoading.value = false
                    return@launch
                }

                val notes = repository.allNotes.first()
                if (notes.isEmpty()) {
                    _errorMessage.value = "ليس لديك أي ملاحظات بعد. اكتب بعض الملاحظات لكي أتمكن من إعطائك نصائح وخطاطات!"
                    _isLoading.value = false
                    return@launch
                }

                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )

                val notesContext = notes.joinToString("\n---\n") {
                    "العنوان: ${it.title}\nالمحتوى: ${it.content}"
                }

                val prompt = """
                    أنت مستشار ذكي وتقوم بتحليل الملاحظات التالية للمستخدم.
                    قم باستخراج "خطاطة للمهام والأفكار" (Mind Map) واضحة ومنظمة، بالإضافة إلى تقديم "نصائح وحلول عملية" لتحسين إنتاجية المستخدم أو حل مشاكله المذكورة في الملاحظات.
                    اكتب الرد بصيغة Markdown منسقة واحترافية باللغة العربية، واستخدم العناوين الكبيرة والقوائم (Bullets).
                    
                    ملاحظات المستخدم:
                    $notesContext
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                _advisorContent.value = response.text ?: "عذراً، تعذر توليد النصيحة في الوقت الحالي."
            } catch (e: Exception) {
                _errorMessage.value = "حدث خطأ أثناء التواصل مع الذكاء الاصطناعي: يرجى التأكد من اتصالك بالإنترنت ومن صحة مفتاح API."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
