package com.noteflow.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noteflow.app.data.Note
import com.noteflow.app.ui.theme.NoteBackgrounds
import com.noteflow.app.ui.theme.NoteColors
import com.noteflow.app.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NoteDetailScreen(
    viewModel: NoteViewModel,
    noteId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    var note by remember { mutableStateOf<Note?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        note = viewModel.getNoteById(noteId)
    }

    val essenceResult by viewModel.essenceResult.collectAsStateWithLifecycle()
    val isExtracting by viewModel.isExtracting.collectAsStateWithLifecycle()
    
    DisposableEffect(noteId) {
        onDispose { viewModel.clearEssence() }
    }

    val accentColor = note?.let { NoteColors[it.colorIndex % NoteColors.size] } ?: NoteColors[0]
    val bgColor = note?.let { NoteBackgrounds[it.colorIndex % NoteBackgrounds.size] } ?: NoteBackgrounds[0]

    AnimatedVisibility(
        visible = note != null,
        enter = fadeIn(tween(300)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    ) {
        note?.let { n ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(bgColor, Color(0xFF0D0D1A), Color(0xFF0D0D1A)))
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 50.dp, bottom = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowBack, "رجوع", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (n.isPinned) {
                            Icon(Icons.Default.PushPin, "مثبتة", tint = accentColor, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                        IconButton(onClick = { onEdit(n.id) }) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Edit, "تعديل", tint = accentColor, modifier = Modifier.size(20.dp))
                            }
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF6B6B).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Delete, "حذف", tint = Color(0xFFFF6B6B), modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // Accent line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                Brush.horizontalGradient(listOf(accentColor, Color.Transparent))
                            )
                    )

                    // Scrollable content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                    ) {
                        // Title
                        Text(
                            text = n.title.ifBlank { "بدون عنوان" },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 36.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Metadata row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, null, tint = accentColor.copy(0.8f), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatFull(n.modifiedAt),
                                    fontSize = 12.sp,
                                    color = accentColor.copy(0.8f)
                                )
                            }
                            if (n.reminderTime != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Alarm, null, tint = Color(0xFFFFD600).copy(0.8f), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formatFull(n.reminderTime),
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFD600).copy(0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Content
                        com.noteflow.app.ui.components.MarkdownText(
                            text = n.content,
                            fontSize = 17.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // AI Essence Section
                        if (essenceResult != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E35).copy(alpha = 0.5f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("✨ الخلاصة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accentColor)
                                    Spacer(Modifier.height(8.dp))
                                    Text(essenceResult!!.summary ?: "", color = Color.White.copy(alpha = 0.9f))
                                    
                                    if (!essenceResult!!.actionItems.isNullOrEmpty()) {
                                        Spacer(Modifier.height(12.dp))
                                        Text("📝 القراطس (المهام):", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = accentColor)
                                        Spacer(Modifier.height(4.dp))
                                        essenceResult!!.actionItems!!.forEach { task ->
                                            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircleOutline, null, tint = accentColor, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(task, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.extractEssence(n.content) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E35)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isExtracting) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = accentColor, strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("جاري استخراج الخلاصة...", color = Color.White)
                                } else {
                                    Icon(Icons.Default.AutoAwesome, null, tint = accentColor, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("استخراج الخلاصة ✨", color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }

                // Delete confirm dialog
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("حذف الملاحظة", color = Color.White) },
                        text = { Text("هل أنت متأكد من حذف هذه الملاحظة؟", color = Color.White.copy(0.7f)) },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.deleteNote(n)
                                showDeleteDialog = false
                                onBack()
                            }) {
                                Text("حذف", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("إلغاء", color = Color.White.copy(0.6f))
                            }
                        },
                        containerColor = Color(0xFF1E1E35),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatFull(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy، HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
