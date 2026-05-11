package com.example.jazzlibraryktroomjpcompose.ui.common.util

import androidx.compose.ui.graphics.Color

fun instrumentColor(instrumentId: Int): Color {
    // A predefined list of colors that blend with the app's background (#FF123456)
    // We'll map instrument IDs (assuming 1..n) to distinct hues
    return when (instrumentId) {
        1 -> Color(0xFF5C6BC0) // Bass – deep indigo, brightened for contrast
        2 -> Color(0xFF2C7DA0) // Guitar – warm teal-blue (distinct from piano)
        3 -> Color(0xFF4FC3F7) // Piano – cool light blue
        4 -> Color(0xFFFF80C0) // Drums – light purple (sharp, bright)
        5 -> Color(0xFFFF8C69) // Voice – warm salmon (complementary to background)
        6 -> Color(0xFFFFA500) // Sax – warm amber (brass, warmer than trumpet)
        7 -> Color(0xFFFDE603) // Trumpet – brightest yellow
        8 -> Color(0xFF81C79B) // Violin – soft green (lyrical)
        9 -> Color(0xFFB085FF) // Vibes – vibrant purple (shimmering percussion)
        10 -> Color(0xFFAED581) // Clarinete – light green (agile woodwind)
        11 -> Color(0xFFF57C00) // Trombone – warm bronze (bass instrument)
        12 -> Color(0xFFCFCFCF) // Journalism – light gray (neutral, unrelated)
        13 -> Color(0xFFE57373) // Other – soft red (fallback, good contrast)
        else -> Color(0xFFB0BEC5) // Grey – light blue‑gray

        /*
        1 -> Color(0xFFE57373) // Light red, bass
2 -> Color(0xFFF06292) // Pink, guitar
3 -> Color(0xFFBA68C8) // Purple, piano
4 -> Color(0xFF9575CD) // Deep Purple, drums
5 -> Color(0xFF64B5F6) // Blue (complementary to background), voice
6 -> Color(0xFF4FC3F7) // Light Blue, sax
7 -> Color(0xFF81C784) // Green, trumpet
8 -> Color(0xFFFFD54F) // Yellow, violin
9 -> Color(0xFFFFB74D) // Orange, vibes
10 -> Color(0xFFA1887F) // Brown, clarinete
11 -> Color(0xFFBCAAA4) // Tan, trombone
12 -> Color(0xFFE1BEE7) // Lavender, journalism
13 -> Color(0xFFCFD8DC) // Blue Grey, other
else -> Color(0xFFB0BEC5) // Grey
         */


    }
}