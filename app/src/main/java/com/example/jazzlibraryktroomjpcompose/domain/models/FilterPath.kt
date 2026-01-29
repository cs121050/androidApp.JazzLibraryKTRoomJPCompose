package com.example.jazzlibraryktroomjpcompose.domain.models

data class FilterPath(
    val autoIncrementId: Int = 0,
    val categoryId: Int, // 1=instrument, 2=artist, 3=duration, 4=type
    val entityId: Int,
    val entityName: String
) {
    val categoryName: String
        get() = when (categoryId) {
            CATEGORY_INSTRUMENT -> "Instrument"
            CATEGORY_ARTIST -> "Artist"
            CATEGORY_DURATION -> "Duration"
            CATEGORY_TYPE -> "Type"
            else -> "Unknown"
        }

    val displayInfo: String
        get() = "$categoryName: $entityName (ID: $entityId)"

    companion object {
        const val CATEGORY_INSTRUMENT = 1
        const val CATEGORY_ARTIST = 2
        const val CATEGORY_DURATION = 3
        const val CATEGORY_TYPE = 4

        val CATEGORIES = listOf(
            CATEGORY_INSTRUMENT to "Instrument",
            CATEGORY_ARTIST to "Artist",
            CATEGORY_DURATION to "Duration",
            CATEGORY_TYPE to "Type"
        )

        fun getCategoryName(categoryId: Int): String {
            return CATEGORIES.find { it.first == categoryId }?.second ?: "Unknown"
        }
    }

    // For easy comparison and UI operations
    fun isSameCategory(other: FilterPath): Boolean = this.categoryId == other.categoryId
    fun isSameEntity(other: FilterPath): Boolean = this.categoryId == other.categoryId && this.entityId == other.entityId

    override fun toString(): String = displayInfo
}