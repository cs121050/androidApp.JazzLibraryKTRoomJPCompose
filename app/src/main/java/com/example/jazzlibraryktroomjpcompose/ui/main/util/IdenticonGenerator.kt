package com.example.jazzlibraryktroomjpcompose.ui.main.util


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist

fun generateIdenticon(artistFullName: String, instrumentId: Int, size: Int = 200): Bitmap {
    // Get base color from instrument (your existing function)
    val baseColor = instrumentColor(instrumentId).toArgb()

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = baseColor
        style = Paint.Style.FILL
    }

    val cellSize = size / 5
    val hash = artistFullName.hashCode()

    // 5x5 grid, mirrored on the left half
    for (row in 0 until 5) {
        for (col in 0 until 3) { // only left half (columns 0,1,2)
            val bitIndex = row * 5 + col
            val bit = (hash ushr bitIndex) and 1
            if (bit == 1) {
                // Draw block at (col, row)
                val left = col * cellSize
                val top = row * cellSize
                canvas.drawRect(
                    left.toFloat(), top.toFloat(),
                    (left + cellSize).toFloat(), (top + cellSize).toFloat(),
                    paint
                )

                // Mirror to the right side (except middle column)
                if (col != 2) {
                    val mirroredLeft = (4 - col) * cellSize
                    canvas.drawRect(
                        mirroredLeft.toFloat(), top.toFloat(),
                        (mirroredLeft + cellSize).toFloat(), (top + cellSize).toFloat(),
                        paint
                    )
                }
            }
        }
    }

    return bitmap
}