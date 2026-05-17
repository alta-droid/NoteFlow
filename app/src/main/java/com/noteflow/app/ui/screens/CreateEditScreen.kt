package com.noteflow.app.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noteflow.app.data.Note
import com.noteflow.app.notification.ReminderScheduler
import com.noteflow.app.ui.theme.NoteColors
import com.noteflow.app.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditScreen(
    viewModel: NoteViewModel,
    noteId: Long? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(0) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    var isPinned by remember { mutableStateOf(false) }
    var existingNote by remember { mutableStateOf<Note?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Load existing note if editing
    LaunchedEffect(noteId) {
        if (noteId != null && noteId != -1L) {
            val note = viewModel.getNoteById(noteId)
            note?.let {
                title = it.title
                content = it.content
                selectedColorIndex = it.colorIndex
                reminderTime = it.reminderTime
                isPinned = it.isPinned
                existingNote = it
            }
        }
    }

    val accentColor = NoteColors[selectedColorIndex % NoteColors.size]
    val isEditing = existingNote != null

    fun pickDateTime() {
        val cal = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            TimePickerDialog(context, { _, h, min ->
                val picked = Calendar.getInstance().apply {
                    set(y, m, d, h, min, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                reminderTime = picked.timeInMillis
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun save() {
        if (title.isBlank() && content.isBlank()) return
        isSaving = true
        scope.launch {
            val now = System.currentTimeMillis()
            if (isEditing) {
                val updated = existingNote!!.copy(
                    title = title, content = content, colorIndex = selectedColorIndex,
                    reminderTime = reminderTime, isPinned = isPinned, modifiedAt = now
                )
                viewModel.updateNote(updated)
                reminderTime?.let { ReminderScheduler.scheduleReminder(context, updated.id, title, content, it) }
                    ?: ReminderScheduler.cancelReminder(context, updated.id)
            } else {
                val note = Note(title = title, content = content, colorIndex = selectedColorIndex,
                    reminderTime = reminderTime, isPinned = isPinned)
                viewModel.insertNote(note) { newId ->
                    reminderTime?.let { ReminderScheduler.scheduleReminder(context, newId, title, content, it) }
                }
            }
            isSaving = false
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D0D1A), Color(0xFF12122A))))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 50.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "رجوع", tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (isEditing) "تعديل الملاحظة" else "ملاحظة جديدة",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                // Pin toggle
                IconButton(onClick = { isPinned = !isPinned }) {
                    Icon(
                        if (isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                        "تثبيت",
                        tint = if (isPinned) accentColor else Color.White.copy(alpha = 0.4f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("عنوان الملاحظة", color = Color.White.copy(alpha = 0.35f), fontSize = 22.sp) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = accentColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(listOf(accentColor.copy(0.8f), Color.Transparent))
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Content field
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 200.dp),
                    placeholder = { Text("اكتب ملاحظتك هنا...", color = Color.White.copy(alpha = 0.3f)) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White.copy(alpha = 0.9f), lineHeight = 26.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = accentColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Color picker
                Text("اختر اللون", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NoteColors.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColorIndex == index) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Reminder section
                Text("التذكير", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.07f))
                        .clickable { pickDateTime() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Alarm, null, tint = accentColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = reminderTime?.let { formatDateTime(it) } ?: "اضغط لضبط تذكير",
                        color = if (reminderTime != null) Color.White else Color.White.copy(0.4f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (reminderTime != null) {
                        IconButton(
                            onClick = { reminderTime = null },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White.copy(0.5f), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Save button
        Button(
            onClick = { save() },
            enabled = !isSaving && (title.isNotBlank() || content.isNotBlank()),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Save, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ الملاحظة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

private fun formatDateTime(millis: Long): String {
    val sdf = SimpleDateFormat("EEE، dd MMM yyyy — HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
