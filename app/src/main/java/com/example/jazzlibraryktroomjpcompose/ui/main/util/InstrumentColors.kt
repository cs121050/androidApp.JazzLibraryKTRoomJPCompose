package com.example.jazzlibraryktroomjpcompose.ui.main.util

import androidx.compose.ui.graphics.Color

fun instrumentColor(instrumentId: Int): Color {
    // A predefined list of colors that blend with the app's background (#FF123456)
    // We'll map instrument IDs (assuming 1..n) to distinct hues
    return when (instrumentId) {
        1 -> Color(0xFFE57373) // Light red
        2 -> Color(0xFFF06292) // Pink
        3 -> Color(0xFFBA68C8) // Purple
        4 -> Color(0xFF9575CD) // Deep Purple
        5 -> Color(0xFF64B5F6) // Blue (complementary to background)
        6 -> Color(0xFF4FC3F7) // Light Blue
        7 -> Color(0xFF81C784) // Green
        8 -> Color(0xFFFFD54F) // Yellow
        9 -> Color(0xFFFFB74D) // Orange
        10 -> Color(0xFFA1887F) // Brown
        else -> Color(0xFFB0BEC5) // Grey
    }
}