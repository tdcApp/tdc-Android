package com.bagadbille.tdc.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class QuestionSegment {
    data class Text(val content: String) : QuestionSegment()
    data class CodeBlock(val code: String, val tag: String) : QuestionSegment()
}

/**
 * Splits raw question text into regular text segments and monospace code/algorithm blocks.
 * Supports:
 * 1. Markdown code fences: ``` [tag] \n code \n ```
 * 2. Properly handles literal `\n`, `\r\n`, and `\t` from JSON payloads.
 * 3. Falls back cleanly to 100% normal text when no code fences are present.
 */
fun parseQuestionSegments(rawText: String): List<QuestionSegment> {
    if (rawText.isBlank()) return emptyList()

    // Normalize escaped newlines and tabs from JSON/network transmission
    val normalized = rawText
        .replace("\\r\\n", "\n")
        .replace("\\n", "\n")
        .replace("\\t", "    ")
        .replace("\r\n", "\n")

    val segments = mutableListOf<QuestionSegment>()
    val codeBlockRegex = Regex("```([a-zA-Z0-9_+#.-]*)\\n?([\\s\\S]*?)```")

    var lastIndex = 0
    val matches = codeBlockRegex.findAll(normalized).toList()

    if (matches.isEmpty()) {
        return listOf(QuestionSegment.Text(normalized.trim()))
    }

    for (match in matches) {
        val start = match.range.first
        val end = match.range.last + 1

        if (start > lastIndex) {
            val textBefore = normalized.substring(lastIndex, start).trim()
            if (textBefore.isNotEmpty()) {
                segments.add(QuestionSegment.Text(textBefore))
            }
        }

        val rawTag = match.groupValues[1].trim()
        val codeContent = match.groupValues[2].trimEnd()
        val displayTag = if (rawTag.isNotBlank()) rawTag.uppercase() else "ALGORITHM / LOGIC"

        segments.add(QuestionSegment.CodeBlock(code = codeContent, tag = displayTag))
        lastIndex = end
    }

    if (lastIndex < normalized.length) {
        val remaining = normalized.substring(lastIndex).trim()
        if (remaining.isNotEmpty()) {
            segments.add(QuestionSegment.Text(remaining))
        }
    }

    return segments
}

/**
 * Smart Question Content Composable:
 * - If the question has NO code: renders regular, clean typography with zero overhead.
 * - If the question has code or algorithms: renders the prompt on top, and embeds the
 *   code in a dedicated, monospace, horizontally scrollable editor card that preserves
 *   exact indentation, spacing, and algorithm structure without wrapping.
 */
@Composable
fun FormattedQuestionContent(
    text: String,
    modifier: Modifier = Modifier,
    promptStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    ),
    promptColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val segments = remember(text) { parseQuestionSegments(text) }

    Column(modifier = modifier.fillMaxWidth()) {
        segments.forEachIndexed { index, segment ->
            when (segment) {
                is QuestionSegment.Text -> {
                    Text(
                        text = segment.content,
                        style = promptStyle,
                        color = promptColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is QuestionSegment.CodeBlock -> {
                    Spacer(Modifier.height(8.dp))
                    AlgorithmCodeCard(
                        code = segment.code,
                        tag = segment.tag
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (index < segments.size - 1 && segment is QuestionSegment.Text) {
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun AlgorithmCodeCard(
    code: String,
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar for the algorithm container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.7.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Horizontally-scrollable fixed-width monospace code area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
