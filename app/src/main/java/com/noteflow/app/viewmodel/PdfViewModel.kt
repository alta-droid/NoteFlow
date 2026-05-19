package com.noteflow.app.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import javax.inject.Inject
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font

@HiltViewModel
class PdfViewModel @Inject constructor() : ViewModel() {

    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private val pdfMutex = Mutex()

    private val _pageCount = MutableStateFlow(0)
    val pageCount: StateFlow<Int> = _pageCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    closePdf()
                    fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                    if (fileDescriptor != null) {
                        pdfRenderer = PdfRenderer(fileDescriptor!!)
                        _pageCount.value = pdfRenderer?.pageCount ?: 0
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _isLoading.value = false
        }
    }

    suspend fun renderPage(pageIndex: Int, width: Int): Bitmap? = withContext(Dispatchers.IO) {
        pdfMutex.withLock {
            try {
                val renderer = pdfRenderer ?: return@withContext null
                if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null

                val page = renderer.openPage(pageIndex)
                try {
                    val aspectRatio = page.height.toFloat() / page.width.toFloat()
                    val height = (width * aspectRatio).toInt()

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    return@withContext bitmap
                } finally {
                    page.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    suspend fun extractTextFromPage(context: Context, uri: Uri, pageIndex: Int): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext ""
            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            stripper.startPage = pageIndex + 1
            stripper.endPage = pageIndex + 1
            val text = stripper.getText(document)
            document.close()
            return@withContext text
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext ""
        }
    }

    fun closePdf() {
        try {
            pdfRenderer?.close()
            fileDescriptor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfRenderer = null
        fileDescriptor = null
        _pageCount.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        closePdf()
    }
}
