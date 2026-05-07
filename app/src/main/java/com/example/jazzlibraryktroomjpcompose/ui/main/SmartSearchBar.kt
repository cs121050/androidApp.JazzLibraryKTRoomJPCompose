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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.SearchHistoryRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.Album
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.domain.models.Video
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "SmartSearchBar"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSearchBar(
    viewModel: MainViewModel,
    onFilterClick: () -> Unit,
    hideSearchDropdown: Boolean,
    onSearchBarClicked: () -> Unit,
    onDropdownVisibilityChanged: (Boolean) -> Unit,
    allVideos: List<Video>,   // <-- filtered videos (respect current filter path)
    onVideoSelected: (Video) -> Unit,
    allArtists: List<Artist>,
    allAlbums: List<Album>,
    onExpandToolbar: () -> Unit,
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

    // Compute autocomplete items – NO LIMIT, shows all matching video titles
    val autocompleteItems = remember(text, mode, allVideos, allArtists, allAlbums) {
        if (text.isBlank()) return@remember emptyList()
        val lowerText = text.lowercase()
        when (mode) {
            0 -> allVideos.filter { it.name.contains(lowerText, ignoreCase = true) }.map { it.name }
            1 -> allArtists.filter { it.fullName.contains(lowerText, ignoreCase = true) }.map { it.fullName }
            2 -> allAlbums.filter { it.title.contains(lowerText, ignoreCase = true) }.map { it.title }
            else -> emptyList()
        }
    }


    // Decide what to show: autocomplete or history
    val showAutocomplete = autocompleteItems.isNotEmpty()

    // Single source of truth for showing dropdown
    LaunchedEffect(isFocused, text, hideSearchDropdown, mode, autocompleteItems) {
        val newShow = when {
            !isFocused -> false
            hideSearchDropdown -> false
            text.isEmpty() -> true                     // show history
            autocompleteItems.isNotEmpty() -> true     // show autocomplete for any mode
            else -> false
        }
        if (newShow != showSuggestions) {
            showSuggestions = newShow
        }
        if (!showSuggestions) {
            isScrollLocked = false
            unlockJob?.cancel()
        }
    }

    // Callback to parent when dropdown visibility changes
    LaunchedEffect(showSuggestions) {
        onDropdownVisibilityChanged(showSuggestions)
    }

    BackHandler(enabled = showSuggestions) {
        Log.d(TAG, "BackHandler: closing dropdown")
        keyboardController?.hide()
        focusManager.clearFocus()
        showSuggestions = false
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

    fun performSearch(query: String, searchMode: Int, isMedia: Int = 0) {
        if (query.isNotBlank()) {
            viewModel.handleChipSelection(
                categoryId = FilterPath.CATEGORY_SEARCH,
                entityId = searchMode,
                entityName = query,
                isSelected = true,
                isMedia = isMedia
            )
            text = ""
            showSuggestions = false
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (!isFocused) showSuggestions = false
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = text,
                onValueChange = { newText ->
                    text = newText
                    onSearchBarClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        if (focusState.isFocused) {
                            onSearchBarClicked()
                            onExpandToolbar()
                        }
                    },
                placeholder = { Text("Search videos, artists, albums...") },
                leadingIcon = {
                    if (isFocused) {
                        IconButton(onClick = { mode = (mode + 1) % 3 }) {
                            modeIcon()
                        }
                    } else {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

            // Dropdown: show autocomplete OR history
            if (showSuggestions && (showAutocomplete || searchHistory.isNotEmpty())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)   // increased height for scrolling
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
                    val historyToShow = searchHistory.filter { it.mode == mode }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        if (showAutocomplete) {
                            items(autocompleteItems) { title ->
                                AutocompleteItem(
                                    title = title,
                                    searchText = text,
                                    mode = mode,
                                    onClick = {
                                        Log.d(TAG, "Autocomplete selected: $title")
                                        performSearch(title, mode, 1)
                                    }
                                )
                            }
                        } else {
                            items(historyToShow) { entry ->
                                SuggestionItem(
                                    entry = entry,
                                    onClick = {
                                        if (entry.isMedia == 1) {
                                            // Immediate filter/search (like autocomplete)
                                            performSearch(entry.query, entry.mode, 1)
                                        } else {
                                            // Fill the search bar (no auto-search)
                                            text = entry.query
                                            mode = entry.mode
                                            showSuggestions = false
                                            isScrollLocked = false
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        }
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
}

@Composable
private fun AutocompleteItem(
    title: String,
    searchText: String,
    mode:Int,
    onClick: () -> Unit
) {
    // Build text with bold + blue highlight for the matching substring
    val annotatedText = buildAnnotatedString {
        val lowerTitle = title.lowercase()
        val lowerSearch = searchText.lowercase()
        var startIndex = 0
        while (true) {
            val index = lowerTitle.indexOf(lowerSearch, startIndex)
            if (index == -1) {
                append(title.substring(startIndex))
                break
            }
            if (index > startIndex) {
                append(title.substring(startIndex, index))
            }
            pushStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary   // blue highlight
                )
            )
            append(title.substring(index, index + searchText.length))
            pop()
            startIndex = index + searchText.length
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = when (mode) {
                    0 -> Icons.Default.PlayArrow
                    1 -> Icons.Default.Person
                    2 -> Icons.Default.Album
                    else -> Icons.Default.Search
                },
                contentDescription = "Search",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = annotatedText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        // No delete button for autocomplete
    }
    Divider()
}

@Composable
private fun SuggestionItem(
    entry: SearchHistoryRoomEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = when (entry.isMedia) {
        1 -> when (entry.mode) {   // isMedia == 1 → mode-specific icon
            0 -> Icons.Default.PlayArrow
            1 -> Icons.Default.Person
            2 -> Icons.Default.Album
            else -> Icons.Default.Search
        }   // isMedia == 0 → search icon
        else -> Icons.Default.Search // fallback for null or other values
    }

    val customTint = when (entry.isMedia) {
        1 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),   // ← add this line
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ){
            Icon(
                imageVector = Icons.Default.History,
//                imageVector = when (entry.mode) {
//                    0 -> Icons.Default.PlayArrow
//                    1 -> Icons.Default.Person
//                    2 -> Icons.Default.Album
//                    else -> Icons.Default.Search
//                },
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Search history",
                modifier = Modifier.size(16.dp),
                tint = customTint
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Divider()
}