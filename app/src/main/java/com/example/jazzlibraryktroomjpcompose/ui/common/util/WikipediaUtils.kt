package com.example.jazzlibraryktroomjpcompose.ui.common.util

import org.json.JSONObject

data class WikipediaSummary(
    val title: String,
    val extract: String,
    val thumbnail: String? = null
)

fun cleanWikipediaText(rawText: String): String? {
    val lines = rawText.lines().toMutableList()

    // Remove everything up to and including the first "edit" line (main header)
    val firstEditIndex = lines.indexOfFirst { it.trim() == "edit" }
    if (firstEditIndex != -1) {
        lines.subList(0, firstEditIndex + 1).clear()
    }

    // Remove any subsequent "edit" line and everything after it
    val secondEditIndex = lines.indexOfFirst { it.trim() == "edit" }
    if (secondEditIndex != -1) {
        lines.subList(secondEditIndex, lines.size).clear()
    }

    val cleanedLines = mutableListOf<String>()
    for (line in lines) {
        var l = line.trim()
        if (l.isEmpty()) continue

        // Remove citations like [1], [2]
        l = l.replace(Regex("\\[.*?\\]"), "")

        // Skip lines that start with "obj"
        if (l.startsWith("obj", ignoreCase = true)) continue

        // Stop at footnote markers (lines starting with '^')
        if (l.startsWith('^')) break

        if (l.isNotBlank()) {
            cleanedLines.add(l)
        }
    }

    val result = cleanedLines.joinToString("\n")
    return if (result.length >= 40) result else null
}

fun parseWikipediaData(jsonString: String?): List<Pair<String, String>> {
    if (jsonString.isNullOrBlank()) return emptyList()
    return try {
        val json = JSONObject(jsonString)
        val keys = json.keys()
        val list = mutableListOf<Pair<String, String>>()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.getString(key)
            list.add(key to value)
        }
        list
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}