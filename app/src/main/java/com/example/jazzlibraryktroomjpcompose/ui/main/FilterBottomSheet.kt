package com.example.jazzlibraryktroomjpcompose.ui.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    uiState: MainUiState,
    filterState: FilterState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Sheet takes 70% of screen
    val sheetHeight = screenHeight * 0.7f

    // Animate in/out - REVERSED LOGIC
    // When hidden: sheetHeight (below screen bottom)
    // When visible: 0.dp (at screen bottom)
    val offset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else sheetHeight, // FIXED: Reversed
        animationSpec = tween(300),
        label = "sheetAnimation"
    )

    // Use Popup to ensure it's on top of everything
    if (isVisible) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                excludeFromSystemGesture = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(onClick = onDismiss)
            ) {
                // Bottom sheet content - positioned at BOTTOM
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(sheetHeight)
                        .align(Alignment.BottomStart) // FIXED: Align to bottom
                        .offset(y = offset) // FIXED: Animate from bottom
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .clickable { } // Prevent clicks from passing through
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        )
                    }

                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close filters",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Content area
                    FilterSheetContent(
                        uiState = uiState,
                        filterState = filterState,
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSheetContent(
    uiState: MainUiState,
    filterState: FilterState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Instrument Chip Group
        FilterChipGroupSection(
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

        Spacer(modifier = Modifier.height(32.dp))

        // Artist Chip Group (Placeholder for now)
        PlaceholderChipGroup(
            title = "Artists",
            count = uiState.availableArtists.size
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Duration Chip Group (Placeholder for now)
        PlaceholderChipGroup(
            title = "Durations",
            count = uiState.availableDurations.size
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Type Chip Group (Placeholder for now)
        PlaceholderChipGroup(
            title = "Types",
            count = uiState.availableTypes.size
        )

        // Add some bottom padding for scrolling
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun PlaceholderChipGroup(
    title: String,
    count: Int
) {
    Column {
        Text(
            text = "$title ($count)",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            repeat(minOf(3, count)) { index ->
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = {
                        Text("Coming Soon")
                    },
                    enabled = false,
                    colors = FilterChipDefaults.filterChipColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }

        if (count > 3) {
            Text(
                text = "+ ${count - 3} more",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}