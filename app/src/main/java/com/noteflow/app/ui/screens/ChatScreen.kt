package com.noteflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
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
import com.noteflow.app.ui.components.MarkdownText
import com.noteflow.app.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1E1E35), Color(0xFF0D0D1A))))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
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
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "اسأل ملاحظاتك ✨",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Chat History
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                if (isLoading) {
                    item {
                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
                            CircularProgressIndicator(color = Color(0xFF00FFB2), modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("يفكر...", color = Color.White.copy(alpha=0.6f))
                        }
                    }
                }
                items(messages.reversed()) { msg ->
                    ChatBubble(msg = msg)
                }
            }

            // Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("ابحث في ملاحظاتك...", color = Color.White.copy(0.4f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A4A),
                        unfocusedContainerColor = Color(0xFF2A2A4A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (input.isNotBlank() && !isLoading) {
                            viewModel.sendMessage(input)
                            input = ""
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (input.isNotBlank()) Color(0xFF00FFB2) else Color(0xFF444466))
                ) {
                    Icon(Icons.Default.Send, "إرسال", tint = Color(0xFF0D0D1A))
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatViewModel.ChatMessage) {
    val alignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (msg.isUser) Color(0xFF00FFB2).copy(alpha = 0.2f) else Color(0xFF2A2A4A)
    val textColor = if (msg.isUser) Color(0xFF00FFB2) else Color.White
    val shape = if (msg.isUser) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp) 
                else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(bgColor)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            MarkdownText(text = msg.text, color = textColor, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}
