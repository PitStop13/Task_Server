package com.taskserver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskserver.app.ui.theme.*

@Composable
fun TerminalOutput(
    lines: List<String>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    serverHost: String = "localhost",
    serverUsername: String = "root"
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp) // Sposta il componente più in basso come richiesto
            .shadow(16.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(BorderDefault.copy(alpha = 0.5f), BorderDefault.copy(alpha = 0.1f))
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        color = Void, // Sfondo principale scurissimo/nero
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // macOS Style Header - Più sottile e moderno
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer.copy(alpha = 0.95f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Semaforo di controllo (Dot stilizzati)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TerminalDot(Color(0xFFFF5F56))
                    TerminalDot(Color(0xFFFFBD2E))
                    TerminalDot(Color(0xFF27C93F))
                }
                
                Spacer(Modifier.weight(1f))
                
                // Host info con stile tipografico migliorato
                Text(
                    text = "$serverUsername@$serverHost:~",
                    style = TextStyle(
                        fontFamily = MonospaceFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextTertiary.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp
                    )
                )
                
                Spacer(Modifier.weight(1f))
                
                // Placeholder per simmetria o icone future (es. pulsante copia tutto)
                Box(modifier = Modifier.width(42.dp)) 
            }

            // Divider sottile con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, BorderDefault, Color.Transparent)
                        )
                    )
            )

            // Terminal Content
            SelectionContainer {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(
                        items = lines,
                        key = { it.hashCode() + System.nanoTime() } // Chiave per performance e stabilità
                    ) { line ->
                        TerminalLine(line)
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalLine(line: String) {
    val (text, color) = parseAnsiColor(line)
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 1.dp)
    ) {
        Text(
            text = text.ifEmpty { " " },
            style = TextStyle(
                fontFamily = MonospaceFontFamily,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = color,
                letterSpacing = 0.3.sp
            ),
            softWrap = false
        )
    }
}

@Composable
private fun TerminalDot(color: Color) {
    Box(
        modifier = Modifier
            .size(11.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.8f))
            .border(0.5.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
    )
}

@Composable
private fun parseAnsiColor(line: String): Pair<String, Color> {
    // Regex migliorata per catturare tutti i codici di reset e colore ANSI
    val ansiRegex = Regex("\\x1b\\[[0-9;]*m")
    val cleanText = line.replace(ansiRegex, "")
    
    return when {
        line.contains("\u001b[31m") || line.contains("[31m") -> Pair(cleanText, TerminalRed)
        line.contains("\u001b[32m") || line.contains("[32m") -> Pair(cleanText, TerminalGreen)
        line.contains("\u001b[33m") || line.contains("[33m") -> Pair(cleanText, TerminalAmber)
        line.contains("\u001b[34m") || line.contains("[34m") -> Pair(cleanText, CyanPrimary)
        line.contains("\u001b[35m") || line.contains("[35m") -> Pair(cleanText, PurpleBright)
        line.contains("\u001b[36m") || line.contains("[36m") -> Pair(cleanText, CyanBright)
        (line.contains("@") && (line.trimEnd().endsWith("$") || line.trimEnd().endsWith("#"))) -> Pair(cleanText, CyanPrimary)
        line.startsWith("$") || line.startsWith("#") -> Pair(cleanText, CyanPrimary)
        line.contains("error", ignoreCase = true) || line.contains("failed", ignoreCase = true) -> Pair(cleanText, TerminalRed)
        line.contains("warning", ignoreCase = true) -> Pair(cleanText, TerminalAmber)
        line.contains("success", ignoreCase = true) || line.contains("done", ignoreCase = true) -> Pair(cleanText, TerminalGreen)
        else -> Pair(cleanText, TextPrimary)
    }
}
