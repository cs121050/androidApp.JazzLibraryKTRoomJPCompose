package com.example.jazzlibraryktroomjpcompose.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.domain.models.Instrument

@Composable
fun RightDrawer(
    uiState: MainUiState,
    filterState: FilterState,
    isOpen: Boolean,
    onChipSelected: (Int, Int, String, Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            .padding(16.dp)
    ) {
        // Drawer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close Filters")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Instrument Chip Group (Vertical Scrollable)
        ChipGroupSection(
            title = "Instruments",
            categoryId = FilterPath.CATEGORY_INSTRUMENT,
            items = if (filterState.currentFilterPath.isEmpty()) {
                uiState.allInstruments
            } else {
                uiState.availableInstruments
            },
            currentFilterPath = filterState.currentFilterPath,
            onChipSelected = onChipSelected,
            modifier = Modifier.weight(1f)
        )

        // We'll add more chip groups (Artist, Duration, Type) later
        // For now, just show the Instrument group as requested
    }
}

@Composable
fun ChipGroupSection(
    title: String,
    categoryId: Int,
    items: List<Any>, // Could be Instrument, Artist, etc.
    currentFilterPath: List<FilterPath>,
    onChipSelected: (Int, Int, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items) { item ->
                when (item) {
                    is Instrument -> {
                        val isSelected = currentFilterPath.any {
                            it.categoryId == categoryId && it.entityId == item.id
                        }

                        FilterChipItem(
                            id = item.id,
                            name = item.name,
                            categoryId = categoryId,
                            isSelected = isSelected,
                            onSelectedChange = { selected ->
                                onChipSelected(categoryId, item.id, item.name, selected)
                            },
                            additionalInfo = "ID: ${item.id}" // Show all columns if needed
                        )
                    }
                    // We'll add other types later
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    id: Int,
    name: String,
    categoryId: Int,
    isSelected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    additionalInfo: String? = null
) {
    FilterChip(
        selected = isSelected,
        onClick = { onSelectedChange(!isSelected) },
        label = {
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium
                )
                additionalInfo?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}