package com.example.jazzlibraryktroomjpcompose.ui.main

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.SearchHistoryRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSearchBar(
    viewModel: MainViewModel,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isScrollLocked by remember { mutableStateOf(false) }
    var unlockJob by remember { mutableStateOf<Job?>(null) }
    var text by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(0) }
    var isFocused by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    val searchHistory by viewModel.searchHistory.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    var manualBackHandled by remember { mutableStateOf(false) }

    Log.d("SmartSearchBar", "Compose: showSuggestions=$showSuggestions, isImeVisible=$isImeVisible")


    // Hide suggestions when keyboard is hidden (IME gone), but skip if manually handled
    LaunchedEffect(isImeVisible) {
        Log.d("SmartSearchBar", "LaunchedEffect: isImeVisible=$isImeVisible, manualBackHandled=$manualBackHandled")
        if (!isImeVisible && showSuggestions && !manualBackHandled) {
            Log.d("SmartSearchBar", "IME hidden → hiding suggestions")
            showSuggestions = false
        }
        // Reset manual flag after a short delay (prevent race)
        if (manualBackHandled) {
            delay(200)
            manualBackHandled = false
        }
    }

    // Single back press: hide keyboard, clear focus, hide suggestions
    BackHandler(enabled = showSuggestions) {
        Log.d("SmartSearchBar", "BackHandler triggered, showSuggestions=$showSuggestions")
        manualBackHandled = true
        keyboardController?.hide()
        focusManager.clearFocus()
        showSuggestions = false
        Log.d("SmartSearchBar", "After BackHandler: showSuggestions=$showSuggestions")
    }

    val modeIcon = @Composable {
        Icon(
            imageVector = when (mode) {
                0 -> Icons.Default.PlayArrow
                1 -> Icons.Default.Person
                2 -> Icons.Default.Album
                else -> Icons.Default.Search
            },
            contentDescription = "Search mode",
            modifier = Modifier.size(24.dp)
        )
    }

    LaunchedEffect(isFocused, text) {
        Log.d("SmartSearchBar", "Focus or text changed: isFocused=$isFocused, text='$text'")
        val newShow = isFocused && text.isEmpty()
        if (newShow != showSuggestions) {
            Log.d("SmartSearchBar", "Changing showSuggestions from $showSuggestions to $newShow")
            showSuggestions = newShow
        }
        if (!showSuggestions) {
            isScrollLocked = false
            unlockJob?.cancel()
        }
    }

    val nestedScrollConnection = remember(isScrollLocked) {
        object : NestedScrollConnection {
            override suspend fun onPreFling(available: Velocity): Velocity {
                return if (isScrollLocked) available else Velocity.Zero
            }
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return if (isScrollLocked && source == NestedScrollSource.Drag) {
                    Offset(0f, available.y)
                } else Offset.Zero
            }
        }
    }

    fun performSearch(query: String, searchMode: Int) {
        if (query.isNotBlank()) {
            viewModel.handleChipSelection(
                categoryId = FilterPath.CATEGORY_SEARCH,
                entityId = searchMode,
                entityName = query,
                isSelected = true
            )
            text = ""
            showSuggestions = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .onFocusChanged { focusState ->
                Log.d("SmartSearchBar", "onFocusChanged: ${focusState.isFocused}")
                isFocused = focusState.isFocused
                if (!isFocused) showSuggestions = false
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search videos, artists, albums...") },
                leadingIcon = {
                    IconButton(
                        onClick = { mode = (mode + 1) % 3 },
                        enabled = isFocused
                    ) { modeIcon() }
                },
                trailingIcon = {
                    Row {
                        if (text.isNotEmpty()) {
                            IconButton(onClick = { text = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                        IconButton(onClick = onFilterClick) {
                            Icon(Icons.Default.FilterList, contentDescription = "Open Filters", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (text.isNotBlank()) performSearch(text, mode)
                })
            )

            if (showSuggestions && searchHistory.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .nestedScroll(nestedScrollConnection)
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                var localUnlockJob: Job? = null
                                try {
                                    awaitFirstDown(requireUnconsumed = false)
                                    isScrollLocked = true
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.type == PointerEventType.Release || event.type == PointerEventType.Exit) {
                                            localUnlockJob = coroutineScope.launch {
                                                delay(100)
                                                isScrollLocked = false
                                            }
                                            break
                                        }
                                    }
                                } catch (e: CancellationException) {
                                    localUnlockJob?.cancel()
                                    isScrollLocked = false
                                    throw e
                                }
                            }
                        }
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        items(searchHistory) { entry ->
                            SuggestionItem(
                                entry = entry,
                                onClick = {
                                    text = entry.query
                                    mode = entry.mode
                                    showSuggestions = false
                                    isScrollLocked = false
                                },
                                onDelete = {
                                    coroutineScope.launch { viewModel.deleteSearchHistoryEntry(entry) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    entry: SearchHistoryRoomEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                imageVector = when (entry.mode) {
                    0 -> Icons.Default.PlayArrow
                    1 -> Icons.Default.Person
                    2 -> Icons.Default.Album
                    else -> Icons.Default.Search
                },
                contentDescription = "Mode",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = entry.query,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Delete",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Divider()
}