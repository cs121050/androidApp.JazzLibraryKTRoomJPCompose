package com.example.jazzlibraryktroomjpcompose.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath

@Composable
fun FilterBottomSheetContent(
    uiState: MainUiState,
    filterState: FilterState,
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        // Header with close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close filters",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Active filters (if any)
        if (filterState.currentFilterPath.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Active Filters:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Use Row with wrap for active filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterState.currentFilterPath.forEach { filter ->
                            // Custom chip for active filters
                            ActiveFilterChip(
                                text = filter.entityName,
                                onClick = {
                                    viewModel.handleChipSelection(
                                        filter.categoryId,
                                        filter.entityId,
                                        filter.entityName,
                                        false
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Filter categories in scrollable column
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            // Instrument Chip Group
            SimpleChipGroupSection(
                title = "Instruments",
                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                items = if (filterState.currentFilterPath.isEmpty()) {
                    uiState.allInstruments
                } else {
                    uiState.availableInstruments
                },
                currentFilterPath = filterState.currentFilterPath,
                onChipSelected = { categoryId, entityId, entityName, isSelected ->
                    viewModel.handleChipSelection(categoryId, entityId, entityName, isSelected)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Clear All Filters Button at bottom
            if (filterState.currentFilterPath.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { viewModel.clearAllFilters() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ClearAll, contentDescription = "Clear all")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Filters")
                }
            }
        }
    }
}

// Custom Active Filter Chip (for the top section)
@Composable
fun ActiveFilterChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Simple Chip Group Section using Custom Chips
@Composable
fun SimpleChipGroupSection(
    title: String,
    categoryId: Int,
    items: List<Any>,
    currentFilterPath: List<FilterPath>,
    onChipSelected: (Int, Int, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Simple column layout - one chip per line
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                when (item) {
                    is com.example.jazzlibraryktroomjpcompose.domain.models.Instrument -> {
                        val isSelected = currentFilterPath.any {
                            it.categoryId == categoryId && it.entityId == item.id
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onChipSelected(categoryId, item.id, item.name, !isSelected)
                            },
                            label = { Text(item.name) },
                            enabled = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    // Add other item types here when you implement them
                }
            }
        }
    }
}

// OR if FilterChip still doesn't work, use this custom chip:
@Composable
fun CustomFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .border(
                BorderStroke(
                    if (isSelected) 1.dp else 0.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
    }
}