package com.noteflow.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    lineHeight: TextUnit = TextUnit.Unspecified
) {
    val annotatedString = buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Bold: **text**
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Header 1: # Header
                text.startsWith("# ", i) && (i == 0 || text[i - 1] == '\n') -> {
                    val end = text.indexOf('\n', i)
                    val realEnd = if (end != -1) end else text.length
                    withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = MaterialTheme.typography.titleLarge.fontSize)) {
                        append(text.substring(i + 2, realEnd))
                    }
                    if (end != -1) append("\n")
                    i = realEnd + (if (end != -1) 1 else 0)
                }
                // Bullet List: - item
                text.startsWith("- ", i) && (i == 0 || text[i - 1] == '\n') -> {
                    val end = text.indexOf('\n', i)
                    val realEnd = if (end != -1) end else text.length
                    append("• ")
                    append(text.substring(i + 2, realEnd))
                    if (end != -1) append("\n")
                    i = realEnd + (if (end != -1) 1 else 0)
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = overflow,
        lineHeight = lineHeight
    )
}
