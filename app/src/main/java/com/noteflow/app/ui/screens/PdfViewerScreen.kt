package com.noteflow.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noteflow.app.viewmodel.PdfViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    viewModel: PdfViewModel,
    uriString: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uri = Uri.parse(uriString)
    
    val pageCount by viewModel.pageCount.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var isNightMode by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(true) } // Locked by default for safe reading
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    val scope = rememberCoroutineScope()
    var extractionProgress by remember { mutableStateOf(false) }

    LaunchedEffect(uri) {
        viewModel.loadPdf(context, uri)
    }

    // Invert matrix for night mode reading
    val invertMatrix = floatArrayOf(
        -1f, 0f, 0f, 0f, 255f,
        0f, -1f, 0f, 0f, 255f,
        0f, 0f, -1f, 0f, 255f,
        0f, 0f, 0f, 1f, 0f
    )
    val colorFilter = if (isNightMode) ColorFilter.colorMatrix(ColorMatrix(invertMatrix)) else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLocked) "وضع القراءة الآمن 📖" else "وضع التعديل والنسخ ✏️", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.closePdf()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { isNightMode = !isNightMode }) {
                        Icon(
                            if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            "تغيير لون الخلفية"
                        )
                    }
                    IconButton(onClick = { isLocked = !isLocked }) {
                        Icon(
                            if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            "القفل",
                            tint = if (isLocked) MaterialTheme.colorScheme.primary else Color(0xFFFF6B6B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(if (isNightMode) Color.Black else Color(0xFFF5F5F7))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (pageCount == 0) {
                Text(
                    "تعذر فتح الملف.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
                    scale = (scale * zoomChange).coerceIn(1f, 5f)
                    offset += offsetChange
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = transformableState),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(pageCount) { index ->
                        PdfPage(
                            viewModel = viewModel,
                            pageIndex = index,
                            colorFilter = colorFilter,
                            isLocked = isLocked,
                            onExtractText = {
                                if (!isLocked) {
                                    scope.launch {
                                        extractionProgress = true
                                        val text = viewModel.extractTextFromPage(context, uri, index)
                                        extractionProgress = false
                                        if (text.isNotBlank()) {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("PDF Text", text)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "تم نسخ النص للصفحة رقم ${index + 1}", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "لم يتم العثور على نص للنسخ (قد تكون الصفحة صورة).", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                // Temporary extraction progress indicator
                if (extractionProgress) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("جاري استخراج ونسخ النص...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                // Instructions if Unlocked
                AnimatedVisibility(
                    visible = !isLocked,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFF6B6B))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCopy, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("وضع التعديل مفعل: اضغط مطولاً على أي صفحة لنسخ النص الخاص بها.", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PdfPage(
    viewModel: PdfViewModel,
    pageIndex: Int,
    colorFilter: ColorFilter?,
    isLocked: Boolean,
    onExtractText: () -> Unit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(size) {
        if (size.width > 0) {
            // Render with higher resolution for better zoom
            bitmap = viewModel.renderPage(pageIndex, size.width * 2)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f) // Initial aspect ratio until rendered
            .onSizeChanged { size = it }
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White) // PDF default background
            .pointerInput(isLocked) {
                detectTapGestures(
                    onLongPress = {
                        if (!isLocked) onExtractText()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1}",
                modifier = Modifier.fillMaxSize(),
                colorFilter = colorFilter
            )
        } else {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
