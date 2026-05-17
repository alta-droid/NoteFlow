package com.noteflow.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noteflow.app.data.Note
import com.noteflow.app.ui.theme.NoteBackgrounds
import com.noteflow.app.ui.theme.NoteColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = NoteColors[note.colorIndex % NoteColors.size]
    val bgColor = NoteBackgrounds[note.colorIndex % NoteBackgrounds.size]

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                pressed = true
                onClick()
            }
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.6f), Color.Transparent)),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Accent line at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(listOf(accentColor, accentColor.copy(alpha = 0.3f)))
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifBlank { "بدون عنوان" },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "مثبتة",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content preview
            if (note.content.isNotBlank()) {
                MarkdownText(
                    text = note.content,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Tags/Category
            if (note.category != null || note.tags.isNotEmpty()) {
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    note.category?.let { cat ->
                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(accentColor.copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(text = cat, fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.Bold)
                        }
                    }
                    note.tags.take(3).forEach { tag ->
                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).border(0.5.dp, accentColor.copy(alpha=0.5f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(text = "#$tag", fontSize = 10.sp, color = Color.White.copy(alpha=0.8f))
                        }
                    }
                }
            }

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(note.modifiedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.8f)
                )
                if (note.reminderTime != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "تذكير",
                            tint = accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = formatTime(note.reminderTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) kotlinx.coroutines.delay(150)
        pressed = false
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
