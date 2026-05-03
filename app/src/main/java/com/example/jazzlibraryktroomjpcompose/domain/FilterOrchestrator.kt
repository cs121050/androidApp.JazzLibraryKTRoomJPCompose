package com.example.jazzlibraryktroomjpcompose.domain

import android.util.Log
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.domain.repository.FilterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure domain layer – contains business rules, no database access.
 */
@Singleton
class FilterOrchestrator @Inject constructor(
    private val filterRepository: FilterRepository
) {

    fun getFilteredDataFlow(filterPath: List<FilterPath>): Flow<FilterRepository.FilteredData> =
        filterRepository.getFilteredDataFlow(filterPath)

    // In FilterOrchestrator.kt
    suspend fun handleChipSelection(
        currentFilterPath: List<FilterPath>,
        selectedCategoryId: Int,
        selectedEntityId: Int,
        selectedEntityName: String,
        isSelected: Boolean  // new parameter
    ): List<FilterPath> {
        // Special case: SEARCH chip
        if (selectedCategoryId == FilterPath.CATEGORY_SEARCH) {
            return if (isSelected) {
                // Replace: remove any existing search chip, then add the new one
                val filteredPath = currentFilterPath.filterNot { it.categoryId == FilterPath.CATEGORY_SEARCH }
                filteredPath + FilterPath(
                    categoryId = selectedCategoryId,
                    entityId = selectedEntityId,
                    entityName = selectedEntityName
                )
            } else {
                // Deselection: remove the specific search chip (match category, entityId, and entityName)
                currentFilterPath.filterNot {
                    it.categoryId == selectedCategoryId &&
                            it.entityId == selectedEntityId &&
                            it.entityName == selectedEntityName
                }
            }
        }

        // Original logic for other categories (no change needed)
        val result = when {
            // Deselection case (chip already selected)
            currentFilterPath.any { it.categoryId == selectedCategoryId && it.entityId == selectedEntityId } -> {
                val newPath = currentFilterPath.filterNot {
                    it.categoryId == selectedCategoryId && it.entityId == selectedEntityId
                }
                if (selectedCategoryId == FilterPath.CATEGORY_INSTRUMENT) {
                    newPath.filterNot { it.categoryId == FilterPath.CATEGORY_ARTIST }
                } else {
                    newPath
                }
            }

            // Selection case
            else -> {
                var filteredPath = currentFilterPath.filterNot { it.categoryId == selectedCategoryId }

                when (selectedCategoryId) {
                    FilterPath.CATEGORY_ARTIST -> {
                        filteredPath = filteredPath.filterNot { it.categoryId == FilterPath.CATEGORY_INSTRUMENT }
                        val instrumentPair = filterRepository.getArtistInstrument(selectedEntityId)
                        val newPath = filteredPath.toMutableList()
                        if (instrumentPair != null) {
                            val (instrumentId, instrumentName) = instrumentPair
                            if (instrumentId > 0 && instrumentName.isNotBlank()) {
                                newPath.add(
                                    FilterPath(
                                        categoryId = FilterPath.CATEGORY_INSTRUMENT,
                                        entityId = instrumentId,
                                        entityName = instrumentName
                                    )
                                )
                            }
                        }
                        newPath.add(
                            FilterPath(
                                categoryId = FilterPath.CATEGORY_ARTIST,
                                entityId = selectedEntityId,
                                entityName = selectedEntityName
                            )
                        )
                        newPath.toList()
                    }

                    FilterPath.CATEGORY_INSTRUMENT -> {
                        filteredPath = filteredPath.filterNot { it.categoryId == FilterPath.CATEGORY_ARTIST }
                        filteredPath + FilterPath(
                            categoryId = selectedCategoryId,
                            entityId = selectedEntityId,
                            entityName = selectedEntityName
                        )
                    }

                    else -> {
                        filteredPath + FilterPath(
                            categoryId = selectedCategoryId,
                            entityId = selectedEntityId,
                            entityName = selectedEntityName
                        )
                    }
                }
            }
        }
        return result.distinctBy { it.categoryId }
    }

    suspend fun handleChipDeselection(
        currentFilterPath: List<FilterPath>,
        categoryId: Int,
        entityId: Int
    ): List<FilterPath> {
        val result = when (categoryId) {
            FilterPath.CATEGORY_INSTRUMENT -> {
                currentFilterPath.filterNot { filter ->
                    filter.categoryId == categoryId || filter.categoryId == FilterPath.CATEGORY_ARTIST
                }
            }

            FilterPath.CATEGORY_SEARCH -> {
                currentFilterPath.filterNot { it.categoryId == categoryId && it.entityId == entityId }
            }

            else -> {
                currentFilterPath.filterNot {
                    it.categoryId == categoryId && it.entityId == entityId
                }
            }
        }
        return result.distinctBy { it.categoryId }
    }
}